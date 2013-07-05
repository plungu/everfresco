package com.support.model;

import org.alfresco.service.namespace.QName;

public class EverfrescoModel {
	//public static final QName ASPECT_EVERFRESCO_SYNCABLE = QName.createQName("http://www.socialguru.net/model/everfresco/1.0", "syncable");
	
	//Aspects
	public static final QName ASPECT_EVERFRESCO_SYNCABLE = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "syncable");
	public static final QName ASPECT_EVERFRESCO_PERSON = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "person");
	
	//Properties
	public static final QName PROPERTY_EVERFRESCO_USERNAME = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "username");
	public static final QName PROPERTY_EVERFRESCO_PASSWORD = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "password");
	public static final QName PROPERTY_EVERFRESCO_NODE_ID = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "nodeId");
	public static final QName PROPERTY_EVERFRESCO_LAST_SYNC = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "lastSync");
	public static final QName PROPERTY_EVERFRESCO_SYNCED = QName.createQName("http://www.alfresco.com/model/everfresco/1.0", "synced");
	 
//	private QName property;
// 
//	private EverfrescoModel(QName qname) {
//		property = qname;
//	}
// 
//	public QName getProperty() {
//		return property;
//	}
}
