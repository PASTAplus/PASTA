*Data Package Manager*
======================

The Data Package Manager consists of five distinct sections of the PASTA
web-service APIs:

#. *Upload* and *Evaluation* of data packages
#. *Browse* and *Discovery* of data packages
#. *Listing* data packages
#. *Provenance* tracking and metadata
#. Data package *Event* tracking

*Upload and Evaluation*
-----------------------

.. function:: package.createDataPackage(EML) -> transaction identifier

     Creates a new PASTA data package by providing the EML document describing
     the data package to be created in the request message body and returning a
     transaction identifier in the response message body as plain text; the
     transaction identifier may be used in a subsequent call to
     *readDataPackageError* to determine the operation status; see
     *readDataPackage* to obtain the data package resource map if the operation
     completed successfully.
     
     :Rest Verb/URL: POST /package/eml
     :Request body: The EML document in XML format
     :MIME Type: *application/xml*
     :Response(s):
     .. csv-table::
        :header: "Code", "Explanation", "Body", "MIME Type"
       
        "**202** - Accepted", "The create data package request was accepted", "Transaction identifier", "*text/plain*"
        "**401** - Unauthorized", "The user is not authorized to perform this operation.", "Error message", "*text/plain*"
        "**405** - Method not allowed", "The specified HTTP method is not allowed for the requested resource", "Error message", "*text/plain*"
        
     :Example(s):
        
     1. Using :command:`curl` to upload a new data package to PASTA where
        ``knb-lter-lno.1.1.xml`` is the filesystem EML document in XML format::
     
          curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
          -H "Content-Type: application/xml" --data-binary @knb-lter-lno.1.1.xml \
          -X POST https://pasta.lternet.edu/package/eml
