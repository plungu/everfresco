package com.support.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

/*
 * @IsEvernoteSyncableEvaluator
 * Extends: BaseEvaluator
 * Visiblity: Public
 * Use: evaluate if the everfresco aspect is applied to node
 */
public class HasEvernoteChannelEvaluator extends BaseEvaluator {
	
	private Logger log = Logger.getLogger(this.getClass()); 
	private static final String ASPECT_EVERFRESCO = "ef:syncable";
	
	/*
	 * @use determine if everfresco aspect has been applied. 
	 * (non-Javadoc)
	 * @see org.alfresco.web.evaluator.BaseEvaluator#evaluate(org.json.simple.JSONObject)
	 */
	@Override
	@SuppressWarnings(value = { "all" })
	public boolean evaluate(JSONObject jsonObject) {
		log.info("****  Executing Everfresco HasChannel Evaluator ****");

		boolean hasENChannel = hasEvernoteChannels(jsonObject);
		
        if (hasENChannel) {
        	log.info("************ Evaluator: Has Evernote Channel *************");
            return true;
        } else {
        	log.info("************ Evaluator: Does Not Have Evernote *************");
        	return false;
        }

	}
	
	public final Boolean hasEvernoteChannels(final JSONObject actionObject)
	{
 
		Boolean result = false;
		try
		{
			final JSONObject node = (JSONObject) actionObject.get("node");// get node
			final String nodeRef = (String) node.get("nodeRef");// get noderef of content
 
			final RequestContext rc = ThreadLocalRequestContext.getRequestContext();// get request context
			final String userId = rc.getUserId();
 
			final Connector conn = rc.getServiceRegistry().getConnectorService().getConnector("alfresco", userId, ServletUtil.getSession());// get connector
 
			// custom repository webscript which checks if noderef has given association or not
			//final String url = "/node/association/" + contentRef.replace("://", "/") + "/" + associations.get(0).replace(":", "_");
			final String url = "/api/publishing/" + nodeRef.replace("://", "/") + "/channels";
			 
 
			final Response response = conn.call(url);// get response
 
			if (Status.STATUS_OK == response.getStatus().getCode())// make sure we are getting valid response
			{
				final org.json.JSONObject scriptResponse = new org.json.JSONObject(response.getResponse());
				
				org.json.JSONObject channelData = (org.json.JSONObject) scriptResponse.get("data");
				org.json.JSONArray publishChannels = (org.json.JSONArray) channelData.get("publishChannels");
				for (int i=0; publishChannels.length()>i; i++)
				{
					org.json.JSONObject channel = (org.json.JSONObject)publishChannels.getJSONObject(i);
					org.json.JSONObject channelType = channel.getJSONObject("channelType");
					String id = channelType.getString("id");
					if (id.equalsIgnoreCase("everfresco"))
						result = true;
					break;
				}
				return result;
			}
		} 
		catch (final ConnectorServiceException e)
		{
			throw new AlfrescoRuntimeException("Failed to connect repository: " + e.getMessage());
 
		} 
		catch (final JSONException e)
		{
 
			throw new AlfrescoRuntimeException("Failed to parse JSON string: " + e.getMessage());
		}
 
		return result;
	}

}
