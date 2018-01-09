package eDOCPoster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class Poster {
	
	public static String controlID;
	public String isapiURL;
	public String eDOCSigURL;
	public String encryptionKey;
	public String sourceTable;
	public String[] sourceFields;
	
	private String settingsKeyString = "AAAAAAAAAABBBBBBBBBBCCCCCCCCCC";
	
	/**
	 * Constructor - All parameters defined
	 *
	 * @param String cid - Control ID  
	 * @param String ripURL - RIP Isapi URL
	 * @param String edsURL - eDOCSignature URL
	 * @param String eKey - Encryption Key
	 * @param String sTable - Source Table Name
	 * @param String sFields - Source Fields 
	 * 
	 */
	public Poster (String cid, String ripURL, String edsURL, String eKey, String sTable, String[] sFields) {
		controlID = cid;
		isapiURL = ripURL;
		eDOCSigURL = edsURL;
		encryptionKey = eKey;
		sourceTable = sTable;
		sourceFields = sFields;
	}
	
	/**
	 * Constructor - Control ID and properties file defined.
	 * Uses controlID to find and pull parameter information from properties file
	 *
	 * @param String cid - Control ID  
	 * @param String Filepath for properties file
	 * @throws Throwable 
	 * 
	 */
	public Poster(String cid, String propFilePath) throws Throwable {
		controlID = cid;
		File propFile = new File(propFilePath);
		if(propFile.exists() && !propFile.isDirectory()) { 
			loadFromFilePath(propFilePath, controlID);
		}else{
			throw new java.lang.Error("Error during Poster construction: specified properties file does not exist"); 
		}
	}
	
	/**
	 * Constructor - Control ID only.
	 * Will look in application path for an edoc.properties file and attempt to load settings
	 * Throws error if file does not exist 
	 *
	 * @param String cid - Control ID  
	 * @throws Throwable 
	 * 
	 */
	public Poster(String cid) throws Throwable {
		controlID = cid;
		String propFilePath = System.getProperty("user.dir")+"\\edoc.properties"; 
		File propFile = new File(propFilePath);
		if(propFile.exists() && !propFile.isDirectory()) { 
			loadFromFilePath(propFilePath, controlID);
		}else{
			throw new java.lang.Error("Error during Poster construction: could not locate properties file in execution directory. It is recommended that you call constructor Poster(String cid, String propFilePath) or (String cid, String arcURL, String edsURL, String hKey)."); 
		}
	}
		
	/**
	 * Call to perform a simple hand off to the edocsignature system. 
	 * Use this to check to make sure connection is up and working.
	 * Defined in the eDOCSignature API in the /SESSIONS section and Appendix A
	 *
	 * @param String user - eDOCSignature user	 *   
	 * 
	 */	
	public String doHandoff(String user) throws Exception {
		String responseString = null;
		System.out.println("Attempting handoff...");
        HttpClient httpclient = HttpClientBuilder.create().build();
        //Set up a SESSIONS request
        HttpPost request = new HttpPost(isapiURL+"/SESSIONS/");
        JSONObject result = new JSONObject();
        try {
        	if (user.isEmpty()) {
        		throw new java.lang.Error("Error: user cannot be empty");
        	}
        	String handoff = BuildEncryptedHandoffString(user);
        	String host = Inet4Address.getLocalHost().getHostAddress();
        	//String host = getExternalIP();
        	//build the hand off string
        	String bodyContent = "{\"action\":\"handoff\",\"controlid\":\""+controlID+"\",\"host\":\""+host+"\",\"handoff\":\""+handoff+"\"}";
        	System.out.println(bodyContent);
        	StringEntity se = new StringEntity(bodyContent);
	        request.addHeader("content-type", "application/json");
	        request.setEntity(se);
	        HttpResponse response = httpclient.execute(request);
	        HttpEntity rentity = response.getEntity();
        	responseString = EntityUtils.toString(rentity);    
        	} catch (ClientProtocolException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} catch (IOException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} finally {
        		request.releaseConnection();
        	}
        return responseString;
	}
	
	/**
	 * Call to complete an import template call to the edocsignature system.
	 * Defined in the /TEMPLATES section with the IMPORTTEMPLATES command.
	 * 
	 * @param String user - eDOCSignature user
	 * @param String pdfFileName - File location of pdf file to upload
	 * @param String pdfName - Name of template
	 * @param String dataMap - Map of data from CU*Prodigy system. Should be formatted as an array of string arrays of ["CU*P field","eDOC field"]	    
	 * 
	 */	
	public String importTemplate(String user, String pdfFilePath, String pdfName, String[][] dataMap) throws Exception {
		System.out.println("Attempting template import...");
		String responseString = null;
        HttpClient httpclient = HttpClientBuilder.create().build();
        //Set up a SESSIONS request
        HttpPost request = new HttpPost(isapiURL+"/TEMPLATES/");
        JSONObject result = new JSONObject();
        try {
        	if (user.isEmpty()) {
        		throw new java.lang.Error("Error: user cannot be empty");
        	}
        	//initialize file from file path
        	File file = new File(pdfFilePath);
        	//build handoff string, fetch local machine ip, convert map data
        	String handoff = BuildEncryptedHandoffString(user);
        	String host = Inet4Address.getLocalHost().getHostAddress();
        	//String host = getExternalIP();
        	String mapData = buildMapData(dataMap);
        	//build the JSON body
        	String bodyContent = "{\"action\":\"IMPORT\","
        						+ "\"controlid\":\""+controlID+"\","
        						+ "\"host\":\""+host+"\","
        						+ "\"handoff\":\""+handoff+"\","
        						+ "\"user\":\""+user+"\","
        						+ "\"formname\":\""+pdfName+"\","
        						+ "\"mapdata\":[\""+mapData+"\"],"
        						+ "\"files\":[\""+file.getName()+"\"]}";
        	System.out.println(bodyContent);
            //FileBody fileBody = new FileBody(file, ContentType.APPLICATION_OCTET_STREAM);
            StringBody stringBody1 = new StringBody(bodyContent, ContentType.APPLICATION_JSON);
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            builder.addPart("JSON", stringBody1);
            HttpEntity entity = builder.build();
            
            request.setEntity(entity);
            HttpResponse response = httpclient.execute(request);
            HttpEntity rentity = response.getEntity();
            responseString = EntityUtils.toString(rentity);     
            System.out.println(responseString);
        	} catch (ClientProtocolException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} catch (IOException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} finally {
        		request.releaseConnection();
        	}
        return responseString;  
	}
	
	/**
	 * Call to create a package from a set of templates and template data.
	 * Defined in the /TEMPLATES section with the CREATEFROMTEMPLATES command.
	 * 
	 * @param String user - eDOCSignature user
	 * @param String packageName - Name of package to create
	 * @param String[] templateList - Array of templates to build into a package
	 * @param String[][][] templateData - Mapping of data. Built as an array of pairs organized by template.	    
	 * 
	 */	
	public String createPackageFromTemplates(String user, String packageName, String[] templateList, String[][][] templateData) throws Exception {
        String responseString= "";
		System.out.println("Attempting creating package from templates...");
        HttpClient httpclient = HttpClientBuilder.create().build();
        //Set up a SESSIONS request
        HttpPost request = new HttpPost(isapiURL+"/PACKAGES/");
        JSONObject result = new JSONObject();
        try {
        	if (user.isEmpty()) {
        		throw new java.lang.Error("Error: user cannot be empty");
        	}
        	String handoff = BuildEncryptedHandoffString(user);
        	//String host = getExternalIP();
        	String host = InetAddress.getLocalHost().getHostAddress().toString();
        	String templatesString = buildTemplatesString(templateList, templateData);
        	//build the hand off string
        	String bodyContent = "{\"action\":\"CREATEFROMTEMPLATES\","
        						+ "\"controlid\":\""+controlID+"\","			
        						+ "\"handoff\":\""+handoff+"\","
        						+ "\"host\":\""+host+"\","
        						+ "\"name\":\""+packageName+"\","
        						+ "\"user\":\""+user+"\","
        						+ "\"templates\":["+templatesString+"]}";
        	System.out.println(bodyContent);
        	StringEntity se = new StringEntity(bodyContent);
	        request.addHeader("content-type", "application/json");
	        request.setEntity(se);
	        HttpResponse response = httpclient.execute(request);
	        HttpEntity rentity = response.getEntity();
        	responseString = EntityUtils.toString(rentity);
        	System.out.println();    
        	} catch (ClientProtocolException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} catch (IOException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} finally {
        		request.releaseConnection();
        	}
		return responseString;
	}
	
	/**
	 * Call to get and download a form in the system by tableID and docID
	 * Defined in the /DOCUMENTS/FORMS section with the GETFORM command.
	 * 
	 * @param String user - eDOCSignature user.
	 * @param String docID - ID of doc to retrieve.
	 * @param String tableID - Table to look to. If blank will look into all tables until docID is found.
	 * @param String filePath - Path to download file. Can be either a directory or file path. Will use docID as file name if a directory is given.	    
	 * 
	 */	
	public String getForm(String user, String docID, String tableID, String filePath) throws Exception {
		File outFile = new File(filePath);
		System.out.println("Attempting document retrieval...");
        HttpClient httpclient = HttpClientBuilder.create().build();
        //Set up a DOCUMENTS/FORMS request
        HttpPost request = new HttpPost(isapiURL+"/DOCUMENTS/FORMS/");
        JSONObject result = new JSONObject();
        try {
        	if (user.isEmpty()) {
        		throw new java.lang.Error("Error: user cannot be empty");
        	}
        	String handoff = BuildEncryptedHandoffString(user);
        	String host = Inet4Address.getLocalHost().getHostAddress();
        	//String host = getExternalIP();

        	//build the hand off string
        	String bodyContent = "{\"action\":\"GETFORM\","
        						+ "\"controlid\":\""+controlID+"\","			
        						+ "\"handoff\":\""+handoff+"\","
        						+ "\"host\":\""+host+"\","
        						+ "\"docid\":\""+docID+"\","
        						+ "\"table\":\""+tableID+"\","
        						+ "\"outputtype\":\"PDF\"}";
        	System.out.println(bodyContent);
        	StringEntity se = new StringEntity(bodyContent);
	        request.addHeader("content-type", "application/json");
	        request.setEntity(se);
	        HttpResponse response = httpclient.execute(request);
	        HttpEntity rentity = response.getEntity();
	        
	        BufferedHttpEntity buf = new BufferedHttpEntity(rentity);
	        //ensure file path is valid - if not, append the filename and extension        	
	        if (outFile.exists() == true){
	        	if (outFile.isDirectory()){
	        		if(filePath.charAt(filePath.length()-1)!=File.separatorChar){
	    	        	filePath += File.separator;
	    	        }
	        		outFile = new File(filePath+docID+".pdf");
	            	FileOutputStream os = new FileOutputStream(outFile);
	            	buf.writeTo(os);
	            	while (buf.isStreaming()){
	            		buf.writeTo(os);
	            	}
	        	}
	        } else {
	        	throw new Exception("Invalid or missing output filepath!"); 
	        }
        	
        	} catch (ClientProtocolException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} catch (IOException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} finally {
        		request.releaseConnection();
        	}
		return outFile.getAbsolutePath();
	}
	
	/**
	 * Call to get a list of forms in the system that satisfy a given criteria
	 * Defined in the /DOCUMENTS/FORMS section with the GETFORMS command.
	 * 
	 * @param String user - eDOCSignature user.
	 * @param String[] tableIDsList - List of tables to look into. If blank, will search all tables.
	 * @param String[][] criteriaList - List of criteria. Formatted as a set of arrays of pairs in the format ["Form","FormType"] or ["Created_ON","<1970"].
	 * @param String[] fieldsList - List of fields to return. If left blank will return the system minimum fields. 	    
	 * 
	 */
	public String getForms(String user, String[] tableIDsList, String[][] criteriaList, String[] fieldsList) throws Exception {
		System.out.println("Getting the form list...");
		String responseString = "";
        HttpClient httpclient = HttpClientBuilder.create().build();
        //Set up a SESSIONS request
        HttpPost request = new HttpPost(isapiURL+"/DOCUMENTS/FORMS/");
        JSONObject result = new JSONObject();
        try {
        	if (user.isEmpty()) {
        		throw new java.lang.Error("Error: user cannot be empty");
        	}
        	//build the handoff string
        	String handoff = BuildEncryptedHandoffString(user);
        	String host = Inet4Address.getLocalHost().getHostAddress();
        	//String host = getExternalIP();
        	String tableIDs = buildListString(tableIDsList);
        	String criteria = buildNameValueListString(criteriaList);
        	String fields = buildListString(fieldsList);
        	// table ids cannot be empty
        	if (tableIDs.isEmpty()) {
        		throw new java.lang.Error("Error: table ids list cannot be empty"); 	
        	}
        	//build the JSON body
        	String bodyContent = "{\"action\":\"GETFORMS\","
        						+ "\"controlid\":\""+controlID+"\","
        						+ "\"host\":\""+host+"\","
        						+ "\"handoff\":\""+handoff+"\","
        						+ "\"user\":\""+user+"\",";
        	if (criteria.isEmpty()) {
        		if (fields.isEmpty()) {
        			bodyContent = bodyContent + "\"tableids\":["+tableIDs+"]";
        		} else {
        			bodyContent = bodyContent + "\"tableids\":["+tableIDs+"],";
        			bodyContent = bodyContent + "\"fields\":["+fields+"]";
        		}
        	} else {
        		bodyContent = bodyContent + "\"tableids\":["+tableIDs+"],";
        		if (fields.isEmpty()) {
        			bodyContent = bodyContent + "\"criteria\":["+criteria+"]";
        		} else {
        			
        			bodyContent = bodyContent + "\"criteria\":["+criteria+"],";
        			bodyContent = bodyContent + "\"fields\":["+fields+"]";
        		}        		
        	}
        	bodyContent = bodyContent + "}";
        	System.out.println(bodyContent);			
        	
        	StringEntity se = new StringEntity(bodyContent);
	        request.addHeader("content-type", "application/json");
	        request.setEntity(se);
	        HttpResponse response = httpclient.execute(request);
	        HttpEntity rentity = response.getEntity();
        	responseString = EntityUtils.toString(rentity);
        	
        	} catch (ClientProtocolException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} catch (IOException e) {
        		result.put("status", "500");
        		result.put("bodyContent", "");
        		e.printStackTrace();
        	} finally {
        		request.releaseConnection();
        	}
        return responseString;
	}
	
	/**
	 * Call to get a list of forms in the source table as defined during the creation of the poster object.
	 * Use this definition if the source table is defined in the properties file. 
	 * 
	 * @param String user - eDOCSignature user.	    
	 * 
	 */
	public String getSourceTableForms(String user)throws Exception{
		if (sourceTable.isEmpty()){
			throw new java.lang.Error("No source table found"); 
		}
		return getSourceTableForms(user, sourceTable);		
	}
	
	/**
	 * Call to get a list of forms in the source table as defined during the creation of the poster object.
	 * Use this definition if source table is undefined in the properties file.
	 * 
	 * @param String user - eDOCSignature user.	
	 * @param String table - Source table to search. 	    
	 * 
	 */
	public String getSourceTableForms(String user, String table) throws Exception{
		String[] tableList = new String[]{table};
		String[][] criteriaList = new String[1][];
		criteriaList[0] = new String[]{"Imported","<1970"};
		String[] fieldsList = new String[]{};
		return getForms(user, tableList, criteriaList, fieldsList);		
	}
	
	/**
	 * Call to get a specified form from the source table.
	 * Use this definition if the source table is defined in the properties file.
	 * 
	 * @param String user - eDOCSignature user.	
	 * @param String docID - Document to retrieve. 	    
	 * 
	 */
	public String getSourceForm(String user, String docID) throws Exception{
		if (sourceTable.isEmpty()){
			throw new java.lang.Error("No source table found"); 
		}
		return getSourceForm(user, sourceTable, docID, sourceFields);
	}
	
	/**
	 * Call to get a specified form from the source table.
	 * Use this definition if the source table is undefined in the properties file.
	 * 
	 * @param String user - eDOCSignature user.	
	 * @param String table - Source table to search. 	    
	 * @param String docID - Document to retrieve. 
	 * 
	 */
	public String getSourceForm(String user, String table, String docID) throws Exception{
		if (sourceTable.isEmpty() && table.isEmpty()){
			throw new java.lang.Error("No source table found"); 
		}
		return getSourceForm(user, table, docID, sourceFields);
	}
	
	/**
	 * Call to get a specified form from the source table.
	 * Use this definition if the source table and the list of fields are undefined in the properties file
	 * 
	 * @param String user - eDOCSignature user.	
	 * @param String table - Source table to search. 	    
	 * @param String docID - Document to retrieve.
	 * @param String[] fieldsList - List of fields to return.  
	 * 
	 */
	public String getSourceForm(String user, String importTable, String docID, String[] fieldsList) throws Exception{
		String[] tableList = new String[]{importTable};
		String[][] criteriaList = new String[1][];
		criteriaList[0] = new String[]{"_Doc_ID",docID};
		return getForms(user, tableList, criteriaList, fieldsList);
	}	
	
	/*
	 * Helper function to build an encrypted string for a handoff
	 */	
	private String BuildEncryptedHandoffString(String user) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy/HH/mm"); 
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));  
		String timeStamp = sdf.format(new Date());
		String handoffString = "TIMESTAMP="+timeStamp+"&USER="+user;
		String keyStr = encryptionKey;
		
		String encrypted = null;
		try {
			encrypted = TripleDES.encrypt(keyStr,"RANDOM", handoffString, "DESede/CBC/PKCS5Padding");
		} catch (Throwable e) {
			e.printStackTrace();
		}
        //String decrypted=TripleDESEncryptor.decrypt(encrypted);
		return encrypted;
	}
	
	/*
	 * Helper function that loads settings from a properties file based on a Control ID
	 */	
	private void loadFromFilePath(String filePath, String cid) throws Throwable{
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(filePath);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			isapiURL = prop.getProperty(cid+".isapiURL");
			encryptionKey = TripleDES.decrypt(settingsKeyString, prop.getProperty(cid+".encryptionKey"));
			eDOCSigURL = prop.getProperty(cid+".edocSignatureURL");
			sourceTable = prop.getProperty(cid+".sourceTable");
			String sourceFieldsStr = prop.getProperty(cid+".sourceFields");
			sourceFields = sourceFieldsStr.split(",");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	/*
	 * Helper function that builds a string out of an array of data mappings
	 */	
	private String buildMapData(String[][] dataMap){
		String mapData="";
		if ((dataMap.length)==0){

		}else{
			mapData = "{";
			for (int i = 0; i < dataMap.length; i++){
				String[] curMap = dataMap[i];			
				if (i == dataMap.length-1){
					mapData = mapData+"\""+curMap[0]+"\":\""+curMap[1]+"\"";
				}else{
					mapData = mapData+"\""+curMap[0]+"\":\""+curMap[1]+"\",";
				}						
			}
			mapData = mapData + "}";
		}
		return mapData;		
	}
	
	/*
	 * Helper function that builds a string out of a list of templates and the corresponding list of template data
	 */	
	public String buildTemplatesString(String[] templateList, String[][][] templateData) throws Exception {
		String templateString = "{";		
		if(templateList.length == templateData.length){			
			int numTemplates = templateData.length;
			for (int i=0; i < numTemplates; i++){
				String tempName = templateList[i];
				templateString = templateString+ "\"template\":\""+tempName+"\",\"data\":[{";
				String[][] setOfPairs = templateData[i];
				int numPairs = setOfPairs.length;
				for (int j=0; j < numPairs; j++){
					String[] pair = setOfPairs[j];
					if (j == numPairs-1){
						templateString = templateString+"\""+pair[0]+"\":\""+pair[1]+"\""; 
					}else{
						templateString = templateString+"\""+pair[0]+"\":\""+pair[1]+"\","; 
					}


				}
				if (i == templateList.length-1){
					templateString = templateString + "}]}"; 
				}else{
					templateString = templateString + "}]},{"; 
				}
									
			}
		}else{ //if size of lists do not match, do not attempt to continue
			throw new java.lang.Error("Error: Size of templateList and size of templateData do not match"); 
		}
		//templateString = templateString+ "}";
		return templateString;
	}
	
	/*
	 * Helper function that builds a string out of a list of values (String)
	 */	
	private String buildListString(String[] aList) throws Exception {
		//String ListString = "[";
		String ListString = "";
		for (int i = 0; i < aList.length; i++){
			String tempName = aList[i];
			if (i == aList.length-1){
				ListString = ListString + "\""+tempName+"\""; 
			}
			else {
				ListString = ListString + "\""+tempName+"\",";
			}
		}
		//ListString = ListString + "]";
		return ListString;
	}

	/*
	 * Helper function that builds a string out of a list of name/value pairs (String)
	 */	
	private String buildNameValueListString(String[][] aNameValueList) throws Exception {
		//String NameValueString = "[";
		String NameValueString = "";
		for (int i = 0; i < aNameValueList.length; i++){
			String[] tempData = aNameValueList[i];
			if (i == aNameValueList.length-1){
				NameValueString = NameValueString+"{\""+tempData[0]+"\":\""+tempData[1]+"\"}"; 
			}
			else {
				NameValueString = NameValueString+"{\""+tempData[0]+"\":\""+tempData[1]+"\"},";
			}
		}		
		//NameValueString = NameValueString + "]";
		return NameValueString;
	}
	
	/*
	 * Helper function that retrieves the external IP address of the host machine from the amazon API.
	 * Can be updated with different URLs if the Amazon services goes offline or fails
	 */	
	private String getExternalIP() throws IOException{
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(
		                whatismyip.openStream()));

		String ip = in.readLine(); //you get the IP as a String
		return ip;
	}
	
	/*
	 * Helper function that parses a successful template creation response string and opens the created 
	 * template in browser at the given eDOCSignature site.
	 */	
	public void openTemplateInBrowser(String jsonString) throws IOException{
		String isSuccess = jsonString.substring(jsonString.indexOf("\"result\"")+9, jsonString.indexOf("\"result\"")+10);
		if(isSuccess.equals("t")){
			String session = jsonString.substring(jsonString.indexOf("\"session\"")+11, jsonString.length()-2);
			String id = jsonString.substring(jsonString.indexOf("\"id\"")+6, jsonString.indexOf("\"session\"")-2);
	
			String urlString = eDOCSigURL+"managetemplate.php?TID="+id+"&SID="+session+"&CID="+controlID;
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(urlString));
		}
	}
	
	/*
	 * Helper function that parses a successful package creation response string and opens the created 
	 * template in browser at the given eDOCSignature site.
	 */	
	public void openPackageInBrowser(String jsonString) throws IOException{
		String isSuccess = jsonString.substring(jsonString.indexOf("\"result\"")+9, jsonString.indexOf("\"result\"")+10);
		if(isSuccess.equals("t")){
			String session = jsonString.substring(jsonString.indexOf("\"session\"")+11,jsonString.indexOf("\"session\"")+43);
			String id = jsonString.substring(jsonString.indexOf("\"id\"")+6, jsonString.indexOf("\"id\"")+38);

			String urlString = eDOCSigURL+"senddoc.php?LoadPkgTemp="+id+"&SID="+session+"&CID="+controlID;
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(urlString));			
		}
	}

}
