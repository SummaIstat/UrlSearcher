package it.istat.urlsearcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


//*******************************
//***** Author				*****
//***** Donato Summa		*****
//*******************************

public class UrlSearcher {

    //private static final String URLEncoder = null;
	static Logger logger = Logger.getLogger(UrlSearcher.class);
	List<String> firmNamesList = new ArrayList<String>();
    List<String> firmIdsList = new ArrayList<String>();
    
    // program parameters
    private static String proxyHost = null;
    private static String proxyPort = null;
    private static String firmsNamesFilePath;
    private static String firmsIdsFilePath;
    private static String txtFilesFolderPath;
    private static String seedFileFolderPath;
    private static String seedFileName = "seed";
    private static String logFilePath;
    
    public static void main(String[] args) throws IOException {
        
    	UrlSearcher scraper = new UrlSearcher();
        scraper.configure(args);
        
        //=====================================================================================================
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date startDateTime = new Date();
        logger.info("******************************************************** \n");
        logger.info("Starting datetime = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
    	//=====================================================================================================
                
        scraper.scrape(firmsNamesFilePath, firmsIdsFilePath, txtFilesFolderPath);
        scraper.generateUrlCrawlerSeedFile();
        
        //=====================================================================================================
      	Date endDateTime = new Date();
      	logger.info("Started at = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
      	logger.info("Ending datetime = " + dateFormat.format(endDateTime)); //15/12/2014 15:59:48
      	//=====================================================================================================
    }
    
    private void configure(String[] args) throws IOException {
    	
    	if (args.length == 1){
			if (isAValidFile(args[0])){
				FileInputStream fis = new FileInputStream(args[0]);
				InputStream inputStream = fis;
				Properties props = new Properties();
				props.load(inputStream);
				
				customizeLog(args[0]);
				
				// if you are behind a proxy you have to set the IP address and the port number of the proxy
				// PROXY_HOST
				if(props.getProperty("PROXY_HOST") != null){
					proxyHost = props.getProperty("PROXY_HOST");
					System.getProperties().put("http.proxyHost", proxyHost);
				}
				
				// PROXY_PORT
				if(props.getProperty("PROXY_PORT") != null){
					proxyPort = props.getProperty("PROXY_PORT");
					System.getProperties().put("http.proxyPort", proxyPort);
				}
				
				// Mandatory parameters
				// FIRM_NAMES_FILE_PATH
				if(props.getProperty("FIRM_NAMES_FILE_PATH") != null){
					firmsNamesFilePath = props.getProperty("FIRM_NAMES_FILE_PATH");
				}else{
					logger.error("Wrong/No configuration for the parameter FIRM_NAMES_FILE_PATH !");
					System.exit(1);
				}
				
				// FIRM_IDS_FILE_PATH
				if(props.getProperty("FIRM_IDS_FILE_PATH") != null){
					firmsIdsFilePath = props.getProperty("FIRM_IDS_FILE_PATH");
				}else{
					logger.error("Wrong/No configuration for the parameter FIRM_IDS_FILE_PATH !");
					System.exit(1);
				}
				
				// TXT_FILES_FOLDER_PATH
				if(props.getProperty("TXT_FILES_FOLDER_PATH") != null){
					txtFilesFolderPath = props.getProperty("TXT_FILES_FOLDER_PATH");
					if (!isAValidDirectory(txtFilesFolderPath)){
			        	logger.error("The TXT_FILES_FOLDER_PATH parameter that you set ( " + txtFilesFolderPath + " ) is not valid");
			        	System.exit(1);
			        }
				}else{
					logger.error("Wrong/No configuration for the parameter TXT_FILES_FOLDER_PATH !");
					System.exit(1);
				}
				
				// SEED_FILE_FOLDER_PATH
				if(props.getProperty("SEED_FILE_FOLDER_PATH") != null){
					seedFileFolderPath = props.getProperty("SEED_FILE_FOLDER_PATH");
					if (!isAValidDirectory(seedFileFolderPath)){
						logger.error("The SEED_FILE_FOLDER_PATH parameter that you set ( " + seedFileFolderPath + " ) is not valid");
			        	System.exit(1);
			        }
				}else{
					logger.error("Wrong/No configuration for the parameter SEED_FILE_FOLDER_PATH !");
					System.exit(1);
				}
				
				// LOG_FILE_PATH
				if(props.getProperty("LOG_FILE_PATH") != null){
					logFilePath = props.getProperty("LOG_FILE_PATH");
				}else{
					logger.error("Wrong/missing configuration for the parameter LOG_FILE_PATH !");
					System.exit(1);
				}
				
			} else {
				logger.error("Error opening file " + args[0] + " or non-existent file");
				logger.error("==>  program execution terminated <==");
				System.exit(1);
			}
		} else {
			logger.error("usage: java -jar UrlSearcher.jar [urlSearcherConf.properties fullpath]");
			System.exit(1);
		}			
	}

	private int scrape(String firmsNamesFilePath, String firmsIdsFilePath, String txtFilesFolderPath) {
        
        firmNamesList = UrlSearcher.getListFromFile(firmsNamesFilePath);
        firmIdsList = UrlSearcher.getListFromFile(firmsIdsFilePath);
        File resultsFolder = new File(txtFilesFolderPath);
        
        for(int i=0 ; i < firmNamesList.size() ; i++){ 
            String firmName = firmNamesList.get(i);
            String firmId = firmIdsList.get(i);
            logger.info((i+1) + " / " + firmNamesList.size() + " ) " + "I am processing " + firmName + " having ID " + firmId );
            String path = resultsFolder.getPath() + File.separator + firmId + ".txt";
            
            try {
                File file = new File(path);
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write("ID = " + firmId);
                bw.newLine();
                bw.write("Name = " + firmName);
                bw.newLine();
                bw.newLine();
                
                StringBuffer sb = bingSearch(firmName);
                
                Document doc = Jsoup.parse(sb.toString());
                Elements resultLinks = doc.select("li[class=b_algo] h2 a");
                
                for(int j=0 ; j < resultLinks.size() ; j++){ 
                    bw.newLine();
                    bw.newLine();
                    //bw.write(sb.toString());
                    
                    Element element;            
                    element = resultLinks.get(j);
                    bw.write(element.text().toString());
                    bw.newLine();
                    try{
                    	bw.write(URLDecoder.decode(element.attr("href"),"UTF-8"));
                    }
                    catch(IllegalArgumentException iae){
                    	logger.warn(iae.getMessage());
                    	logger.warn("the following url will not be decoded : " + element.attr("href"));
                    	//iae.printStackTrace();
                    	bw.write(element.attr("href"));
                    }
                    bw.newLine();
                    bw.newLine();
                }
                
                bw.flush();
                bw.close();
            }
            catch(IOException e) {
              e.printStackTrace();
            }
        } 
        
        return 0;
    }
        
    private  StringBuffer bingSearch(String searchTerm){
        StringBuffer totalResults = new StringBuffer();
        URL urlObj;
        HttpURLConnection connection;
        String userAgent = "(Mozilla/5.0 (Windows; U; Windows NT 6.0;en-US; rv:1.9.2)" + " Gecko/20100115 Firefox/3.6)";
        //int responseCode;
        
        try {
            //searchTerm = searchTerm.replace(' ','+'); // used before the encoding of the searchTerm in UTF-8
            urlObj = new URL("http://www.bing.com/" + "search?q=" + URLEncoder.encode(searchTerm, "UTF-8"));
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
                        
            //responseCode = connection.getResponseCode();
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8));
            totalResults = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                totalResults.append(inputLine);
            }
            in.close();  
            //logger.info(totalResults);
            
        } catch (IOException ex) {
        	logger.error(ex.getMessage());
            System.err.println(ex.getMessage());
        } finally {
            return totalResults;
        }
        
    }
    
    private void generateUrlCrawlerSeedFile() {
    	// every row in the seed file will have the following structure :
    	// pageUrl	firmId	linkPosition
    	
		Date dateTime = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = formatter.format(dateTime);
        String path = seedFileFolderPath + File.separator + seedFileName + "_" + now + ".txt";
        
		try {
			
			File file = new File(path);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			String txtFilesFolder = txtFilesFolderPath;			
			File folder = new File(txtFilesFolder);
			
		    for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isDirectory()) {
		            // do nothing
		        } else {
		        	logger.info("Analysis of the file " + fileEntry.getName());
		            FileInputStream fis = new FileInputStream(fileEntry);
					InputStream is = fis;
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String strLine;
					String codiceAzienda = ""; // codiceAzienda means firmId
					int codiceLink = 1; // codiceLink means the position of the link in the links list provided by the search engine
					while ((strLine = br.readLine()) != null) {
						codiceAzienda = fileEntry.getName().substring(0, fileEntry.getName().length() - 4);
						if (strLine.startsWith("http")){
							//bw.write(strLine.toLowerCase() + "\t" + "codiceAzienda=" + codiceAzienda + "\t" +"codiceLink=" + codiceLink);
							bw.write(strLine.toLowerCase() + "\t" + codiceAzienda + "\t" + codiceLink);
							bw.newLine();
							codiceLink++;
						}
						
					}					
					is.close();
		        }
		    }
		    
		    bw.close();
		    
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Error: " + e.getMessage());
			System.err.println("Error: " + e.getMessage());
		}
    }
    
    //=====================================================================================================
    //==========		Utility Methods
    //=====================================================================================================
    
    public static boolean isAValidFile(String filePathString) {
		File f = new File(filePathString);
		if(f.exists() && !f.isDirectory()) { 
			return true;
		}
		return false;
	}
    
    public static boolean isAValidDirectory(String dirPathString) {
		File f = new File(dirPathString);
		if(f.exists() && f.isDirectory()) { 
			return true;
		}
		return false;
	}
    
    public static List<String> getListFromFile(String filePath) {
        List<String> orderedList = new ArrayList<String>();
        File file = new File(filePath);
        FileInputStream fis = null;
        try {
                //InputStream is = Main.class.getResourceAsStream(fileConListaNomiAziendePath);
                fis = new FileInputStream(file);
                //BufferedReader br = new BufferedReader(new InputStreamReader(is));
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                        orderedList.add(strLine);
                }
                fis.close();
        } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error: " + e.getMessage());
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
        }
        return orderedList;
    }
    
    private void customizeLog(String fileProperties) throws IOException {
		
		FileInputStream fis = new FileInputStream(fileProperties);
		InputStream inputStream = fis;
		Properties props = new Properties();
		props.load(inputStream);
		
		if(props.getProperty("LOG_FILE_PATH") != null){
			
			logFilePath = props.getProperty("LOG_FILE_PATH");
			
			RollingFileAppender rfa = new RollingFileAppender();
			rfa.setName("FileLogger");
			rfa.setFile(logFilePath);
			rfa.setAppend(true);
			rfa.activateOptions();
			rfa.setMaxFileSize("20MB");
			rfa.setMaxBackupIndex(30);
			rfa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));

			Logger.getRootLogger().addAppender(rfa);
			
		}else{
			logger.error("Wrong/missing configuration for the parameter LOG_FILE_PATH !");
			System.exit(1);
		}
			
		inputStream.close();
		fis.close();
		
	}
}