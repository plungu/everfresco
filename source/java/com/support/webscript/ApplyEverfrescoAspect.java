package com.support.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SealedObject;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
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
import com.support.publishing.EverfrescoChannelType;

public class ApplyEverfrescoAspect extends AbstractWebScript {

	Logger log = Logger.getLogger(this.getClass()); 
    
	static final String SESSION_ACCESS_TOKEN = "accessToken";
	static final String SESSION_NOTE_STORE_URL = "noteStoreUrl";
	
	private NodeService nodeService;
	private ChannelService channelService;
    private MetadataEncryptor encryptor;
	
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }
    
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		
		log.info("************ Syncing Everfreso and add aspect Webscript *************");		
		
		try
    	{				
				String nodeRefStr = req.getParameter("nodeRef");
		    	NodeRef nodeRef = new NodeRef(nodeRefStr);
		    	log.info("************ Getting nodeRef: "+ nodeRef.getId());
		    	
		    	Channel channel = channelService.getChannelById("workspace://SpacesStore/df6e88dd-9884-4e6b-9230-ca5bdf27a3f5");
		    	Map<QName, Serializable> channelProps = null;
		    	if(channel != null)
		    	{
			    	log.info("************ Getting channel: "+ channel.getName());
			    	channelProps = channel.getProperties();
		    	}else{
		    		List<Channel> channels = channelService.getChannels();
		    		channel = channels.get(0);
		    		String channelId = channel.getId();
		    		String channelTypeID = channel.getChannelType().getId();
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
	}
	
	
	
	
}
