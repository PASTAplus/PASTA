Data Package Manager API
========================

The Data Package Manager API consists of six groups of PASTA web services:

#. :ref:`Upload and Evaluation <upload-and-evaluation>` of data packages
#. :ref:`Browse and Discovery <browse-and-discovery>` of data packages
#. :ref:`Listing <listing>` data packages
#. :ref:`Accessing <accessing>` data package resources
#. :ref:`Provenance <provenance>` tracking and metadata
#. Data package :ref:`Event Notifications <event-notifications>`
#. :ref:`Miscellaneous <miscellaneous>` data package services

.. _upload-and-evaluation:

Upload and Evaluation
---------------------

Describes web service methods for uploading (creating or updating) and evaluating data packages.

*Create Data Package*
^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Creates a new PASTA data package by providing the EML document describing
the data package to be created in the request message body and returning a
*transaction identifier* in the response message body as plain text; the
*transaction identifier* may be used in a subsequent call to
:ref:`Read Data Package Error <read-data-package-error>` to determine the operation status; see
:ref:`Read Data Package<read-data-package>` to obtain the data package resource map if the operation
completed successfully.

  
.. This section is commented out but saved for future development
.. :Rest Verb/URL: POST /package/eml
.. :Request body: The EML document in XML format
.. :MIME Type: *application/xml*
.. :Response(s):
.. .. csv-table::
..   :header: "Code", "Explanation", "Body", "MIME Type"
 
..   "**202** - Accepted", "The create data package request was accepted", "Transaction identifier", "*text/plain*"
..   "**401** - Unauthorized", "The user is not authorized to perform this operation.", "Error message", "*text/plain*"
..   "**405** - Method not allowed", "The specified HTTP method is not allowed for the requested resource", "Error message", "*text/plain*"
.. End: This section is commented out but saved for future development

REST API
""""""""

`POST : https://pasta.lternet.edu/package/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/eml>`_

Examples
""""""""
  
1. Using :command:`curl` to upload a new data package to PASTA where
   ``knb-lter-lno.1.1.xml`` is the filesystem EML document in XML format::

     curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
       -H "Content-Type: application/xml" --data-binary @knb-lter-lno.1.1.xml \
       -X POST https://pasta.lternet.edu/package/eml

.. _evaluate-data-package:

*Evaluate Data Package*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Evaluate Data Package operation, specifying the EML document describing the data package to be evaluated 
in the request message body, and returning a *transaction identifier* in the response message body as plain 
text; the *transaction identifier* may be used in a subsequent call to 
:ref:`Read Data Package Error <read-data-package-error>` to determine the 
operation status or to :ref:`Read Evaluate Report <read-evaluate-report>` to obtain the evaluate quality report.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/evaluate/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/evaluate/eml>`_

*Update Data Package*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Update Data Package operation, specifying the scope and identifier of the data package to be updated 
in the URI, along with the EML document describing the data package to be created in the request message 
body, and returning a *transaction identifier* in the response message body as plain text; the 
*transaction identifier* may be used in a subsequent call to 
:ref:`Read Data Package Error <read-data-package-error>`  to determine the operation status; 
see :ref:`Read Data Package<read-data-package>` to obtain the data package resource map if 
the operation completed successfully.

REST API
""""""""

`PUT : https://pasta.lternet.edu/package/eml/{scope}/{identifier} <https://pasta.lternet.edu/package/docs/api#PUT%20:%20/eml/{scope}/{identifier}>`_

.. _browse-and-discovery:

Browse and Discovery
--------------------

Describes web service methods for browsing and discovering data packages.

*Search Data Packages*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Searches data packages in PASTA using the specified Solr query as the query 
parameters in the URL. Search results are returned as XML. Detailed examples 
of Solr queries and their corresponding search results XML are shown below.
  
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

REST API
""""""""

`GET : https://pasta.lternet.edu/package/search/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/search/eml>`_

Solr Queries
""""""""""""

Solr queries are demonstrated in the examples below.

.. note::
   A full discussion of Solr query syntax is beyond the scope of this document. Documentation on this topic
   can be found online, for example, the `Apache Solr Wiki <https://wiki.apache.org/solr/>`_.

Searchable Fields
"""""""""""""""""

Documents in PASTA's Solr repository can be discovered based on metadata values stored in the following list
of searchable fields:

Single-value Fields:

* abstract
* begindate
* doi
* enddate
* funding
* geographicdescription
* id
* methods
* packageid
* pubdate
* responsibleParties
* scope
* singledate
* site
* taxonomic
* title

Multi-value Fields

* author
* coordinates
* keyword
* organization
* timescale

Search Results
""""""""""""""

Search results are returned in XML format. (See examples below.)

Examples
""""""""
  
1. Using :command:`curl` to query PASTA for all documents containing the term "Vernberg".
   In this example, all fields for matching documents are included in the search results
   (``fl=*``).
     
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

2. Using :command:`curl` to query PASTA for all documents containing the term "Vernberg"
   and limiting the returned fields to the "packageid" and "doi" fields (``fl=packageid,doi``)::
 
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

3. Using :command:`curl` to query PASTA for all documents containing the term "sediment"
   in the keyword field (``q=keyword:sediment``) and limiting the returned fields to the 
   keyword field (``fl=keyword``). Note that because the ``keyword`` field is a multi-value
   field, its elements are nested inside a parent ``keywords`` element.
     
   (Note: *For brevity, only two documents are displayed in the search results shown below.*)::

     curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax\
       &q=keyword:sediment&fq=-scope:ecotrends&fq=-scope:lter-landsat*&fl=keyword\
       &sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=10"

     <resultset numFound='71' start='0' rows='10'>
         <document>
             <keywords>
                 <keyword>sedimentation</keyword>
                 <keyword>NTL LTER</keyword>
                 <keyword>North Temperate Lakes - LTER</keyword>
                 <keyword>sediment</keyword>
                 <keyword>sediment deposition</keyword>
             </keywords>
         </document>
         <document>
             <keywords>
                 <keyword>Georgia</keyword>
                 <keyword>Sapelo Island</keyword>
                 <keyword>USA</keyword>
                 <keyword>GCE</keyword>
                 <keyword>Georgia Coastal Ecosystems</keyword>
                 <keyword>LTER</keyword>
                 <keyword>Sediment Monitoring</keyword>
                 <keyword>accumulation</keyword>
                 <keyword>elevation</keyword>
                 <keyword>erosion</keyword>
                 <keyword>freshwater</keyword>
                 <keyword>marshes</keyword>
                 <keyword>sea level</keyword>
                 <keyword>sediment elevation table</keyword>
                 <keyword>sediments</keyword>
                 <keyword>soils</keyword>
                 <keyword>Organic Matter</keyword>
             </keywords>
         </document>
     </resultset>

.. _listing:

Listing
-------

Describes web service methods for listing data packages.


*List Data Entities*
^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Entities operation, specifying the scope, identifier, and revision values to match in the URI.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/eml/{scope}/{identifier}/{revision}>`_

*List Data Descendants*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Descendants operation, specifying the scope, identifier, and revision values to match in the URI.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/descendants/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/descendants/eml/{scope}/{identifier}/{revision}>`_

*List Data Sources*
^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Sources operation, specifying the scope, identifier, and revision values to match in the URI.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/sources/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/sources/eml/{scope}/{identifier}/{revision}>`_

*List Data Package Identifiers*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Package Identifiers operation, specifying the scope value to match in the URI.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}>`_

*List Data Package Revisions*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Package Revisions operation, specifying the scope and identifier values to match in the URI. 
The request may be filtered by applying the modifiers "oldest" or "newest" to the "filter" query parameter.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope}/{identifier} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}/{identifier}>`_

*List Data Package Scopes*
^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Package Scopes operation, returning all scope values extant in the data package registry.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml>`_

*List Deleted Data Packages*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Deleted Data Packages operation, returning all document identifiers (excluding revision values) that 
have been deleted from the data package registry.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/deleted <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/deleted>`_

*List Service Methods*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Service Methods operation, returning a simple list of web service methods supported by the 
Data Package Manager web service.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/service-methods <https://pasta.lternet.edu/package/docs/api#GET%20:%20/service-methods>`_

*List Recent Uploads*
^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Recent Uploads operation, optionally specifying the upload type ("insert" or "update") and a 
maximum limit as query parameters in the URL. (See example below.)

REST API
""""""""

`GET : https://pasta.lternet.edu/package/uploads/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/uploads/eml>`_

.. _accessing:

Accessing Data Package Resources
--------------------------------

Describes web service methods for accessing data package resources such as data, metadata, and reports.

*Read Data Entity*
^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity operation, specifying the scope, identifier, revision, and entity identifier of 
the data entity to be read in the URI.

Revision may be specified as "newest" or "oldest" to retrieve data from the newest or oldest 
revision, respectively.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity ACL*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity ACL operation, specifying the scope, identifier, and revision of the data entity object 
whose Access Control List (ACL) is to be read in the URI, returning an XML string representing the ACL 
for the data entity. Please note: only a very limited set of users are authorized to use this service method.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/acl/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/acl/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity Checksum*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Checksum operation, specifying the scope, identifier, and revision of the data entity 
object whose checksum is to be read in the URI, returning a 40-character SHA-1 checksum value.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/checksum/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/checksum/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity Name*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Name operation, specifying the scope, identifier, revision, and entity identifier of 
the data entity whose name is to be read in the URI.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/name/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/name/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity Names*
^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Names operation, specifying the scope, identifier, and revision of the data package
whose data entity names are to be read in the URI, returning a newline-separated list of
entity identifiers and name values. Each line in the list contains an entity identifier
and its corresponding name value, separated by a comma. Only data entities that the user is
authorized to read are included in the list.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/name/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/name/eml/{scope}/{identifier}/{revision}>`_

*Read Data Entity Size*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Size operation, specifying the scope, identifier, and revision of the data entity 
object whose size is to be read in the URI, returning the size value (in bytes).

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/size/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/size/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity Sizes*
^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Sizes operation, specifying the scope, identifier, and revision of the data package
whose data entity sizes are to be read in the URI, returning a newline-separated list of
entity identifiers and size values (in bytes). Each line in the list contains an entity identifier
and its corresponding size value, separated by a comma. Only data entities that the user is
authorized to read are included in the list.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/size/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/size/eml/{scope}/{identifier}/{revision}>`_

.. _read-data-package:

*Read Data Package*
^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package operation, specifying the scope, identifier, and revision of the data package to 
be read in the URI, returning a resource graph with reference URLs to each of the metadata, data, 
and quality report resources that comprise the data package.

Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest revision, respectively.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}/{identifier}/{revision}>`_

*Read Data Package ACL*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package ACL operation, specifying the scope, identifier, and revision of the data package 
whose Access Control List (ACL) is to be read in the URI, returning an XML string representing the 
ACL for the data package. Please note: only a very limited set of users are authorized to use this service method.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/acl/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/acl/eml/{scope}/{identifier}/{revision}>`_

.. _read-data-package-archive:

*Read Data Package Archive*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Archive operation, specifying the *transaction identifier* of the data package archive 
to be read in the URI, returning the data package archive as a binary object in the ZIP file format.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/archive/eml/{scope}/{identifier}/{revision}/{transaction} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/archive/eml/{scope}/{identifier}/{revision}/{transaction}>`_

*Read Data Package DOI*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package DOI operation, specifying the scope, identifier, and revision of the data package 
DOI to be read in the URI, returning the canonical *Digital Object Identifier*.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/doi/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/doi/eml/{scope}/{identifier}/{revision}>`_

.. _read-data-package-error:

*Read Data Package Error*
^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Error operation, specifying the scope, identifier, revision, and *transaction identifier*
of the data package error to be read in the URI, returning the error message as plain text.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/error/eml/{transaction} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/error/eml/{transaction}>`_

*Read Data Package Report*
^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Report operation, specifying the scope, identifier, and revision of the data package 
quality report document to be read in the URI.

If an HTTP Accept header with value 'text/html' is included in the request, returns an HTML representation 
of the report. The default representation is XML.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/report/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/report/eml/{scope}/{identifier}/{revision}>`_

*Read Data Package Report ACL*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Report ACL operation, specifying the scope, identifier, and revision of the data 
package report whose access control list (ACL) is to be read in the URI, returning an XML string 
representing the ACL for the data package report resource. Please note: only a very limited set of 
users are authorized to use this service method.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/report/acl/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/report/acl/eml/{scope}/{identifier}/{revision}>`_

*Read Data Package Report Checksum*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Report Checksum operation, specifying the scope, identifier, and revision of the 
data package report object whose checksum is to be read in the URI, returning a 40 character SHA-1 checksum value.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/report/checksum/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/report/checksum/eml/{scope}/{identifier}/{revision}>`_

.. _read-evaluate-report:

*Read Evaluate Report*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Evaluate Report operation, specifying the *transaction identifier* of the evaluate quality report 
document to be read in the URI.

If an HTTP Accept header with value 'text/html' is included in the request, returns an HTML representation 
of the report. The default representation is XML.

See the :ref:`Evaluate Data Package <evaluate-data-package>` service method for information about how to 
obtain the *transaction identifier*.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/evaluate/report/eml/{transaction} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/evaluate/report/eml/{transaction}>`_

*Read Metadata*
^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata operation, specifying the scope, identifier, and revision of the EML document to be read in the URI.

Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest revision, respectively.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/eml/{scope}/{identifier}/{revision}>`_

*Read Metadata ACL*
^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata ACL operation, specifying the scope, identifier, and revision of the data package metadata 
whose Access Control List (ACL) is to be read in the URI, returning an XML string representing the ACL 
for the data package metadata resource. Please note: only a very limited set of users are authorized to 
use this service method.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/acl/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/acl/eml/{scope}/{identifier}/{revision}>`_

*Read Metadata Checksum*
^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata Checksum operation, specifying the scope, identifier, and revision of the metadata 
object whose checksum value is to be read in the URI, returning a 40 character SHA-1 checksum value.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/checksum/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/checksum/eml/{scope}/{identifier}/{revision}>`_

*Read Metadata Format*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata Format operation, specifying the scope, identifier, and revision of the metadata to be 
read in the URI, returning the metadata format type, e.g. "eml://ecoinformatics.org/eml-2.1.1"

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/format/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/format/eml/{scope}/{identifier}/{revision}>`_

.. _provenance:

Provenance
----------

Describes web service methods for tracking and generating provenance metadata.

*Get Provenance Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Add Provenance Metadata from Level-1 metadata in PASTA to an XML document containing a single methods 
element in the request message body.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/provenance/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/provenance/eml/{scope}/{identifier}/{revision}>`_

.. _event-notifications:

Event Notifications
-------------------

Describes web service methods for subscribing to and receiving data package event notifications.

*Create Event Subscription*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Create Event Subscription creates a new event subscription.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/event/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/event/eml>`_

*Delete Event Subscription*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Delete Event Subscription deletes the event subscription with the specified ID from the subscription 
database. After "deletion," the subscription might still exist in the subscription database, 
but it will be inactive - it will not conflict with future creation requests, it cannot be read, 
and it will not be notified of events.

REST API
""""""""

`DELETE : https://pasta.lternet.edu/package/event/eml/{subscriptionId} <https://pasta.lternet.edu/package/docs/api#DELETE%20:%20/event/eml/{subscriptionId}>`_

*Execute Event Subscription*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Execute Event Subscription operation, specifying the ID of the event subscription whose URL is to be executed. 
Used to execute a particular subscription in the event manager, via an HTTP POST request. Upon notification, 
the event manager queries its database for the subscription matching the specified *subscriptionId*. 
POST requests are then made (asynchronously) to the matching subscription.

The request headers must contain an authorization token. If the request is successful, an HTTP response 
with status code 200 'OK' is returned. If the request is unauthorized, based on the content of the 
authorization token and the current access control rule for event notification, status code 401 
'Unauthorized' is returned. If the request contains an error, status code 400 'Bad Request' is returned, 
with a description of the encountered error.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/event/eml/{subscriptionId} <https://pasta.lternet.edu/package/docs/api#POST%20:%20/event/eml/{subscriptionId}>`_

*Query Event Subscriptions*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Query Event Subscriptions operation, returns a list of the subscriptions whose attributes match those 
specified in the query string. If a query string is omitted, all subscriptions in the subscription 
database will be returned for which the requesting user is authorized to read. If query parameters are 
included, they are used to filter that set of subscriptions based on their attributes.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/event/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/event/eml>`_

*Get Event Subscription*
^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Get Event Subscription returns the event subscription with the specified ID.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/event/eml/{subscriptionId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/event/eml/{subscriptionId}>`_

*Get Event Subscription Schema*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Get Event Subscription Schema operation, returns the XML schema for event subscription creation request entities.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/event/eml/schema <https://pasta.lternet.edu/package/docs/api#GET%20:%20/event/eml/schema>`_

.. _miscellaneous:

Miscellaneous Services
----------------------

Additional web service methods for working with data packages.

*Create Data Package Archive*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Create Data Package Archive (Zip) operation, specifying the scope, identifier, and revision of the 
data package to be Zipped in the URI, and returning a *transaction identifier* in the response message 
body as plain text; the *transaction identifier* may be used in a subsequent call to 
:ref:`Read Data Package Error <read-data-package-error>` to determine the operation status or 
to :ref:`Read Data Package Archive <read-data-package-archive>` to obtain the Zip archive.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/archive/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#POST%20:%20/archive/eml/{scope}/{identifier}/{revision}>`_

*Is Authorized*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Is Authorized (to read resource) operation, determines whether the user as defined in the authentication 
token has permission to read the specified data package resource.

REST API
""""""""
`GET : https://pasta.lternet.edu/package/authz?resourceId={resource identifier} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/authz>`_
