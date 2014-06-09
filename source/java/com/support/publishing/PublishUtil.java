package com.support.publishing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import com.evernote.edam.type.Data;

public final class PublishUtil {

    /**
     * Helper method to read the contents of a file on disk and create a new Data object.
     */
    public static Data readFileAsData(File file) throws Exception {

      // Read the full binary contents of the file
      FileInputStream in = new FileInputStream(file);
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      byte[] block = new byte[10240];
      int len;
      
      while ((len = in.read(block)) >= 0) {
    	  byteOut.write(block, 0, len);
      }
      
      in.close();
      byte[] body = byteOut.toByteArray();
      
      // Create a new Data object to contain the file contents
      Data data = new Data();
      data.setSize(body.length);
      data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
      data.setBody(body);
      
      return data;
    }

    /**
     * Helper method to convert a byte array to a hexadecimal string.
     */
    public static String bytesToHex(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      
      for (byte hashByte : bytes) {
    	  
    	  int intVal = 0xff & hashByte;
    	  if (intVal < 0x10) {
    		  sb.append('0');
    	  }
    	  sb.append(Integer.toHexString(intVal));
    	  
      }
      
      return sb.toString();
    }
    
    /**
     * 
     */
    public static String wrapContent(String content)
    {
    	String enmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
	            + "<en-note>" 
	            + content
	            + "</en-note>";
		return enmlContent;
    }
    
    public static String wrapContent(String title, String description, String mimeType, String hashHex )
    {
    	String enmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>" 
                + "<span style=\"color:green;\">" +title+" : " +description+ "</span><br/>"
                + "<en-media type=\"" +mimeType+ "\" hash=\"" + hashHex + "\"/>"
                + "</en-note>";
            return enmlContent;
    }
}
