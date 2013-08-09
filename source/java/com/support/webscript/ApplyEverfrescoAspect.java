package com.support.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.support.model.EverfrescoModel;

public class ApplyEverfrescoAspect extends AbstractWebScript {

	Logger log = Logger.getLogger(this.getClass()); 
    
	static final String SESSION_ACCESS_TOKEN = "accessToken";
	static final String SESSION_NOTE_STORE_URL = "noteStoreUrl";
	
	private NodeService nodeService;
	private ChannelService channelService;
	
	
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		log.info("************ Executing Apply Everfreso aspect Webscript *************");
		WebScriptSession session = req.getRuntime().getSession();
		String accessToken = (String)session.getValue(SESSION_ACCESS_TOKEN);
		String noteStoreUrl = (String)session.getValue(SESSION_NOTE_STORE_URL);
		
		log.info("****** accesstoken: "+accessToken);
		log.info("****** noteStoreUrl: "+noteStoreUrl);
  
		try
    	{
//			if (accessToken == null || noteStoreUrl == null) {
//				
//				String everfrescoAuthURL = req.getServerPath()+req.getContextPath()+"/service/everfresco/authenticate?action=getAccessToken";
//				log.info("Evernote Access Token is not in session. Go to authenticate "+everfrescoAuthURL);
//				HttpServletResponse httpResponse = WebScriptServletRuntime.getHttpServletResponse(res);
//		        httpResponse.setStatus(302);
//		        httpResponse.sendRedirect(everfrescoAuthURL);
//		          
//			} else {
				
				log.info("************ Applying Everfresco Aspect *************");
				String nodeRefStr = req.getParameter("nodeRef");
		    	NodeRef nodeRef = new NodeRef(nodeRefStr);
		    	Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		    	nodeService.addAspect(nodeRef, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, null);
				 
				// build a json object
		    	JSONObject obj = new JSONObject();
		    	
		    	// put some data on it
		    	obj.put("result", "success");
		    	
		    	// build a JSON string and send it back
		    	String jsonString = obj.toString();
		    	res.getWriter().write(jsonString);
//			}
    	}
    	catch(Exception e)
    	{
    		throw new WebScriptException("Unable to serialize JSON");
    	}	
	}
	
	
	
	
}
