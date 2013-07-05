package com.support.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/*
 * @IsEvernoteSyncableEvaluator
 * Extends: BaseEvaluator
 * Visiblity: Public
 * Use: evaluate if the everfresco aspect is applied to node
 */
public class IsEvernoteSyncableEvaluator extends BaseEvaluator {
	private static final String ASPECT_EVERFRESCO = "ef:syncable";
	
	/*
	 * @use determine if everfresco aspect has been applied. 
	 * (non-Javadoc)
	 * @see org.alfresco.web.evaluator.BaseEvaluator#evaluate(org.json.simple.JSONObject)
	 */
	@Override
	public boolean evaluate(JSONObject jsonObject) {
		
        try {
        	JSONArray nodeAspects = getNodeAspects(jsonObject);
        	System.out.println("********* Calling Evaluator: EverFresco ********* ");
        	
        	if (nodeAspects == null) {
                return false;
            } else {
                if (nodeAspects.contains(ASPECT_EVERFRESCO)) {
                	System.out.println("********* Evaluator: EverFresco Aspect Applied ********* ");
                	
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("JSONException whilst running action evaluator: " + err.getMessage());
        }
	}

}
