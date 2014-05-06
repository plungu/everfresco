package com.support.share;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.alfresco.web.site.SlingshotUserFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AlfrescoUser;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;

@SuppressWarnings("deprecation")
public class EverfrescoUserFactory extends SlingshotUserFactory {

	public static final String PROPERTY_EVERFRESCO_USERNAME = "{http://www.alfresco.com/model/everfresco/1.0}efUserName";
	public static final String PROPERTY_EVERFRESCO_PASSWORD = "{http://www.alfresco.com/model/everfresco/1.0}efPassword";
	public static final String PROP_EF_USERNAME = "efUserName";
	public static final String PROP_EF_PASSWORD = "efPassword";
	
    @Override
    protected AlfrescoUser constructUser(JSONObject properties, Map<String, Boolean> capabilities,
            Map<String, Boolean> immutability) throws JSONException {
        AlfrescoUser user = super.constructUser(properties, capabilities, immutability);
        user.setProperty(PROP_EF_USERNAME, properties.has(PROPERTY_EVERFRESCO_USERNAME) 
        		? properties.getString(PROPERTY_EVERFRESCO_USERNAME) : null);
        user.setProperty(PROP_EF_PASSWORD, properties.has(PROPERTY_EVERFRESCO_PASSWORD) 
        		? properties.getString(PROPERTY_EVERFRESCO_PASSWORD) : null);
        return user;
    }

    @Override
    public void saveUser(AlfrescoUser user) throws UserFactoryException {
        RequestContext context = (RequestContext)ThreadLocalRequestContext.getRequestContext();
        if (!context.getUserId().equals(user.getId())) {
            throw new UserFactoryException("Unable to persist user with different Id that current Id.");
        }
        
        StringBuilderWriter buf = new StringBuilderWriter(512);
        JSONWriter writer = new JSONWriter(buf);
        
        try {
            writer.startObject();
            
            writer.writeValue("username", user.getId());
            
            writer.startValue("properties");
            writer.startObject();
            writer.writeValue(CM_FIRSTNAME, user.getFirstName());
            writer.writeValue(CM_LASTNAME, user.getLastName());
            writer.writeValue(CM_JOBTITLE, user.getJobTitle());
            writer.writeValue(CM_ORGANIZATION, user.getOrganization());
            writer.writeValue(CM_LOCATION, user.getLocation());
            writer.writeValue(CM_EMAIL, user.getEmail());
            writer.writeValue(CM_TELEPHONE, user.getTelephone());
            writer.writeValue(CM_MOBILE, user.getMobilePhone());
            writer.writeValue(CM_SKYPE, user.getSkype());
            writer.writeValue(CM_INSTANTMSG, user.getInstantMsg());
            writer.writeValue(CM_GOOGLEUSERNAME, user.getGoogleUsername());
            writer.writeValue(CM_COMPANYADDRESS1, user.getCompanyAddress1());
            writer.writeValue(CM_COMPANYADDRESS2, user.getCompanyAddress2());
            writer.writeValue(CM_COMPANYADDRESS3, user.getCompanyAddress3());
            writer.writeValue(CM_COMPANYPOSTCODE, user.getCompanyPostcode());
            writer.writeValue(CM_COMPANYFAX, user.getCompanyFax());
            writer.writeValue(CM_COMPANYEMAIL, user.getCompanyEmail());
            writer.writeValue(CM_COMPANYTELEPHONE, user.getCompanyTelephone());
            
            // START everfresco Specific properties
            writer.writeValue(PROPERTY_EVERFRESCO_USERNAME, user.getStringProperty(PROP_EF_USERNAME));
            writer.writeValue(PROPERTY_EVERFRESCO_PASSWORD, user.getStringProperty(PROP_EF_PASSWORD));
            // END everfresco Specific properties
            
            writer.endObject();
		    writer.endValue();
		    
		    writer.startValue("content");
		    writer.startObject();
		    writer.writeValue(CM_PERSONDESCRIPTION, user.getBiography());
		    writer.endObject();
		    writer.endValue();
		    
		    writer.endObject();
		    
		    Connector conn = FrameworkUtil.getConnector(context, ALFRESCO_ENDPOINT_ID);
		    ConnectorContext c = new ConnectorContext(HttpMethod.POST);
		    c.setContentType("application/json");
		    Response res = conn.call("/slingshot/profile/userprofile", c,
		            new ByteArrayInputStream(buf.toString().getBytes()));
		    if (Status.STATUS_OK != res.getStatus().getCode()) {
		        throw new UserFactoryException("Remote error during User save: " + res.getStatus().getMessage());
		    }
		} catch (IOException ioErr) {
		    throw new UserFactoryException("IO error during User save: " + ioErr.getMessage(), ioErr);
		} catch (ConnectorServiceException cse)	{
		    throw new UserFactoryException("Configuration error during User save: " + cse.getMessage(), cse);
		}
	}
}
