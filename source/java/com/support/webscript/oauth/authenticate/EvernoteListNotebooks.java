package com.support.webscript.oauth.authenticate;

import java.util.*;
import java.io.IOException;
import java.net.*;

import javax.servlet.http.HttpSession;

import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.evernote.edam.type.*;
import com.evernote.edam.notestore.*;
import com.evernote.client.oauth.*;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.*;
import org.scribe.model.*;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;

public class EvernoteListNotebooks extends AbstractWebScript {

	Logger log = Logger.getLogger(this.getClass()); 

	static final String SESSION_ACCESS_TOKEN = "accessToken";
	static final String SESSION_NOTE_STORE_URL = "noteStoreUrl";

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) 
	{
		  log.info("****** Inside the Evernote List Webscript ******");
		  
		  WebScriptSession session = req.getRuntime().getSession();
		  String accessToken = (String)session.getValue(SESSION_ACCESS_TOKEN);
		  String noteStoreUrl = (String)session.getValue(SESSION_NOTE_STORE_URL);
		  
		  log.info("****** accesstoken: "+accessToken);
		  log.info("****** noteStoreUrl: "+noteStoreUrl);
		  
		  if (accessToken == null || noteStoreUrl == null) {
		
			  log.error("Evernote Access Token is not in session");
			  throw new WebScriptException("Evernote Access Token is not in session");
		
		  } else {
			  
		      try {
		  
		            log.info("****** Listing notebooks from: " + noteStoreUrl);
		            THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
		            TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
		            NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
		            List<?> notebooks = noteStore.listNotebooks(accessToken);
	            	// build a json object
	    	    	JSONObject obj = new JSONObject();
	    	    	// put some data on it
		            for (Object notebook : notebooks) {
		            	log.info("Notebook: " + ((Notebook)notebook).getName());
		    	    	obj.put(((Notebook)notebook).getGuid(), ((Notebook)notebook).getName());
		            }

	    	    	// build a JSON string and send it back
	    	    	String jsonString = obj.toString();
	    	    	res.getWriter().write(jsonString);
		            
		      } catch (Exception e) {
		        e.printStackTrace();
		        throw new WebScriptException("Could not authenticate Evernote");
		      }

		  }
		  
	  }
}	  