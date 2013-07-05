  //TODO: understand the response from the webscript. How to capture and use the response. 
  //TODO: understand and explain how genericAction works
  (function() {
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onActionApplyEverFresco",
        fn: function support_onActionSync(file) {
        	//alert("Apply Everfresco "+file.displayName);
            this.modules.actions.genericAction(
            {
                success:
                {
                    message: this.msg("message.everfresco.sync.success", file.displayName, Alfresco.constants.USERNAME),
                    event:  {   name: "metadataRefresh"  }
                },
                failure:
                {
                    message: this.msg("message.everfresco.sync.failure", file.displayName, Alfresco.constants.USERNAME)
                },
                webscript:
                {
                    name: "everfresco/applyEverfresco.json?nodeRef={nodeRef}",
                    stem: Alfresco.constants.PROXY_URI,
                    method: Alfresco.util.Ajax.POST,
                    params:
                    {
                        nodeRef: file.nodeRef
                    }
                },
                config:
                {
                }

            });
        }
    });
  })();
  
  (function() {
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onActionRemoveEverFresco",
        fn: function support_onActionSync(file) {
        	//alert("Apply Everfresco "+file.displayName);
            this.modules.actions.genericAction(
            {
                success:
                {
                    message: this.msg("message.everfresco.sync.success", file.displayName, Alfresco.constants.USERNAME),
                    event:  {   name: "metadataRefresh"  }
                },
                failure:
                {
                    message: this.msg("message.everfresco.sync.failure", file.displayName, Alfresco.constants.USERNAME)
                },
                webscript:
                {
                    name: "everfresco/removeEverfresco.json?nodeRef={nodeRef}",
                    stem: Alfresco.constants.PROXY_URI,
                    method: Alfresco.util.Ajax.POST,
                    params:
                    {
                        nodeRef: file.nodeRef
                    }
                },
                config:
                {
                }

            });
        }
    });
  })();