package com.support.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
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

import com.support.model.EverfrescoModel;
import com.support.publishing.EverfrescoChannelType;

public class ApplyEverfrescoAspect extends AbstractWebScript {

	private Logger log = Logger.getLogger(this.getClass()); 
    
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

		//handle requests from the wiki action and the doclib action
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

			if (nodeRefStr==null || nodeRefStr.isEmpty())
				throw new WebScriptException("Noderef not found");

			nodeRef = new NodeRef(nodeRefStr);
		}
		
		try
    	{				
	    	
			log.info("************ nodeRef: "+ nodeRef.getId());
	    	
	    	Channel channel = null;
	    	
	    	//Gets everfresco channels for the everfresco doclib action 
	    	List<Channel> channels = channelService.getChannels();
	    	
	    	//find the first Everfresco channel created in case no default is selected
	    	Long firstChannel = new Date().getTime();
    		Long nextChannel = null;
    		
	    	for (Channel c:channels) 
	    	{
	    		
                if(c.getChannelType().getId() == "everfresco")
                {		
		    		
		    		nextChannel = ((Date)c.getProperties().get(ContentModel.PROP_CREATED)).getTime();
		    		
		    		if (firstChannel > nextChannel) {
		    			
		    			firstChannel = nextChannel;
		    			channel = c;
		    					    		
		    		} 
	    		}
            
	    	}
	    	
	    	//find the selected default channel to use with the everfresco action
	    	for (Channel c:channels) 
	    	{
	    		
                if(c.getChannelType().getId() == "everfresco"){
	    		
		    		boolean defaultChannel = false;
		    		
		    		defaultChannel = (Boolean)c.getProperties().get(EverfrescoModel.PROPERTY_EVERFRESCO_DEFAULT_CHANNEL);
		    		
		    		if (defaultChannel) {
		    			
		    			channel = c;
		    			break;
		    		
		    		} 
	    		}
	    	}
	    	
	    	
	    	//Call publish method to pubilsh to the node to the selected channel 
			Map<QName, Serializable> channelProps = null;	    	
	    	if(channel != null) {		    	
	    		log.info("************ Getting channel: "+ channel.getName());
		    	channelProps = channel.getProperties();
	    	
	    	} else {	    		
	    		throw new WebScriptException("Channel Could not be found!");	    	
	    	}
	    	
	    	EverfrescoChannelType everfrescoChannel = (EverfrescoChannelType)channel.getChannelType();
			everfrescoChannel.publish(nodeRef, channelProps);
			log.debug("************ Syncing Everfreso: "+ everfrescoChannel.getId());
			
			//put some stuff on the response
			//this has no significance at this point
	    	JSONObject obj = new JSONObject();	    	
	    	// put some data on it
	    	obj.put("result", "success");	    	
	    	// build a JSON string and send it back
	    	String jsonString = obj.toString();
	    	res.getWriter().write(jsonString);
	    	
	    	
    	} catch(Exception e) {
    		
    		e.printStackTrace();
    		throw new WebScriptException(e.getMessage());
    	
    	}
			
	}	
}
