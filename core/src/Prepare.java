import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import saturn.config.Globals;




public class Prepare {




	public static String domain;
	public static File initListFile;
	public static File ignoreRulesFile;
	public static File foundURLsHashesFile;
	public static String baseDomain;
	
	
	
	
	public static void main(String[] args) throws Exception{
		parseArguments(args);
	}
	
	
	
	
	public static CommandLine parseArguments(String[] args) throws Exception{
		
		Options options = new Options();
		options.addOption("domain", true, "domain");
		options.addOption("threads", true, "threads");
		options.addOption("clearCache", true, "clearCache");
		options.addOption("downloadedURLsDirectory", true, "downloadedURLsDirectory");
		options.addOption("initListFilePath", true, "initListFilePath");
		options.addOption("ignoreRulesFilePath", true, "ignoreRulesFilePath");
		options.addOption("foundURLsHashesFilePath", true, "foundURLsHashesFilePath");
		options.addOption("cookies", true, "cookies");

		CommandLineParser parser = new PosixParser();

		return parser.parse(options, args, true);
		
	}
	
	
	
	
	public static void configFiles(CommandLine cmd) throws Exception{
		
		domain = cmd.getOptionValue("domain");
		String initListFilePath = cmd.getOptionValue("initListFilePath");
		String ignoreRulesFilePath = cmd.getOptionValue("ignoreRulesFilePath");
		String foundURLsHashesFilePath = cmd.getOptionValue("foundURLsHashesFilePath");
		
		System.out.println(domain);
		
		if(domain != null){
			baseDomain = new URL(domain).getAuthority();
		}
		
		if(initListFilePath == null){
			initListFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + "-initList.txt");
		}else{
			
			initListFilePath = initListFilePath.replaceAll("~", System.getProperty("user.home"));
			
			initListFile = new File(initListFilePath);
			
		}

		if(ignoreRulesFilePath == null){
			ignoreRulesFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + "-ignoreRules.txt");
		}else{
			
			ignoreRulesFilePath = ignoreRulesFilePath.replaceAll("~", System.getProperty("user.home"));
			
			ignoreRulesFile = new File(ignoreRulesFilePath);
			
			
		}

		if(foundURLsHashesFilePath == null){
			foundURLsHashesFile = new File(Globals.SERVICE_DIRECTORY + "/" + baseDomain + ".txt");
		}else{
			
			foundURLsHashesFilePath = foundURLsHashesFilePath.replaceAll("~", System.getProperty("user.home"));
			
			foundURLsHashesFile = new File(foundURLsHashesFilePath);
			
		}
		
		initListFile.createNewFile();
		ignoreRulesFile.createNewFile();
		foundURLsHashesFile.createNewFile();
		
		System.out.println("initListFilePath: " + initListFile);
		System.out.println("ignoreRulesFilePath: " + ignoreRulesFile);
		System.out.println("foundURLsHashesFilePath: " + foundURLsHashesFile);
		
	}

}
