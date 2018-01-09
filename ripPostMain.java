package eDOCPoster;

import java.util.Arrays;

public class ripPostMain {

	public static void main(String[] args) throws Throwable {
		//Poster newPoster = new Poster("Reed");
		//String user = "tkiel";
		
		//Poster newPoster = new Poster("CUPROD");
		//String user = "test@cuprod";
		
		Poster newPoster = new Poster("Reed","C:\\Users\\tkiel\\Documents\\edoc.properties");
		String user = "TKIEL";
		
		System.out.println("POSTER created with the following parameters:");
		System.out.println("ControlID: "+newPoster.controlID);
		System.out.println("isapiURL: "+newPoster.isapiURL);
		System.out.println("eDOCSigURL: "+newPoster.eDOCSigURL);
		System.out.println("sourceTable: "+newPoster.sourceTable);
		System.out.println("sourceFields: "+Arrays.toString(newPoster.sourceFields));
		
		String retJSON;
		
		//HANDOFF
		/*
		retJSON = newPoster.doHandoff(user);
		System.out.println(retJSON);
		*/
		
		//CUP-12
		/*
		String[] tableIDsList = new String[]{"Images"};
		String[][] criteriaList = new String[1][];
		String[] fieldsList = new String[]{};		
		criteriaList[0] = new String[]{"Last_Name","Does"};
		
		retJSON = newPoster.getForms(user, tableIDsList, criteriaList, fieldsList);
		System.out.println(retJSON);
		*/
		
		//CUP-15
		/*
		String pdfFileName2 = "C:\\temp\\MEMBERSHIP APPLICATION-eff 3-24-15.pdf";
		String pdfFileName = "C:\\temp\\Debt Protection Security Contract.pdf";
		String pdfName = "MEMBERSHIP APPLICATION CU Prodigy";
		//String pdfName = "Debt Protection 12-20";
		String[][] dataMap = new String[0][];
		
		retJSON = newPoster.importTemplate(user, pdfFileName, pdfName, dataMap);
		newPoster.openTemplateInBrowser(retJSON);
		*/
		
		//CUP-16
		/*
		String pckName = "New Package";		
		String[] templateList = new String[]{"Sample Template 1","Sample Template 2"};		
		
		String[][] templateData1 = new String[2][];
		templateData1[0] = new String[]{"FirstName" , "John"};
		templateData1[1] = new String[]{"LastName" , "Smith"};
		
		String[][] templateData2 = new String[3][];
		templateData2[0] = new String[]{"Firstname" , "Johnny"};
		templateData2[1] = new String[]{"LastName" , "Appleseed"};
		templateData2[2] = new String[]{"Email" , "sample@email.com"};
		
		String[][][] templateData = new String[2][][];
		templateData[0] = templateData1;
		templateData[1] = templateData2;
		
		retJSON = newPoster.createPackageFromTemplates(user, pckName, templateList, templateData);
		newPoster.openPackageInBrowser(retJSON);
		*/
		
		//CUP-17
		/*
		System.out.println(newPoster.getForm(user, "E15724CBA79548A39BE3F46EBEFBD9B0", "", "C:\\Work\\Java\\eDOC\\temp\\"));
		*/
		
		//CUP-18
		/*
		System.out.println(newPoster.getSourceTableForms(user));
		System.out.println(newPoster.getSourceForm(user, "448E46F1400B4D0BBF322D90C9AA37C3"));	
		*/
		
	}

}
