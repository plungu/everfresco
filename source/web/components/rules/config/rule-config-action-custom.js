/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * RuleConfigAction.
 *
 * @namespace Alfresco
 * @class Alfresco.RuleConfigAction
 */
(function()
{

	if (typeof EverFresco == "undefined" || !EverFresco)
	{
	   var EverFresco = {};
	}
	
   /**
    * YUI Library aliases
    */
//   var Dom = YAHOO.util.Dom,
//      Selector = YAHOO.util.Selector,
//      Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
//    var $html = Alfresco.util.encodeHTML,
//       $hasEventInterest = Alfresco.util.hasEventInterest;
   
   EverFresco.RuleConfigActionCustom = function(htmlId)
   {
	   EverFresco.RuleConfigAction.superclass.constructor.call(this, htmlId);

      // Re-register with our own name
      this.name = "EverFresco.RuleConfigAction";
      Alfresco.util.ComponentManager.reregister(this);

      // Instance variables
      this.customisations = YAHOO.lang.merge(this.customisations, EverFresco.RuleConfigActionCustom.superclass.customisations);
      this.renderers = YAHOO.lang.merge(this.renderers, Alfresco.RuleConfigActionCustom.superclass.renderers);
      return this;
   };

   YAHOO.extend(Alfresco.RuleConfigAction, Alfresco.RuleConfig,
   {

      /**
       * CUSTOMISATIONS
       */

      customisations:
      {

    	 DropZone:
         {
            text: function(configDef, ruleConfig, configEl)
            {
               // Display as path
               this._getParamDef(configDef, "destination-folder")._type = "path";

               return configDef;
            },
            edit: function(configDef, ruleConfig, configEl)
            {
               // Hide parameters since we are using a custom ui
               this._hideParameters(configDef.parameterDefinitions);

               // Make parameter renderer create a "Destination" button that displays an destination folder browser
               configDef.parameterDefinitions.splice(0,0,
               {
                  type: "arca:destination-dialog-button",
                  displayLabel: this.msg("label.to"),
                  _buttonLabel: this.msg("button.select-folder"),
                  _destinationParam: "destination-folder"
               });

               return configDef;
            }
         }
      }
   });

})();
