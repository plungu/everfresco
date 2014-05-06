package com.support.webscript;

import java.io.IOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.support.model.EverfrescoModel;

public class RemoveEverfrescoAspect extends AbstractWebScript {

	private Logger log = Logger.getLogger(this.getClass()); 
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
	
		log.info("************ Executing Everfresco Webscript *************");

		try {
	    	
			String nodeRefStr = req.getParameter("nodeRef");
	    	NodeRef nodeRef = new NodeRef(nodeRefStr);
	    	nodeService.removeAspect(nodeRef, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE);
			log.debug("************ Removing Everfresco Aspect *************");
			
    	} catch(Exception e) {
    		
    		throw new WebScriptException("Unable to serialize JSON");
    	
    	}	
	}
}
