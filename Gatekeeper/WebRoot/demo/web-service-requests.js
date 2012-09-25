var Requests = {

   _relativeUrl : "/test/",

   _setUserAndPassword : function(requestForm) {
      requestForm.user.value = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
      requestForm.password.value = "S@ltL@ke";
   },

   _clearUserAndPassword : function(requestForm) {
      requestForm.user.value = "";
      requestForm.password.value = "";
   },

   _clearCookie : function(cookie) {
      var cookies = document.cookie.split(";")
      for (var i = 0; i < cookies.length; i++)
         eraseCookie(cookies[i].split("=")[0]);
   },

   initializeReadBasic : function(requestForm, cookie) {
      requestForm.description.value = "Perform a GET using basic authentication.";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._setUserAndPassword(requestForm);
      this._clearCookie(cookie);
   },

   initializeReadToken : function(requestForm) {
      requestForm.description.value = "Perform a GET using token authentication. Alternately perform a GET using an expired token (wait 60 seconds).";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._clearUserAndPassword(requestForm);
   },

   initializeReadMalformed : function(requestForm) {
      requestForm.description.value = "Perform a GET using a malformed token for authentication.";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._clearUserAndPassword(requestForm);
   },

   initializeReadPublic : function(requestForm, cookie) {
      requestForm.description.value = "Perform a GET using public authentication.";
      requestForm.relativeUrl.value = this._relativeUrl;
      requestForm.httpVerb.value = "GET";
      requestForm.contentType.value = "N/A";
      requestForm.entity.value = "";
      this._clearUserAndPassword(requestForm);
      this._clearCookie(cookie);
   }

}
