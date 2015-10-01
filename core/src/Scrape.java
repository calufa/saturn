




import java.io.File;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
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
	private static String domain;
	private static File foundURLsHashesFile;
	private static File ignoreRulesFile;
	private static File initListFile;
	private static File downloadedURLsDirectory;
	private static ConcurrentHashMap<String, String> pendingURLs = new ConcurrentHashMap<String, String>();
	private static CopyOnWriteArrayList<String> downloadedHashes = new CopyOnWriteArrayList<String>();




	public static void main(String[] args){

		try{

			prepare(args);

			cacheFoundURLsHashes();

			System.out.println("foundURLs: " + pendingURLs.size());

			for(int i = 0; i < threads; i++){
				new Thread(new Worker()).start();
			}

			while(pendingURLs.size() > 0){
				Thread.sleep(250);
			}

		}catch(Exception e){
			e.printStackTrace();
		}

	}




	private static void prepare(String[] args) throws Exception{

		Options options = new Options();
		options.addOption("domain", true, "domain");
		options.addOption("threads", true, "threads");
		options.addOption("clearCache", true, "clearCache");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args, true);

		domain = cmd.getOptionValue("domain");
		threads = Integer.parseInt(cmd.getOptionValue("threads"));
		boolean clearCache = Boolean.parseBoolean(cmd.getOptionValue("clearCache"));
		
		System.out.println("URL: " + domain);
		System.out.println("threads: " + threads);
		System.out.println("domain: " + domain);
		System.out.println("clearCache: " + clearCache);

		String baseDomain = new URL(domain).getAuthority();

		foundURLsHashesFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + ".txt");
		ignoreRulesFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + "-ignoreRules.txt");
		initListFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + "-initList.txt");
		downloadedURLsDirectory = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain);
		
		if(clearCache){
			
			foundURLsHashesFile.delete();
			System.out.println("<< deleted: " + foundURLsHashesFile);
			
			downloadedURLsDirectory.delete();
			System.out.println("<< deleted: " + downloadedURLsDirectory);
			
		}

	}




	private static void cacheFoundURLsHashes() throws Exception{

		if(foundURLsHashesFile.exists()){

			String[] lines = FileUtils.readFileToString(foundURLsHashesFile).split("\n");

			for(int i = 0; i < lines.length; i++){

				if(!lines[i].equals("")){

					String hash = lines[i].split(" ")[0];
					String url = lines[i].split(" ")[1];

					pendingURLs.put(hash, url);

				}

			}

		}
		
		if(initListFile.exists()){
			
			String[] lines = FileUtils.readFileToString(initListFile).split("\n");

			for(int i = 0; i < lines.length; i++){

				if(!lines[i].equals("")){

					String url = lines[i].split(" ")[0];
					String hash = getHash(url);
					
					System.out.println("initList: " + url);

					pendingURLs.put(hash, url);

				}

			}
			
		}
		
		pendingURLs.put(getHash(domain), domain);

	}




	private static void addFoundURL(String url) throws Exception{

		url = url.trim();

		String hash = getHash(url);

		if(!exists(url, hash)){

			String line = hash + " " + url + "\n";

			FileUtils.writeStringToFile(foundURLsHashesFile, line, true);

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

		if(!ignoreRulesFile.exists()){
			FileUtils.writeStringToFile(ignoreRulesFile, "");
		}

		return FileUtils.readFileToString(ignoreRulesFile).split("\n");

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

						System.out.println("pending: " + pendingURLs.size() + " - " + url);

						String hash = getHash(url);

						File file = new File(downloadedURLsDirectory.getAbsoluteFile() + "/" + hash);

						byte[] bytes;

						if(!file.exists()){

							DownloadByteResult download = new Download().getURLBytes(url);

							FileUtils.writeByteArrayToFile(file, download.bytes);

							bytes = download.bytes;

						}else{
							bytes = FileUtils.readFileToByteArray(file);
						}

						extractLinks(bytes);

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
					url = domain + url;
				}

				if(url.contains(domain) && isValid(url)){
					addFoundURL(url);
				}

			}

		}

	}

}
