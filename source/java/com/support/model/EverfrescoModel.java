package com.support.model;

import org.alfresco.service.namespace.QName;

public class EverfrescoModel {
	
	//Namespace
	public static final String EVERFRESCO_NAMESPACE = "http://www.alfresco.com/model/everfresco/1.0";
	
	//Type
	public static final QName TYPE_EVERFRESCO_DELIVERY_CHANNEL = QName.createQName(
			EVERFRESCO_NAMESPACE, "DeliveryChannel");

	//Aspects
	public static final QName ASPECT_EVERFRESCO_SYNCABLE = QName.createQName(
			EVERFRESCO_NAMESPACE, "syncable");
	public static final QName ASPECT_EVERFRESCO_PERSON = QName.createQName(
		"EVERFRESCO_NAMESPACE", "person");
    public static final QName ASPECT_DELIVERY_CHANNEL = QName.createQName(
        	EVERFRESCO_NAMESPACE, "DeliveryChannelAspect");

	//Properties
	public static final QName PROPERTY_EVERFRESCO_USERNAME = QName.createQName(
		EVERFRESCO_NAMESPACE, "username");
	public static final QName PROPERTY_EVERFRESCO_PASSWORD = QName.createQName(
		EVERFRESCO_NAMESPACE, "password");
	public static final QName PROPERTY_EVERFRESCO_NODE_ID = QName.createQName(
		EVERFRESCO_NAMESPACE, "nodeId");
	public static final QName PROPERTY_EVERFRESCO_LAST_SYNC = QName.createQName(
		EVERFRESCO_NAMESPACE, "lastSync");
//	public static final QName PROPERTY_EVERFRESCO_SYNCED = QName.createQName(
//		EVERFRESCO_NAMESPACE, "synced");
	public static final QName PROPERTY_EVERFRESCO_CONSUMER_KEY = QName.createQName(
		EVERFRESCO_NAMESPACE, "consumerKey");
	public static final QName PROPERTY_EVERFRESCO_CONSUMER_SECRET = QName.createQName(
		EVERFRESCO_NAMESPACE, "consumerSecret");	
	public static final QName PROPERTY_EVERFRESCO_USE_SANDBOX = QName.createQName(
			EVERFRESCO_NAMESPACE, "useSandbox");	
	
}
