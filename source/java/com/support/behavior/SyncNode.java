package com.support.behavior;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.ImporterModuleComponent;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

import com.support.model.EverfrescoModel;

public class SyncNode implements NodeServicePolicies.OnAddAspectPolicy, NodeServicePolicies.OnUpdateNodePolicy { 

	// Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private SiteService siteService;
    private AuthorityService authorityService;
    private ImporterModuleComponent importerModuleComponent;
    private Logger logger = Logger.getLogger(SyncNode.class);
    // Behaviours
    private Behaviour onUpdateNode;
    private Behaviour onAddAspect;
    
    
    public void init() {

		System.out.println("********* Initialize everfresco sync Behavior ********* ");
    	
        // Create behaviours
        this.onAddAspect = new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT);
        this.onUpdateNode = new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
        		EverfrescoModel.ASPECT_EVERFRESCO_SYNCABLE, this.onAddAspect);
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode"), 
        		ContentModel.TYPE_CONTENT, this.onUpdateNode);
    }
    
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		// TODO Auto-generated method stub
		System.out.println("********* everfresco sync Behavior on add aspect********* ");
    	sync(nodeRef);		
	}
    	
	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		// TODO Auto-generated method stub
		System.out.println("********* everfresco sync Behavior on update node ********* ");
    	sync(nodeRef);		
	}

	public void sync(NodeRef nodeRef) {
		
		return;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	public AuthorityService getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public ImporterModuleComponent getImporterModuleComponent() {
		return importerModuleComponent;
	}

	public void setImporterModuleComponent(
			ImporterModuleComponent importerModuleComponent) {
		this.importerModuleComponent = importerModuleComponent;
	}

}
