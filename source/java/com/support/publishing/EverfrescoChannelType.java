package com.support.publishing;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType.AuthStatus;
import org.alfresco.service.cmr.publishing.channels.ChannelType.AuthUrlPair;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;

import com.evernote.client.oauth.EvernoteAuthToken;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.support.model.EverfrescoModel;

public class EverfrescoChannelType extends AbstractChannelType {
	
	Logger log = Logger.getLogger(this.getClass()); 
	
	public final static String ID = "everfresco";
	
	public static final String NAMESPACE = "http://www.alfresco.com/model/everfresco/1.0";
	public static final String PREFIX = "ef";
	public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");
    public static final QName ASPECT_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannelAspect");
    
	//TODO: MAKE MSG PROPERTIES with spring config
	/*
	 * Fill in your Evernote API key. To get an API key, go to
	 * http://dev.evernote.com/documentation/cloud/
	 */
	static final String consumerKey = "plungu";
	static final String consumerSecret = "f3716cbcf05aae1b";
  
	/*
	 * Replace this value with https://www.evernote.com to switch from the Evernote
	 * sandbox server to the Evernote production server.
	 */
	static final String urlBase = "https://sandbox.evernote.com";	  
	static final String requestTokenUrl = urlBase + "/oauth";
	static final String accessTokenUrl = urlBase + "/oauth";
	static final String authorizationUrlBase = urlBase + "/OAuth.action";
	//Change this to use alfresco server port
	
	static final String REQ_PARAM_ACTION = "action";
	static final String REQ_PARAM_OAUTH_TOKEN = "oauth_token";
	static final String REQ_PARAM_OAUTH_VERIFIER = "oauth_verifier";
	
	public OAuthService service; 
	public NodeService nodeService;
	public ContentService contentService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public QName getChannelNodeType() {
		// TODO Auto-generated method stub
		return TYPE_DELIVERY_CHANNEL;
	}

	@Override
	public boolean canPublish() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canUnpublish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canPublishStatusUpdates() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public AuthUrlPair getAuthorisationUrls(Channel channel, String callbackUrl)
    {
	      Class providerClass = org.scribe.builder.api.EvernoteApi.Sandbox.class;
	      if (urlBase.equals("https://www.evernote.com")) {
	        providerClass = org.scribe.builder.api.EvernoteApi.class;
	      }
	      
	      //TODO: inject with spring configuration
	      service = new ServiceBuilder()
	          .provider(providerClass)
	          .apiKey(consumerKey)
	          .apiSecret(consumerSecret)
	          .callback(callbackUrl)
	          .build();
		  //TODO: inject with spring configuration
	      
		log.info("****** Callback URL: "+callbackUrl);

		Token scribeRequestToken = service.getRequestToken();
		String requestToken = scribeRequestToken.getToken();
		String requestTokenSecret = scribeRequestToken.getSecret();
		log.info("****** GetRequestToken: " + scribeRequestToken.getRawResponse() );

        if (requestToken != null && requestTokenSecret != null)
        {
            Map<QName,Serializable> channelProps = new HashMap<QName, Serializable>();
            channelProps.put(PublishingModel.PROP_ACCESS_TOKEN, requestToken);
            channelProps.put(PublishingModel.PROP_ACCESS_SECRET, requestTokenSecret);
            channelProps = getEncryptor().encrypt(channelProps);
            getChannelService().updateChannel(channel, channelProps);
        }
        
		// Send an OAuth message to the Provider asking to exchange the
		// existing Request Token for an Access Token
		String authorizationUrl = authorizationUrlBase + "?oauth_token=" + requestToken;		          			          	

		log.info("****** Redirecting to: " + authorizationUrl );

		
		// Returning a null as the authorisation request URL here to indicate that we should use our own
		// credential-gathering mechanism.
		return new AuthUrlPair(authorizationUrl, callbackUrl);
    }
    
    @Override
    protected AuthStatus internalAcceptAuthorisation(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        AuthStatus authorised = AuthStatus.UNAUTHORISED;
        
        String accessToken = null;
        String noteStoreUrl = null;
        if (callbackParams.containsKey("access_token"))
        {
            //We have been given the access token directly.
            accessToken = callbackParams.get("access_token")[0];
        }
        else if (callbackParams.containsKey(REQ_PARAM_OAUTH_VERIFIER))
        {
        	
        	String requestToken = channel.getProperties().get(PublishingModel.PROP_ACCESS_TOKEN).toString();
        	log.info("****** Encrytped requestToken: " + requestToken );
        	requestToken = getEncryptor().decrypt(PublishingModel.PROP_ACCESS_TOKEN, requestToken).toString();
        	log.info("****** requestToken: " + requestToken );
        	
        	String requestTokenSecret = channel.getProperties().get(PublishingModel.PROP_ACCESS_SECRET).toString();
        	log.info("****** Encrytped requestTokenSecret: " + requestTokenSecret );
        	requestTokenSecret = getEncryptor().decrypt(PublishingModel.PROP_ACCESS_SECRET, requestTokenSecret).toString();
        	log.info("****** requestTokenSecret: " + requestTokenSecret );

        	//requestToken = req.getParameter(REQ_PARAM_OAUTH_TOKEN);
          	String verifier = callbackParams.get(REQ_PARAM_OAUTH_VERIFIER)[0];
        
          	//We have been passed an authorisation code that needs to be exchanged for a token
          	Verifier scribeVerifier = new Verifier(verifier);
          	log.info("****** Scribe Verifier: " + scribeVerifier.getValue() );
          	Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
          	log.info("****** Scribe RequestToken: " + scribeRequestToken.getToken() );
          	EvernoteAuthToken token = new EvernoteAuthToken(service.getAccessToken(scribeRequestToken, scribeVerifier));
          	log.info("****** GetAccessToken Reply: " + token.getRawResponse() );
          	accessToken = token.getToken();
          	noteStoreUrl = token.getNoteStoreUrl();
        }
        if (accessToken != null)
        {
            Map<QName,Serializable> channelProps = new HashMap<QName, Serializable>();
            channelProps.put(PublishingModel.PROP_OAUTH2_TOKEN, accessToken);
            channelProps.put(PublishingModel.PROP_ASSET_URL, noteStoreUrl);
//            channelProps = getEncryptor().encrypt(channelProps);
            getChannelService().updateChannel(channel, channelProps);
            authorised = AuthStatus.AUTHORISED;
        }
        
        return authorised;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map channelProperties)
    {
        
    	log.info("****** Publishing Node ***********" );
      	// To create a new note, simply create a new Note object and fill in 
        // attributes such as the note's title.
        Note note = new Note();
        
        Map<QName, Serializable> props = nodeService.getProperties(nodeToPublish);

        String title = props.get(ContentModel.PROP_TITLE).toString();
        note.setTitle(title);
        String fileName = props.get(ContentModel.PROP_NAME).toString();
        log.info("****** Node Name : " + fileName + " Title: "+title );
      	
        ContentData contentData = (ContentData) props.get(ContentModel.PROP_CONTENT);
        String originalMimeType = contentData.getMimetype();
        
        log.info("****** MimeType : " + originalMimeType );
        
        ContentReader reader = contentService.getReader(nodeToPublish, ContentModel.PROP_CONTENT);
        String content = reader.getContentString();

        note.setContent(content);
        log.info("****** Setting Content : " + originalMimeType );
        
        // Finally, send the new note to Evernote using the createNote method
        // The new Note object that is returned will contain server-generated
        // attributes such as the new note's unique GUID.
        try {
        	
	        String noteStoreUrl = channelProperties.get(PublishingModel.PROP_ASSET_URL).toString();
	        noteStoreUrl = getEncryptor().decrypt(PublishingModel.PROP_ASSET_URL, noteStoreUrl).toString();
	    	log.info("****** noteStoreUrl: " + noteStoreUrl );
	        THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
	        TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
	        NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
	        		
	        String accessToken = channelProperties.get(PublishingModel.PROP_OAUTH2_TOKEN).toString();
	        log.info("****** accessToken: " + accessToken );
//	        accessToken = (String)getEncryptor().decrypt(PublishingModel.PROP_OAUTH2_TOKEN, accessToken);
//	    	log.info("****** accessToken: " + accessToken );
	        Note createdNote = noteStore.createNote(accessToken, note);
	        String newNoteGuid = createdNote.getGuid();
	        
	        log.info("****** newNoteGuid: " + newNoteGuid );
	        
	    	nodeService.addAspect(nodeToPublish, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, null);
    	
        } catch (Exception e){
        	e.printStackTrace();
        	log.error("Error Creating Note: "+note.getTitle());
            
        }
        
    }
    
    /**
     * Helper method to read the contents of a file on disk and create a new Data object.
     */
    private static Data readFileAsData(String fileName) throws Exception {

      // Read the full binary contents of the file
      FileInputStream in = new FileInputStream(fileName);
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      byte[] block = new byte[10240];
      int len;
      while ((len = in.read(block)) >= 0) {
        byteOut.write(block, 0, len);
      }
      in.close();
      byte[] body = byteOut.toByteArray();
      
      // Create a new Data object to contain the file contents
      Data data = new Data();
      data.setSize(body.length);
      data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
      data.setBody(body);
      
      return data;
    }

    /**
     * Helper method to convert a byte array to a hexadecimal string.
     */
    public static String bytesToHex(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      for (byte hashByte : bytes) {
        int intVal = 0xff & hashByte;
        if (intVal < 0x10) {
          sb.append('0');
        }
        sb.append(Integer.toHexString(intVal));
      }
      return sb.toString();
    }
}

