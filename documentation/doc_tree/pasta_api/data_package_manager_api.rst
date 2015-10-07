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

Describes methods for uploading and evaluating data packages.


   **Create Data Package**


     REST API: `POST : /package/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/eml>`_

     Creates a new PASTA data package by providing the EML document describing
     the data package to be created in the request message body and returning a
     transaction identifier in the response message body as plain text; the
     transaction identifier may be used in a subsequent call to
     *readDataPackageError* to determine the operation status; see
     *readDataPackage* to obtain the data package resource map if the operation
     completed successfully.
     
     .. This section is commented out but saved for future development
     .. :Rest Verb/URL: POST /package/eml
     .. :Request body: The EML document in XML format
     .. :MIME Type: *application/xml*
     .. :Response(s):
     .. .. csv-table::
     ..   :header: "Code", "Explanation", "Body", "MIME Type"
       
     ..  "**202** - Accepted", "The create data package request was accepted", "Transaction identifier", "*text/plain*"
     ..   "**401** - Unauthorized", "The user is not authorized to perform this operation.", "Error message", "*text/plain*"
     ..   "**405** - Method not allowed", "The specified HTTP method is not allowed for the requested resource", "Error message", "*text/plain*"
     .. End: This section is commented out but saved for future development
        
     :Example(s):
        
     1. Using :command:`curl` to upload a new data package to PASTA where
        ``knb-lter-lno.1.1.xml`` is the filesystem EML document in XML format::
     
          curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
          -H "Content-Type: application/xml" --data-binary @knb-lter-lno.1.1.xml \
          -X POST https://pasta.lternet.edu/package/eml

*Browse and Discovery*
----------------------

Describes methods for browsing and discovering data packages.

   **Search Data Packages**

     REST API: `GET : /package/search/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/search/eml>`_

     Searches data packages in PASTA using the specified Solr query as the query 
     parameters in the URL. Search results are returned as XML. Detailed example 
     of a Solr query and the search results XML it returns are shown below.
     
     .. This section is commented out but saved for future development
     .. :Rest Verb/URL: GET /package/search/eml
     .. :Request body: None
     .. :MIME Type: 
     .. :Response(s):
     .. .. csv-table::
     ..    :header: "Code", "Explanation", "Body", "MIME Type"
     ..   
     ..    "**200** - OK", "The search was successful", "A resultset XML document containing the search results", "*application/xml*"
     ..    "**400** - Bad Request", "The request message body contains an error, such as an improperly formatted path query string.", "Error message", "*text/plain*"
     ..    "**401** - Unauthorized", "The user is not authorized to perform this operation.", "Error message", "*text/plain*"
     ..    "**405** - Method not allowed", "The specified HTTP method is not allowed for the requested resource", "Error message", "*text/plain*"
     ..    "**500** - Internal Server Error", "The server encountered an unexpected condition which prevented it from fulfilling the request", "Error message", "*text/plain*"
     .. End: This section is commented out but saved for future development

     :Example(s):
        
     1. Using :command:`curl` to query PASTA for all documents containing the term "vernberg".
        In this example, all fields for matching documents are included in the search results
        ("fl=*").
        
        (Note: *For brevity, only one document is displayed in the search results shown below 
        and some of its content has been truncated.*)::
     
          curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax\
          &q=Vernberg&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=*\
          &sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"

          <resultset numFound='3' start='0' rows='10'>
              <document>
                  <abstract>This data package consists of Daily Water Sample Parameter,...</abstract>
                  <begindate>1981</begindate>
                  <doi>doi:10.6073/pasta/2b809c045fdd74a7cc12e8f31fc191eb</doi>
                  <enddate>1993</enddate>
                  <funding></funding>
                  <geographicdescription>North Inlet encompasses about 2,630 hectares of tidal...</geographicdescription>
                  <id>knb-lter-nin.8</id>
                  <docid>knb-lter-nin.8</docid>
                  <methods></methods>
                  <packageid>knb-lter-nin.8.1</packageid>
                  <pubdate>2013</pubdate>
                  <responsibleParties>NIN&#x2d;LTER
                      Vernberg, John
                      Blood, Elizabeth
                      Gardner, Robert
                  </responsibleParties>
                  <scope>knb-lter-nin</scope>
                  <singledate></singledate>
                  <site>nin</site>
                  <taxonomic></taxonomic>
                  <title>Suspended Sediment&#x2e; Daily Water Sample Parameter&#x2c; and Sediment...</title>
                  <authors>
                      <author>Vernberg, John</author>
                      <author>Blood, Elizabeth</author>
                      <author>Gardner, Robert</author>
                  </authors>
                  <spatialCoverage>
                      <coordinates>-79.2936 33.1925 -79.1042 33.357</coordinates>
                  </spatialCoverage>
                  <sources>
                  </sources>
                  <keywords>
                      <keyword>North Inlet Estuary</keyword>
                      <keyword>Baruch Institute</keyword>
                      <keyword>Georgetown, South Carolina</keyword>
                      <keyword>sediment</keyword>
                      <keyword>substances</keyword>
                      <keyword>ecology</keyword>
                      <keyword>community dynamics</keyword>
                      <keyword>populations</keyword>
                  </keywords>
                  <organizations>
                      <organization>NIN&#x2d;LTER</organization>
                  </organizations>
                  <timescales>
                  </timescales>
              </document>
          </resultset>

     2. Using :command:`curl` to query PASTA for all documents containing the term "vernberg"
        and limiting the returned fields to the "packageid" and "doi" fields (fl=packageid,doi)::
     
          curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax\
          &q=Vernberg&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=packageid,doi\
          &sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"

          <resultset numFound='3' start='0' rows='10'>
              <document>
                  <packageid>knb-lter-nin.1.1</packageid>
                  <doi>doi:10.6073/pasta/0675d3602ff57f24838ca8d14d7f3961</doi>
              </document>
              <document>
                  <packageid>knb-lter-nin.5.1</packageid>
                  <doi>doi:10.6073/pasta/3b69d867d7f6620bd2f47794804363d2</doi>
              </document>
              <document>
                  <packageid>knb-lter-nin.8.1</packageid>
                  <doi>doi:10.6073/pasta/2b809c045fdd74a7cc12e8f31fc191eb</doi>
              </document>
          </resultset>

*Listing*
---------

Describes methods for listing data packages.


*Provenance*
------------

Describes methods for tracking and generating provenance metadata.

*Event*
-------

Describes methods for subscribing to and receiving data package event notifications.