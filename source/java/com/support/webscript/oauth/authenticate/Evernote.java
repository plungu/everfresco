package com.support.webscript.oauth.authenticate;

import java.util.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.evernote.client.oauth.*;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.*;
import org.scribe.model.*;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

public class Evernote extends AbstractWebScript {

	Logger log = Logger.getLogger(this.getClass()); 

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

	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) 
	{
		  
		log.debug("****** Inside the Evernote Auth Webscript ******");
		log.debug("************************************************");
		  
		new HashMap<String, Object>();

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
		
		  } else if (action != null &&  accessToken == null || noteStoreUrl == null) {
			  
			  log.debug("****** Checking Action Param: "+action);
		    
			  // Set up the Scribe OAuthService. To access the Evernote production service,
		      // remove EvernoteSandboxApi from the provider class below.
			  String thisUrl = req.getServerPath()+req.getURL();
		      thisUrl = thisUrl.substring(0, thisUrl.lastIndexOf('?'));
		      log.debug("****** thisUrl: "+thisUrl );
		      
		      String cbUrl = thisUrl + callbackUrl;
		      log.debug("****** CallBackUrl: "+cbUrl );
		    
		      @SuppressWarnings("rawtypes")
		      Class providerClass = org.scribe.builder.api.EvernoteApi.Sandbox.class;
		      
		      if (urlBase.equals("https://www.evernote.com")) {
		    	  
		    	  providerClass = org.scribe.builder.api.EvernoteApi.class;
		      
		      }
		      
		    //TODO: inject with spring configuration
		      OAuthService service = new ServiceBuilder()
		          .provider(providerClass)
		          .apiKey(consumerKey)
		          .apiSecret(consumerSecret)
		          .callback(cbUrl)
		          .build();
			  //TODO: inject with spring configuration

		      try {
		    	  if (ACTION_RESET.equals(action)) {
		    		  
		    		  log.debug("****** Resetting Session");
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
		          	
		          		log.debug("****** Removed all attributes from user session");

		        } else if (ACTION_GET_ACCESS_TOKEN.equals(action)) {
			        
		        	// Send an OAuth message to the Provider asking for a new Request
		        	// Token because we don't have access to the current user's account.
		        	Token scribeRequestToken = service.getRequestToken();
					requestToken = scribeRequestToken.getToken();
		          	requestTokenSecret = scribeRequestToken.getSecret();
		          	
		          	session.setValue(SESSION_REQUEST_TOKEN, requestToken);
		          	session.setValue(SESSION_REQUEAT_TOKEN_SECRET, requestTokenSecret);
		          	
		          	log.debug("****** GetRequestToken Reply: " + scribeRequestToken.getRawResponse() );
		          
		        	// Send an OAuth message to the Provider asking to exchange the
		        	// existing Request Token for an Access Token
		          	String authorizationUrl = authorizationUrlBase + "?oauth_token=" + requestToken;		          			          	
		          	HttpServletResponse httpResponse = WebScriptServletRuntime.getHttpServletResponse(res);
		          	httpResponse.sendRedirect(authorizationUrl);
		          	
		          	log.debug("****** Send Redierct: " + authorizationUrl );
			          
		        } else if (ACTION_CALL_BACK_RETURN.equals(action)) {
		        	
		        	//requestToken = req.getParameter(REQ_PARAM_OAUTH_TOKEN);
		          	verifier = req.getParameter(REQ_PARAM_OAUTH_VERIFIER);
		          	session.setValue(SESSION_VERIFIER, verifier);
		          	log.debug("****** CallBackReturn verifier: " +  verifier);

		          	//Use verifier from request token exchange to get the access token
		          	Verifier scribeVerifier = new Verifier(verifier);
		          	log.debug("****** Scribe Verifier: " + scribeVerifier.getValue() );
		          	
		          	Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
		          	log.debug("****** Scribe RequestToken: " + scribeRequestToken.getToken() );
		          	
		          	EvernoteAuthToken token = new EvernoteAuthToken(service.getAccessToken(scribeRequestToken, scribeVerifier));
		          	log.debug("****** GetAccessToken Reply: " + token.getRawResponse() );
		          	
		          	accessToken = token.getToken();
		          	noteStoreUrl = token.getNoteStoreUrl();
		          	
		          	session.setValue(SESSION_ACCESS_TOKEN, accessToken);
		          	session.setValue(SESSION_NOTE_STORE_URL, noteStoreUrl);

	        	}
	        	    
		      } catch (Exception e) {
		        
		    	  e.printStackTrace();
		    	  throw new WebScriptException("Could not authenticate Evernote");
		        
		      }
		  }
        
		  	try {
		
			  	// build a json object
		    	JSONObject obj = new JSONObject();
		    	
		    	// put some data on it
		    	obj.put("accessToken", accessToken);
		    	obj.put("noteStoreUrl", noteStoreUrl);
		
		    	// build a JSON string and send it back
		    	String jsonString = obj.toString();
		    	res.getWriter().write(jsonString);
			
		  	} catch (IOException e) {
				
		  		e.printStackTrace();
			
		  	}
	    	
    	log.debug("****** Hard Coded API Values - consumerKey::"+consumerKey+" accessTokenUrl::"+accessTokenUrl+
			  " requestTokenUrl::"+accessTokenUrl+" authorizationUrlBase::"+authorizationUrlBase+"");	
	  
    	log.debug("****** Session Values - requestToken::"+requestToken+" requestTokenSecret::"+requestTokenSecret+
			  " verifier::"+verifier+" accessToken::"+accessToken+" noteStoreUrl::"+noteStoreUrl+"");		  
    	
	}
}	  