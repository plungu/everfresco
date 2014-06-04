package com.support.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import com.support.transformation.PDFTransformer;

public class PDFTransformerTest {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParserConfigurationException {
	
		String pdfDocument = "/home/support/Downloads/Supported Platforms for Alfresco Enterprise 4.2.x.pdf";
		
		
		PDFTransformer pdfXform = new PDFTransformer();
		
		pdfXform.setDocument(pdfDocument);
		pdfXform.setTitle("Supported Platforms for Alfresco Enterprise 4.2.x");
		String text = pdfXform.getText();
		System.out.println(text);
		
		
		/*
		PDDocument pdDoc = PDDocument.load(pdfDocument);
		PDFDomTree parser = new PDFDomTree();
		
		parser.processDocument(pdDoc);
		Document dom = parser.getDocument();
		System.out.println(dom.getTextContent());
		*/
	}

}
