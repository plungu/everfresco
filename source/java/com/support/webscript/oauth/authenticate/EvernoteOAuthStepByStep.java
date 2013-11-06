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
import org.alfresco.service.cmr.oauth2.*;

public class EvernoteOAuthStepByStep extends DeclarativeWebScript {

	Logger log = Logger.getLogger(this.getClass()); 

	//TODO: MAKE MSG PROPERTIES with spring config
	/*
	 * Fill in your Evernote API key. To get an API key, go to
	 * http://dev.evernote.com/documentation/cloud/
	 */
	static final String consumerKey = "plungu";
	static final String consumerSecret = "f3716cbcf05aae1b";
  
	/*
	 * Replace this value with https://www.evernote.com to switch from the Evernote
	 * sandbox server to the Evernote production server.
	 */
	static final String urlBase = "https://sandbox.evernote.com";	  
	static final String requestTokenUrl = urlBase + "/oauth";
	static final String accessTokenUrl = urlBase + "/oauth";
	static final String authorizationUrlBase = urlBase + "/OAuth.action";
	//Change this to use alfresco server port
	static final String callbackUrl = "?action=callbackReturn";
	//TODO: MAKE MSG PROPERTIES with spring config
	
	
	
	static final String ACTION_RESET = "reset";
	static final String ACTION_GET_REQUEST_TOKEN = "getRequestToken";
	static final String ACTION_GET_ACCESS_TOKEN = "getAccessToken";
	static final String ACTION_CALL_BACK_RETURN = "callbackReturn";
	
	static final String SESSION_REQUEST_TOKEN = "requestToken";
	static final String SESSION_REQUEAT_TOKEN_SECRET = "requestTokenSecret";
	static final String SESSION_ACCESS_TOKEN = "accessToken";
	static final String SESSION_NOTE_STORE_URL = "noteStoreUrl";
	static final String SESSION_VERIFIER = "verifier";

	static final String REQ_PARAM_ACTION = "action";
	static final String REQ_PARAM_OAUTH_TOKEN = "oauth_token";
	static final String REQ_PARAM_OAUTH_VERIFIER = "oauth_verifier";

	@Override
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) 
	{
		  log.info("****** Inside the Evernote Auth Webscript ******");
		  
		  Map<String, Object> model = new HashMap<String, Object>();

		  WebScriptSession session = req.getRuntime().getSession();
		  String accessToken = (String)session.getValue(SESSION_ACCESS_TOKEN);
		  String requestToken = (String)session.getValue(SESSION_REQUEST_TOKEN);
		  String requestTokenSecret = (String)session.getValue(SESSION_REQUEAT_TOKEN_SECRET);
		  String verifier = (String)session.getValue(SESSION_VERIFIER);
		  String noteStoreUrl = (String)session.getValue(SESSION_NOTE_STORE_URL);
		  		  
		  //Check the request action to see what to do next
		  String action = req.getParameter(REQ_PARAM_ACTION);
		  
		  if ("".equals(consumerKey)) {
		
			  log.error(
				  		"Before using this sample code you must edit the file Evernote.java " +
				  		"and replace consumerKey and consumerSecret with the values that you received from Evernote." +
				  		"If you do not have an API key, you can request one from " +
				  		"<a href='http://dev.evernote.com/documentation/cloud/'>http://dev.evernote.com/documentation/cloud/</a>"				  		
					  );
		
		  } else if (action != null) {
			  
			  log.info("****** Checking Action Param: "+action);
		    
			  //TODO: inject with spring configuration
		      // Set up the Scribe OAuthService. To access the Evernote production service,
		      // remove EvernoteSandboxApi from the provider class below.
		      String thisUrl = req.getServerPath()+req.getURL();
		      thisUrl = thisUrl.substring(0, thisUrl.lastIndexOf('?'));
		      log.info("****** thisUrl: "+thisUrl );
		      String cbUrl = thisUrl + callbackUrl;
		      log.info("****** CallBackUrl: "+cbUrl );
		      Class providerClass = org.scribe.builder.api.EvernoteApi.Sandbox.class;
		      if (urlBase.equals("https://www.evernote.com")) {
		        providerClass = org.scribe.builder.api.EvernoteApi.class;
		      }
		      OAuthService service = new ServiceBuilder()
		          .provider(providerClass)
		          .apiKey(consumerKey)
		          .apiSecret(consumerSecret)
		          .callback(cbUrl)
		          .build();
			  //TODO: inject with spring configuration

		      try {
		        if (ACTION_RESET.equals(action)) {
		        	log.info("****** Resetting Session");
		          	// Empty the server's stored session information for the current
		          	// browser user so we can redo the test.
		            session.removeValue(SESSION_ACCESS_TOKEN);
		          	session.removeValue(SESSION_NOTE_STORE_URL);
		          	session.removeValue(SESSION_REQUEAT_TOKEN_SECRET);
		          	session.removeValue(SESSION_REQUEST_TOKEN);
		          	session.removeValue(SESSION_VERIFIER);
		          
		          	accessToken = null;
		          	requestToken = null;
		          	verifier = null;
		          	requestTokenSecret = null;
		          	noteStoreUrl = null;
		          	log.info("****** Removed all attributes from user session");

		        } else if (ACTION_GET_REQUEST_TOKEN.equals(action)) {
		        	// Send an OAuth message to the Provider asking for a new Request
		        	// Token because we don't have access to the current user's account.
		        	Token scribeRequestToken = service.getRequestToken();
							
		          	log.info("****** GetRequestToken Reply: " + scribeRequestToken.getRawResponse() );
		          	requestToken = scribeRequestToken.getToken();
		          	requestTokenSecret = scribeRequestToken.getSecret();
		          	session.setValue(SESSION_REQUEST_TOKEN, requestToken);
		          	session.setValue(SESSION_REQUEAT_TOKEN_SECRET, scribeRequestToken.getSecret());

		        } else if (ACTION_GET_ACCESS_TOKEN.equals(action)) {
		        	// Send an OAuth message to the Provider asking to exchange the
		        	// existing Request Token for an Access Token
		        	Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
		          	Verifier scribeVerifier = new Verifier(verifier);
		          	EvernoteAuthToken token = new EvernoteAuthToken(service.getAccessToken(scribeRequestToken, scribeVerifier));
		          	log.info("****** GetAccessToken Reply: " + token.getRawResponse() );
		          	accessToken = token.getToken();
		          	noteStoreUrl = token.getNoteStoreUrl();
		          	session.setValue(SESSION_ACCESS_TOKEN, accessToken);
		          	session.setValue(SESSION_NOTE_STORE_URL, noteStoreUrl);
		         
		        } else if (ACTION_CALL_BACK_RETURN.equals(action)) {
		        	requestToken = req.getParameter(REQ_PARAM_OAUTH_TOKEN);
		          	verifier = req.getParameter(REQ_PARAM_OAUTH_VERIFIER);
		          	session.setValue(SESSION_VERIFIER, verifier);
		          	log.info("****** CallBackReturn verifier: " +  verifier);
			      
		        } else if ("listNotebooks".equals(action)) {
		        	noteStoreUrl = (String)session.getValue("noteStoreUrl");
		            log.info("****** Listing notebooks from: " + noteStoreUrl);
		            THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
		            TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
		            NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
		            List<?> notebooks = noteStore.listNotebooks(accessToken);
		            model.put("notebooks", notebooks);
		            for (Object notebook : notebooks) {
		            	log.info("Notebook: " + ((Notebook)notebook).getName());
		            }
		            
		          }
		      } catch (Exception e) {
		        e.printStackTrace();
		        throw new WebScriptException("Could not authenticate Evernote");
		      }

		  }
		  
		  model.put("consumerKey", consumerKey);
		  model.put("accessTokenUrl", accessTokenUrl);
		  model.put("requestTokenUrl", requestTokenUrl);
		  model.put("authorizationUrlBase", authorizationUrlBase);		  
		  
		  log.info("****** Hard Coded API Values - consumerKey::"+consumerKey+" accessTokenUrl::"+accessTokenUrl+
				  " requestTokenUrl::"+accessTokenUrl+" authorizationUrlBase::"+authorizationUrlBase+"");
	
		  model.put("requestToken", requestToken);
		  model.put("requestTokenSecret", requestTokenSecret);
		  model.put("verifier", verifier);
		  model.put("accessToken", accessToken);
		  model.put("noteStoreUrl", noteStoreUrl);
		  
		  log.info("****** Session Values - requestToken::"+requestToken+" requestTokenSecret::"+requestTokenSecret+
				  " verifier::"+verifier+" accessToken::"+accessToken+" noteStoreUrl::"+noteStoreUrl+"");		  
		    
		  return model;
	  }
}	  