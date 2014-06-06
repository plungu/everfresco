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
public class IsEvernoteSyncableEvaluator extends BaseEvaluator {
	
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
		log.info("************ Executing Everfresco Evaluator *************");
		
        try {
        	JSONArray nodeAspects = getNodeAspects(jsonObject);
    		log.debug("********* Calling EverFresco Evaluator on: "+jsonObject.get("name"));
        	
        	if (nodeAspects == null) {
                return false;
            } else {
                if (nodeAspects.contains(ASPECT_EVERFRESCO)) {
                	log.debug("************ Evaluator: EverFresco Aspect Applied *************");
                    return true;
                } else {
                	log.debug("************ Evaluator: EverFresco Aspect Not Applied *************");
                	return false;
                }
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("JSONException whilst running action evaluator: " + err.getMessage());
        }
	}
	
	
}
