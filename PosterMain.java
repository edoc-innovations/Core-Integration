package eDOCPoster;

import java.util.Arrays;

public class PosterMain {

	public static void main(String[] args) throws Throwable {		
		Poster newPoster = new Poster("CUPROD");
		String user = "test@cuprod";
		
		System.out.println("POSTER created with the following parameters:");
		System.out.println("ControlID: "+newPoster.controlID);
		System.out.println("isapiURL: "+newPoster.isapiURL);
		System.out.println("eDOCSigURL: "+newPoster.eDOCSigURL);
		//System.out.println("handoffKey: "+newPoster.encryptionKey);
		System.out.println("sourceTable: "+newPoster.sourceTable);
		System.out.println("sourceFields: "+Arrays.toString(newPoster.sourceFields));
		
		String[] tableIDsList = new String[]{"Images"};
		String[][] criteriaList = new String[2][];
		String[] fieldsList = new String[]{};		
		criteriaList[0] = new String[]{"Last_Name","Does"};
		criteriaList[1] = new String[]{"First_Name","John"};
		
		String pdfFileName2 = "C:\\temp\\MEMBERSHIP APPLICATION-eff 3-24-15.pdf";
		String pdfFileName = "C:\\temp\\Debt Protection Security Contract.pdf";
		String pdfName = "MEMBERSHIP APPLICATION CU Prodigy";
		//String pdfName = "Debt Protection 12-20";
		String[][] dataMap = new String[0][];
		
		String pckName = "New Package";		
		String[] templateList = new String[]{"Test Template 1","Test Template 2"};		
		String[][] templateData1 = new String[1][];
		templateData1[0] = new String[]{"Name" , "John Smith"};
		String[][] templateData2 = new String[2][];
		templateData2[0] = new String[]{"FirstName" , "Johnny"};
		templateData2[1] = new String[]{"LastName" , "Appleseed"};
		String[][][] templateData = new String[2][][];
		templateData[0] = templateData1;
		templateData[1] = templateData2;
		
		String[][] updateData = new String[2][];
		updateData[0] = new String[]{"Form","Form1"};
		updateData[1] = new String[]{"Amount","100"};
		
		//HANDOFF
		System.out.println(newPoster.doHandoff(user));
		
		//FORM SEARCH		
		//System.out.println(newPoster.getForms(user, tableIDsList, criteriaList, fieldsList));
		
		//DOCUMENT RETRIEVAL		
		//System.out.println(newPoster.getForm(user, "", "", "C:\\Work\\Java\\eDOC\\temp\\"));
		
		//CREATE TEMPLATE
		//newPoster.openTemplateInBrowser(newPoster.importTemplate(user, pdfFileName, pdfName, dataMap));
		
		//CREATE PACKAGE FROM TEMPLATES		
		//newPoster.openPackageInBrowser(newPoster.createPackageFromTemplates(user, pckName, templateList, templateData));
		
		//GET AP FORMS IN SOURCE TABLE	
		//System.out.println(newPoster.getAPTableForms(user));
		
		//GET AP FORM
		//System.out.println(newPoster.getAPForm(user, ""));
		
		//UPDATE AP FORM
		//System.out.println(newPoster.updateAPForm(user, "", updateData));

	}

}
