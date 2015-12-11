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

REST API
""""""""

`POST : https://pasta.lternet.edu/package/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/eml>`_

Description
"""""""""""

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
 
..   "**202** - Accepted", "The create data package request was accepted", "Transaction identifier", "*text/plain*"
..   "**401** - Unauthorized", "The user is not authorized to perform this operation.", "Error message", "*text/plain*"
..   "**405** - Method not allowed", "The specified HTTP method is not allowed for the requested resource", "Error message", "*text/plain*"
.. End: This section is commented out but saved for future development

Examples
""""""""
  
1. Using :command:`curl` to upload a new data package to PASTA where
   ``knb-lter-lno.1.1.xml`` is the filesystem EML document in XML format::

     curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
       -H "Content-Type: application/xml" --data-binary @knb-lter-lno.1.1.xml \
       -X POST https://pasta.lternet.edu/package/eml

.. _browse-and-discovery:

*Evaluate Data Package*
^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`POST : https://pasta.lternet.edu/package/evaluate/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/evaluate/eml>`_

*Update Data Package*
^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`PUT : https://pasta.lternet.edu/package/eml/{scope}/{identifier} <https://pasta.lternet.edu/package/docs/api#PUT%20:%20/eml/{scope}/{identifier}>`_

Browse and Discovery
--------------------

Describes web service methods for browsing and discovering data packages.

*Search Data Packages*
^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/search/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/search/eml>`_

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

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/eml/{scope}/{identifier}/{revision}>`_

*List Data Descendants*
^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/descendants/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/descendants/eml/{scope}/{identifier}/{revision}>`_

*List Data Sources*
^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/sources/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/sources/eml/{scope}/{identifier}/{revision}>`_

*List Data Package Identifiers*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}>`_

*List Data Package Revisions*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope}/{identifier} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}/{identifier}>`_

*List Data Package Scopes*
^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml>`_

*List Deleted Data Packages*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/deleted <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/deleted>`_

*List Service Methods*
^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/service-methods <https://pasta.lternet.edu/package/docs/api#GET%20:%20/service-methods>`_

.. _accessing:

Accessing Data Package Resources
--------------------------------

Describes web service methods for accessing data package resources such as data, metadata, and reports.

*Read Data Entity*
^^^^^^^^^^^^^^^^^^

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/eml/{scope}/{identifier}/{revision}/{entityId}>`_

.. _provenance:

Provenance
----------

Describes web service methods for tracking and generating provenance metadata.

*Add Provenance Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`PUT : https://pasta.lternet.edu/package/provenance/eml <https://pasta.lternet.edu/package/docs/api#PUT%20:%20/provenance/eml>`_

.. _event-notifications:

Event Notifications
-------------------

Describes web service methods for subscribing to and receiving data package event notifications.

.. _miscellaneous:

Miscellaneous Services
----------------------

Additional web service methods for working with data packages.

*Create Data Package Archive*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""

`POST : https://pasta.lternet.edu/package/archive/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#POST%20:%20/archive/eml/{scope}/{identifier}/{revision}>`_

*Is Authorized*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

REST API
""""""""
`GET : https://pasta.lternet.edu/package/authz <https://pasta.lternet.edu/package/docs/api#GET%20:%20/authz>`_
