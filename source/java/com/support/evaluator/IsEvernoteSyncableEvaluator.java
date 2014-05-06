package com.support.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/*
 * @IsEvernoteSyncableEvaluator
 * Extends: BaseEvaluator
 * Visiblity: Public
 * Use: evaluate if the everfresco aspect is applied to node
 */
public class IsEvernoteSyncableEvaluator extends BaseEvaluator {
	
	Logger log = Logger.getLogger(this.getClass()); 

	private static final String ASPECT_EVERFRESCO = "ef:syncable";
	
	/*
	 * @use determine if everfresco aspect has been applied. 
	 * (non-Javadoc)
	 * @see org.alfresco.web.evaluator.BaseEvaluator#evaluate(org.json.simple.JSONObject)
	 */
	@Override
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
