package com.support.transformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFTransformer {
	
	private String title;
	private final String doctypeCruft = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n\"http://www.w3.org/TR/html4/loose.dtd\">";
	private FileInputStream content;
	
	public PDFTransformer() {
		
	}


	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() throws FileNotFoundException, IOException {
		String text = "";
		PDFParser parser = new PDFParser(this.content);
		parser.parse();
		
		COSDocument doc = parser.getDocument();
		PDDocument pDoc = new PDDocument(doc);
		PDFTextStripper stripper = new PDFText2HTML(this.title);
		text = stripper.getText(pDoc).replace(this.doctypeCruft, "");
		text = text.split("<body>")[1].replace("</body>", "").replace("</html>", "");
		text = text.replace("  "," ").replace("<p> <p>","<p>");
		
		return text;
	}

	public void setContent(File file) throws FileNotFoundException {
		
		this.content = new FileInputStream(file);
		
	}
	
	
	
}
