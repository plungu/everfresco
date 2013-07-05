package com.support.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.support.model.EverfrescoModel;

public class ApplyEverfrescoAspect extends AbstractWebScript {

	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
	
		System.out.println("************ Execute Everfresco Webscript *************");
		try
    	{
	    	String nodeRefStr = req.getParameter("nodeRef");
	    	NodeRef nodeRef = new NodeRef(nodeRefStr);
	    	Map<QName, Serializable> props = new HashMap<QName, Serializable>();
	    	nodeService.addAspect(nodeRef, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, null);
	    	System.out.println("******** Apply Everfresco Aspect **********");
			
			// build a json object
//	    	JSONObject obj = new JSONObject();
	    	
	    	// put some data on it
//	    	obj.put("field1", "data1");
	    	
	    	// build a JSON string and send it back
//	    	String jsonString = obj.toString();
//	    	res.getWriter().write(jsonString);
    	}
    	catch(Exception e)
    	{
    		throw new WebScriptException("Unable to serialize JSON");
    	}	
	}
	
	
	
	
}
