package com.support.publishing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.AssociationRef;
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
	public static final String PREFIX = "ef";
	
	/*
	 * Replace this value with https://www.evernote.com to switch from the Evernote
	 * sandbox server to the Evernote production server.
	 */
	static String urlBase = "https://sandbox.evernote.com";	  
	static final String requestTokenUrl = urlBase + "/oauth";
	static final String accessTokenUrl = urlBase + "/oauth";
	static final String authorizationUrlBase = urlBase + "/OAuth.action"; 
	
	static final String REQ_PARAM_ACTION = "action";
	static final String REQ_PARAM_OAUTH_TOKEN = "oauth_token";
	static final String REQ_PARAM_OAUTH_VERIFIER = "oauth_verifier";
	
	static final String TEXT_PLAIN = "text/plain";
	static final String APPLICATION_PDF = "application/pdf";
	static final String APPLICATION_MSWORD = "application/msword";
	static final String APPLICATION_PPT = "application/vnd.ms-powerpoint";
	static final String APPLICATION_XLS = "application/vnd.ms-excel";
	static final String IMAGE_JPEG = "image/jpeg";
	static final String IMAGE_PNG = "image/png";
	static final String VIDEO_MP4 = "video/mp4";
	static final String VIDEO_MP3 = "video/mp3";

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
		return ID;
	}

	@Override
	public QName getChannelNodeType() {
		return EverfrescoModel.TYPE_EVERFRESCO_DELIVERY_CHANNEL;
	}

	@Override
	public boolean canPublish() {
		return true;
	}

	@Override
	public boolean canUnpublish() {
		return false;
	}

	@Override
	public boolean canPublishStatusUpdates() {
		return false;
	}

    @SuppressWarnings("unchecked")
	@Override
    public AuthUrlPair getAuthorisationUrls(Channel channel, String callbackUrl)
    {
    	@SuppressWarnings("rawtypes")
		Class providerClass = org.scribe.builder.api.EvernoteApi.Sandbox.class;
    	
    	String authorizationUrl = "";
    	
    	Serializable useSandbox = channel.getProperties().get(EverfrescoModel.PROPERTY_EVERFRESCO_USE_SANDBOX);
    	boolean useSandBox = Boolean.parseBoolean(useSandbox.toString());
	    if ( useSandbox != null && !useSandBox ) {
	    	urlBase = "https://www.evernote.com";
	    	providerClass = org.scribe.builder.api.EvernoteApi.class;
	    }
	    
		    Serializable consumerKey = channel.getProperties().get(EverfrescoModel.PROPERTY_EVERFRESCO_CONSUMER_KEY);
		    Serializable consumerSecret = channel.getProperties().get(EverfrescoModel.PROPERTY_EVERFRESCO_CONSUMER_SECRET);
		    
		    if ( (consumerKey != null || consumerSecret != null))
		    { 
			    log.info("consumer key: " + consumerKey);
			    log.info("consumer secret: " + consumerSecret);
			    
			    //TODO: inject with spring configuration
			    service = new ServiceBuilder()
			       	.provider(providerClass)
			       	.apiKey(consumerKey.toString())
			        .apiSecret(consumerSecret.toString())
			        .callback(callbackUrl)
			        .build();  	
		    
			log.info("****** Callback URL: "+callbackUrl);
	
			Token scribeRequestToken = service.getRequestToken();
			String requestToken = scribeRequestToken.getToken();
			String requestTokenSecret = scribeRequestToken.getSecret();
			
			log.info("****** GetRequestToken: " + scribeRequestToken.getRawResponse() );
	
	        if (requestToken != null && requestTokenSecret != null) {
	        	
	            Map<QName,Serializable> channelProps = new HashMap<QName, Serializable>();
	            channelProps.put(PublishingModel.PROP_ACCESS_TOKEN, requestToken);
	            channelProps.put(PublishingModel.PROP_ACCESS_SECRET, requestTokenSecret);
	            channelProps = getEncryptor().encrypt(channelProps);
	            getChannelService().updateChannel(channel, channelProps);
	            
	        }
	        
			// Send an OAuth message to the Provider asking to exchange the
			// existing Request Token for an Access Token
			authorizationUrl = authorizationUrlBase + "?oauth_token=" + requestToken;		          			          	
			log.info("****** Redirecting to: " + authorizationUrl );
    	}
		return new AuthUrlPair(authorizationUrl, callbackUrl);
    }
    
    @Override
    protected AuthStatus internalAcceptAuthorisation(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams) {
        AuthStatus authorised = AuthStatus.UNAUTHORISED;
        
        String accessToken = null;
        String noteStoreUrl = null;
        
        try 
        {
	        if (callbackParams.containsKey("access_token")) {
	            //We have been given the access token directly.
	            accessToken = callbackParams.get("access_token")[0];
	        } else if (callbackParams.containsKey(REQ_PARAM_OAUTH_VERIFIER)) {
	        	
	        	String requestToken;
	        	String requestTokenSecret;
	        	
	        	requestToken = channel.getProperties().get(PublishingModel.PROP_ACCESS_TOKEN).toString();
	        	log.debug("****** Encrytped requestToken: " + requestToken );
	        	requestToken = getEncryptor().decrypt(PublishingModel.PROP_ACCESS_TOKEN, requestToken).toString();
	        	log.debug("****** requestToken: " + requestToken );
	        	
	        	requestTokenSecret = channel.getProperties().get(PublishingModel.PROP_ACCESS_SECRET).toString();
	        	log.debug("****** Encrytped requestTokenSecret: " + requestTokenSecret );
	        	requestTokenSecret = getEncryptor().decrypt(PublishingModel.PROP_ACCESS_SECRET, requestTokenSecret).toString();
	        	log.debug("****** requestTokenSecret: " + requestTokenSecret );
	
	        	//requestToken = req.getParameter(REQ_PARAM_OAUTH_TOKEN);
	          	String verifier = callbackParams.get(REQ_PARAM_OAUTH_VERIFIER)[0];
	        
	          	//We have been passed an authorization code that needs to be exchanged for a token
	          	Verifier scribeVerifier = new Verifier(verifier);
	          	log.debug("****** Scribe Verifier: " + scribeVerifier.getValue() );
	          	
	          	Token scribeRequestToken = new Token(requestToken, requestTokenSecret);
	          	log.debug("****** Scribe RequestToken: " + scribeRequestToken.getToken() );
	          	
	          	EvernoteAuthToken token = new EvernoteAuthToken(service.getAccessToken(scribeRequestToken, scribeVerifier));
	          	log.debug("****** GetAccessToken Reply: " + token.getRawResponse() );
	          	
	          	accessToken = token.getToken();
	          	noteStoreUrl = token.getNoteStoreUrl();
	        }
        }catch(Exception e){
        	e.printStackTrace();
        }
	        
        if (accessToken != null) {
            Map<QName,Serializable> channelProps = new HashMap<QName, Serializable>();
            channelProps.put(PublishingModel.PROP_OAUTH2_TOKEN, accessToken);
            channelProps.put(PublishingModel.PROP_ASSET_URL, noteStoreUrl);
            getChannelService().updateChannel(channel, channelProps);
            authorised = AuthStatus.AUTHORISED;
        }else{
        	authorised = AuthStatus.UNAUTHORISED;
        }
        
        return authorised;
    }

    @Override
    public void publish(NodeRef nodeToPublish, @SuppressWarnings("rawtypes") Map channelProperties)
    {        
    	log.info("****** Publishing Node ***********" );
      	
    	// To create a new note, simply create a new Note object and fill in 
        // attributes such as the note's title.
        Note note = new Note();
        
        Map<QName, Serializable> props = nodeService.getProperties(nodeToPublish);
        
        Serializable description = "";
        Serializable title = "";
        Serializable fileName = "";
        
        title = props.get(ContentModel.PROP_TITLE);
               
        fileName = props.get(ContentModel.PROP_NAME);
        
        // If there is no title, one will be constructed from {fileName}
        if ( title.toString().length() == 0 || title.toString().equals("")) {
        	
        	title = ((String) fileName).substring(0, ((String) fileName).lastIndexOf('.'));
        	log.warn("****** Document: " + fileName.toString() + " missing title. Creating one. ******");
        	
        }
        
        log.debug("****** Node Name : " + fileName.toString() + " Title: "+ title );
        note.setTitle(title.toString());
        
        if(fileName != null)
        	log.debug("****** Node Name : " + fileName.toString() + " Title: "+title );
        
        description = props.get(ContentModel.PROP_DESCRIPTION);
        
        if(description!=null)
        	log.debug("****** Node Name : " + description.toString() );
        
        ContentData contentData = (ContentData) props.get(ContentModel.PROP_CONTENT);
        String originalMimeType = contentData.getMimetype();
        
        log.debug("****** MimeType : " + originalMimeType );
        
        ContentReader reader = contentService.getReader(nodeToPublish, ContentModel.PROP_CONTENT);
        
        if(originalMimeType.equalsIgnoreCase(TEXT_PLAIN)) {	
	        
        	// The content of an Evernote note is represented using Evernote Markup Language
	        // (ENML). The full ENML specification can be found in the Evernote API Overview
	        // at http://dev.evernote.com/documentation/cloud/chapters/ENML.php
        	String content = reader.getContentString();
            String enmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	            + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
	            + "<en-note>" 
	            + content
	            + "</en-note>";
	        note.setContent(enmlContent);
	            
        } else {
        	
            File file = new File("temp");
        	reader.getContent(file);
            String mimeType = originalMimeType; 
            
            // To include an attachment such as an image in a note, first create a Resource
            // for the attachment. At a minimum, the Resource contains the binary attachment 
            // data, an MD5 hash of the binary data, and the attachment MIME type. It can also 
            // include attributes such as filename and location.
            Resource resource = new Resource();
            
            try {
				resource.setData(readFileAsData(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
            
            resource.setMime(mimeType);
            ResourceAttributes attributes = new ResourceAttributes();
            attributes.setFileName(fileName.toString());
            resource.setAttributes(attributes);

            // Now, add the new Resource to the note's list of resources
            note.addToResources(resource);

            // To display the Resource as part of the note's content, include an <en-media>
            // tag in the note's ENML content. The en-media tag identifies the corresponding
            // Resource using the MD5 hash.
            String hashHex = bytesToHex(resource.getData().getBodyHash());
            
            // The content of an Evernote note is represented using Evernote Markup Language
            // (ENML). The full ENML specification can be found in the Evernote API Overview
            // at http://dev.evernote.com/documentation/cloud/chapters/ENML.php
            String enmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                + "<en-note>" 
                + "<span style=\"color:green;\">" +title+" : " +description+ "</span><br/>"
                + "<en-media type=\"" +mimeType+ "\" hash=\"" + hashHex + "\"/>"
                + "</en-note>";
            
            note.setContent(enmlContent);

        }
	    
        log.debug("****** Setting Content : " + originalMimeType );
        
        // Finally, send the new note to Evernote using the createNote method
        // The new Note object that is returned will contain server-generated
        // attributes such as the new note's unique GUID.
        try {
        	
	        String noteStoreUrl = channelProperties.get(PublishingModel.PROP_ASSET_URL).toString();
	        noteStoreUrl = getEncryptor().decrypt(PublishingModel.PROP_ASSET_URL, noteStoreUrl).toString();
	    	log.debug("****** noteStoreUrl: " + noteStoreUrl );
	        
	    	THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
	        TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
	        NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
	        		
	        SealedObject sealedAccessToken = (SealedObject) channelProperties.get(PublishingModel.PROP_OAUTH2_TOKEN);
	        log.debug("****** sealedAccessToken: " + sealedAccessToken );
	        
	        String accessToken = (String)getEncryptor().decrypt(PublishingModel.PROP_OAUTH2_TOKEN, sealedAccessToken);
	    	log.info("****** accessToken: " + accessToken );
	        Note createdNote = noteStore.createNote(accessToken, note);
	        
	        String newNoteGuid = createdNote.getGuid();
	        log.debug("****** newNoteGuid: " + newNoteGuid );
	        
	        //Determine if the noderef is the publish node or the original
	        List<AssociationRef> assRefs = nodeService.getTargetAssocs(nodeToPublish, PublishingModel.ASSOC_SOURCE);
	        if (assRefs != null && !assRefs.isEmpty()) {
	        	
	        	NodeRef source = assRefs.get(0).getTargetRef();
	        	nodeToPublish = source;
	        	
	        }
	        	
	    	nodeService.addAspect(nodeToPublish, EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, null);
	    	Set<QName> aspects = nodeService.getAspects(nodeToPublish);
	    	
	    	for(QName aspect : aspects){
	    		
	    		log.info("************ "+aspect.getLocalName()+" *************");
	    	
	    	}
	    	
	    	log.info("************ Applied Everfresco Aspect *************");
	    	
        } catch (Exception e){
        	
        	e.printStackTrace();
        	log.error("Error Creating Note: "+note.getTitle());
            
        }        
        
    }
    
    /**
     * Helper method to read the contents of a file on disk and create a new Data object.
     */
    private static Data readFileAsData(File file) throws Exception {

      // Read the full binary contents of the file
      FileInputStream in = new FileInputStream(file);
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

