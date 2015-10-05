import java.io.File;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import saturn.config.Globals;
import saturn.utils.Download;
import saturn.utils.DownloadByteResult;




public class Scrape {




	private static int threads;
	private static File downloadedURLsDirectory;
	private static String cookies;
	private static ConcurrentHashMap<String, String> pendingURLs = new ConcurrentHashMap<String, String>();
	private static CopyOnWriteArrayList<String> downloadedHashes = new CopyOnWriteArrayList<String>();




	public static void main(String[] args) throws Exception{

		CommandLine commandLine = Prepare.parseArguments(args);

		Prepare.configFiles(commandLine);

		prepareScraper(commandLine);

		cacheFoundURLsHashes();

		System.out.println("\n>> foundURLs: " + pendingURLs.size() + "\n");

		for(int i = 0; i < threads; i++){
			new Thread(new Worker()).start();
		}

		while(pendingURLs.size() > 0){
			Thread.sleep(250);
		}

	}




	private static void prepareScraper(CommandLine cmd) throws Exception{

		threads = Integer.parseInt(cmd.getOptionValue("threads"));
		boolean clearCache = Boolean.parseBoolean(cmd.getOptionValue("clearCache"));
		String downloadedURLsDirectoryPath = cmd.getOptionValue("downloadedURLsDirectory");
		cookies = cmd.getOptionValue("cookies");

		if(downloadedURLsDirectoryPath == null){
			downloadedURLsDirectory = new File(Globals.SERVICE_DIRECTORY + "/" + Prepare.baseDomain);
		}else{
			
			downloadedURLsDirectoryPath = downloadedURLsDirectoryPath.replaceAll("~", System.getProperty("user.home"));
			
			downloadedURLsDirectory = new File(downloadedURLsDirectoryPath);
			
		}

		if(cookies != null){
			cookies = cookies.replaceAll("'", "");
		}

		if(clearCache){

			Prepare.foundURLsHashesFile.delete();
			System.out.println("<< deleted: " + Prepare.foundURLsHashesFile);

			downloadedURLsDirectory.delete();
			System.out.println("<< deleted: " + downloadedURLsDirectory);

		}
		
		System.out.println("threads: " + threads);
		System.out.println("clearCache: " + clearCache);
		System.out.println("downloadedURLsDirectoryPath: " + downloadedURLsDirectoryPath);
		System.out.println("cookies: " + cookies);

	}




	private static void cacheFoundURLsHashes() throws Exception{

		if(Prepare.foundURLsHashesFile.exists()){

			String[] lines = FileUtils.readFileToString(Prepare.foundURLsHashesFile).split("\n");

			for(int i = 0; i < lines.length; i++){

				if(!lines[i].equals("")){

					String hash = lines[i].split(" ")[0];
					String url = lines[i].split(" ")[1];

					pendingURLs.put(hash, url);

				}

			}

		}

		if(Prepare.initListFile.exists()){

			String[] lines = FileUtils.readFileToString(Prepare.initListFile).split("\n");

			for(int i = 0; i < lines.length; i++){

				if(!lines[i].equals("")){

					String url = lines[i].split(" ")[0];
					String hash = getHash(url);

					System.out.println("initList: " + url);

					pendingURLs.put(hash, url);

				}

			}

		}

		pendingURLs.put(getHash(Prepare.domain), Prepare.domain);

	}




	private static void addFoundURL(String url) throws Exception{

		url = url.trim();

		String hash = getHash(url);

		if(!exists(url, hash)){

			String line = hash + " " + url + "\n";

			FileUtils.writeStringToFile(Prepare.foundURLsHashesFile, line, true);

		}

	}




	private static synchronized boolean exists(String url, String hash){

		boolean exists = downloadedHashes.contains(hash) || pendingURLs.containsKey(hash);

		if(!exists){

			pendingURLs.put(hash, url);
			downloadedHashes.add(hash);

		}

		return exists;

	}




	private static String[] getIgnoreRules() throws Exception{

		if(!Prepare.ignoreRulesFile.exists()){
			FileUtils.writeStringToFile(Prepare.ignoreRulesFile, "");
		}

		return FileUtils.readFileToString(Prepare.ignoreRulesFile).split("\n");

	}




	private static synchronized String getNextURL(){

		if(pendingURLs.size() > 0){

			Entry<String, String> entry = pendingURLs.entrySet().iterator().next();

			pendingURLs.remove(entry.getKey());

			downloadedHashes.add(entry.getKey());

			return entry.getValue();

		}else{
			return null;
		}

	}




	private static String getHash(String url){
		return DigestUtils.md5Hex(url);
	}




	private static class Worker implements Runnable {




		public void run(){

			String url = getNextURL();

			if(url != null){

				try{

					if(isValid(url)){

						System.out.println(Prepare.baseDomain + " -pending: " + pendingURLs.size() + " - " + url);

						String hash = getHash(url);

						File file = new File(downloadedURLsDirectory.getAbsoluteFile() + "/" + hash);

						if(!file.exists()){

							DownloadByteResult download = new Download().getURLBytesWithCookieAndPost(url, cookies, null);

							FileUtils.writeByteArrayToFile(file, download.bytes);
							
							extractLinks(download.bytes);

						}

					}else{
						System.err.println("ignoring: " + url);
					}

				}catch(Exception e){
					e.printStackTrace();
				}

				new Thread(new Worker()).start();

			}

		}




		private boolean isValid(String url) throws Exception{

			String[] ignoreRules = getIgnoreRules();
			boolean valid = true;

			for(int i = 0; i < ignoreRules.length; i++){

				if(!ignoreRules[i].equals("")){

					Pattern pattern = Pattern.compile(ignoreRules[i]);
					Matcher matcher = pattern.matcher(url);

					if(matcher.find()){

						valid = false;
						break;

					}

				}

			}

			return valid;

		}




		private void extractLinks(byte[] bytes) throws Exception{

			String html = new String(bytes);

			Document doc = Jsoup.parse(html);

			for(Element element : doc.select("a")){

				String url = element.attr("href");

				if(url.startsWith("/")){
					url = Prepare.domain + url;
				}

				if(url.contains(Prepare.domain) && isValid(url)){
					addFoundURL(url);
				}

			}

		}

	}

}
