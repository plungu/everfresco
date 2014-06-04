package com.support.transformation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

//import org.apache.log4j.Logger;

public class PDFTransformer {
	
	//private Logger log = Logger.getLogger(this.getClass().getName());
	private String pdfDocument;
	
	public PDFTransformer() {
		
	}

	

	public void setDocument(String pdfDocument) {
		System.out.println("Setting PDFTransformer.pdfDocument to file: " + pdfDocument);
		this.pdfDocument = pdfDocument;
	}



	public String getText() throws FileNotFoundException, IOException {
		String text = "";
		PDFParser parser = new PDFParser(new FileInputStream(this.pdfDocument));
		parser.parse();
		
		COSDocument doc = parser.getDocument();
		PDDocument pDoc = new PDDocument(doc);
		PDFTextStripper stripper = new PDFTextStripper();
		text = stripper.getText(pDoc);
		
		return text;
	}
	
	
	
}
