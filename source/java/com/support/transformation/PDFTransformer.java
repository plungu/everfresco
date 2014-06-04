package com.support.transformation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

//import org.apache.log4j.Logger;

public class PDFTransformer {
	
	//private Logger log = Logger.getLogger(this.getClass().getName());
	private String pdfDocument;
	private String title;
	private final String doctypeCruft = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n\"http://www.w3.org/TR/html4/loose.dtd\">";
	
	public PDFTransformer() {
		
	}

	public void setDocument(String pdfDocument) {
		this.pdfDocument = pdfDocument;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() throws FileNotFoundException, IOException {
		String text = "";
		PDFParser parser = new PDFParser(new FileInputStream(this.pdfDocument));
		parser.parse();
		
		COSDocument doc = parser.getDocument();
		PDDocument pDoc = new PDDocument(doc);
		PDFTextStripper stripper = new PDFText2HTML(this.title);
		text = stripper.getText(pDoc).replace(this.doctypeCruft, "");
		text = text.split("<body>")[1].replace("</body>", "").replace("</html>", "");
		text = text.replace("  "," ").replace("<p> <p>","<p>");
		
		return text;
	}
	
	
	
}
