package com.support.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.support.transformation.PDFTransformer;

public class PDFTransformerTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
	
		PDFTransformer pdfXform = new PDFTransformer();
		String pdfDocument = "/home/support/Downloads/Supported Platforms for Alfresco Enterprise 4.2.x.pdf";
		pdfXform.setDocument(pdfDocument);
		String text = pdfXform.getText();
		System.out.println(text);
	}

}
