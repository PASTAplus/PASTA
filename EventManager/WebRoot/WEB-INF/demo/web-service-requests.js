var Requests = {

   _relativeUrl : "/eventmanager/subscription/eml",

   _setUserAndPassword : function(requestForm) {
      requestForm.user.value = "mickey";
      requestForm.password.value = "mouse";
   },

   initializeCreate : function(requestForm) {
      requestForm.description.value = "Create an event subscription.";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "POST";
      requestForm.contentType.value = "text/xml";

      // Getting example request entity
      xmlhttp = new XMLHttpRequest();
      xmlhttp.open("GET", '/eventmanager/demo/create_subscription.xml', false);
      xmlhttp.send();
      requestForm.entity.value = getResponseEntityAsString(xmlhttp);

      this._setUserAndPassword(requestForm);
   },

   initializeRead : function(requestForm) {
      requestForm.description.value = "Read an event subscription.";
      requestForm.relativeUrl.value = this._relativeUrl + "/{subscriptionId}";
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._setUserAndPassword(requestForm);
   },

   initializeReadAll : function(requestForm) {
      requestForm.description.value = "Read all event subscriptions.";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._setUserAndPassword(requestForm);
   },

   initializeDelete : function(requestForm) {
      requestForm.description.value = "Delete an event subscription.";
      requestForm.relativeUrl.value = this._relativeUrl + "/{subscriptionId}";
      requestForm.httpVerb.value = "DELETE";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._setUserAndPassword(requestForm);
   }

}
