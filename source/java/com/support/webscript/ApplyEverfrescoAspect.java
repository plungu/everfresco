package com.support.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.support.publishing.EverfrescoChannelType;

public class ApplyEverfrescoAspect extends AbstractWebScript {

	Logger log = Logger.getLogger(this.getClass()); 
    
	static final String SESSION_ACCESS_TOKEN = "accessToken";
	static final String SESSION_NOTE_STORE_URL = "noteStoreUrl";
	
	private ChannelService channelService;
    private WikiService wikiService;
	
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public void setNodeService(NodeService nodeService) {
	}

    public void setEncryptor(MetadataEncryptor encryptor)
    {
    }
    
    public void setWikiService(WikiService wikiService)
    {
    	this.wikiService = wikiService;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		
		log.info("************ Syncing Everfreso and add aspect Webscript *************");		
		
		String title = req.getParameter("title");
		String siteShortName = req.getParameter("siteName");
		NodeRef nodeRef = null;
		
		String nodeRefStr = req.getParameter("nodeRef");
		if(nodeRefStr == null || nodeRefStr.isEmpty())
		{	
			if( (title != null && !title.isEmpty()) || (siteShortName != null && !siteShortName.isEmpty())   )
			{
				log.info("************ Wiki Title *************: "+title );		
				log.info("************ Site name *************: "+ siteShortName);	
				WikiPageInfo wikiInfo = wikiService.getWikiPage(siteShortName, title);
				nodeRef = wikiInfo.getNodeRef();
			}else{
				throw new WebScriptException("No Wiki Params found");
			}
		
		}else{
		
			nodeRef = new NodeRef(nodeRefStr);
		}
		
		try
    	{				
	    	log.info("************ nodeRef: "+ nodeRef.getId());
	    	
	    	Channel channel = null;
	    	
	    	//Gets the first/default everfresco channel available
	    	//Note: even when multiple configured it will only grab the first available
	    	List<Channel> channels = channelService.getChannels();
	    	for (Channel c:channels)
	    	{
	    		c.getId();
	    		if (c.getChannelType().getId() == "everfresco")
	    		{
	    			channel = c;
	    			break;
	    		}
		    	
	    	}
	    	Map<QName, Serializable> channelProps = null;
	    	
	    	if(channel != null)
	    	{
		    	log.info("************ Getting channel: "+ channel.getName());
		    	channelProps = channel.getProperties();
	    	}else{
	    		throw new WebScriptException("Channel Could not be found!");
	    	}
	    	
			EverfrescoChannelType everfrescoChannel = (EverfrescoChannelType)channel.getChannelType();
			everfrescoChannel.publish(nodeRef, channelProps);
			log.info("************ Syncing Everfreso: "+ everfrescoChannel.getId());
			
	    	//nodeService.addAspect(nodeRef, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, null);
			//log.info("************ Applying Everfresco Aspect *************");
			 
			// build a json object
	    	JSONObject obj = new JSONObject();
	    	
	    	// put some data on it
	    	obj.put("result", "success");
	    	
	    	// build a JSON string and send it back
	    	String jsonString = obj.toString();
	    	res.getWriter().write(jsonString);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new WebScriptException(e.getMessage());
    	}
			
	}//end execute method
	
	
	
	
}//end class
