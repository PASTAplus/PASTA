Data Package Manager API
========================

The Data Package Manager API consists of six groups of PASTA web services:

#. :ref:`Upload and Evaluation <upload-and-evaluation>` of data packages
#. :ref:`Browse and Discovery <browse-and-discovery>` of data packages
#. :ref:`Listing <listing>` data packages
#. :ref:`Accessing <accessing>` data package resources
#. :ref:`Provenance <provenance>` tracking and metadata
#. Data package :ref:`Event Notifications <event-notifications>`
#. Data package :ref:`Identifier Reservations <reservations>`
#. :ref:`Journal Citations <journal-citations>` services
#. :ref:`System Monitoring <system-monitoring>` services
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

An optional query parameter, "useChecksum", can be appended to the URL. When specified, 
the useChecksum query parameter directs the server to determine whether it can use an
existing copy of a data entity from a previous revision of the data package based on
matching a metadata-documented checksum value (MD5 or SHA-1) to the checksum of the
existing copy. If a match is found, the server will skip the upload of the data entity from
the remote URL and instead use its matching copy. 

Please Note: Specifying "useChecksum" can save time by eliminating data uploads, but clients
should take care to ensure that metadata-documented checksum values are accurate and up to date.

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

An optional query parameter, "useChecksum", can be appended to the URL. When specified, 
the useChecksum query parameter directs the server to determine whether it can use an
existing copy of a data entity from a previous revision of the data package based on
matching a metadata-documented checksum value (MD5 or SHA-1) to the checksum of the
existing copy. If a match is found, the server will skip the upload of the data entity from
the remote URL and instead use its matching copy. 

Please Note: Specifying "useChecksum" can save time by eliminating data uploads, but clients
should take care to ensure that metadata-documented checksum values are accurate and up to date.

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
* projectTitle
* relatedProjectTitle
* timescale

Search Results
""""""""""""""

Search results are returned in XML format. (See examples below.)

Examples
""""""""
  
1. Using :command:`curl` to query PASTA for all documents containing the term "Vernberg",
   excluding documents with scope "ecotrends" (``fq=-scope:ecotrends``) and also
   excluding documents with a scope that begins with the substring "lter-landsat" 
   (``fq=-scope:lter-landsat*``). In this example, all fields for matching documents 
   are included in the search results (``fl=*``).
     
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

2. Using :command:`curl` to query PASTA for all documents with scope "knb-lter-nwt"
   containing the terms "plant" and "nitrogen" as keywords, and limiting the returned fields 
   to the "packageid", "doi", and "keyword" fields and only the first two
   matches (``rows=2``). Note that because the ``keyword`` field is a multi-value
   field, its elements are nested inside a parent ``keywords`` element.::
 
     curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax\
       &q=keyword:plant+AND+keyword:nitrogen&fq=scope:knb-lter-nwt\
       &fl=packageid,doi,keyword&rows=2"

    <resultset numFound='3' start='0' rows='2'>
    <document>
        <packageid>knb-lter-nwt.50.1</packageid>
        <doi></doi>
        <keywords>
            <keyword>aboveground</keyword>
            <keyword>nitrogen pool</keyword>
            <keyword>plant nitrogen concentration</keyword>
            <keyword>tissue nitrogen</keyword>
            <keyword>Biogeochemistry</keyword>
            <keyword>plant production</keyword>
            <keyword>live</keyword>
            <keyword>dead</keyword>
            <keyword>saddle</keyword>
            <keyword>dry meadow</keyword>
            <keyword>moist meadow</keyword>
            <keyword>wet meadow</keyword>
            <keyword>Niwot Ridge LTER</keyword>
            <keyword>NWT</keyword>
            <keyword>biomass</keyword>
            <keyword>vegetation</keyword>
            <keyword>litter</keyword>
        </keywords>
    </document>
    <document>
        <packageid>knb-lter-nwt.137.2</packageid>
        <doi>doi:10.6073/pasta/571f5c624b400498563be31e9a41e74f</doi>
        <keywords>
            <keyword>NWT</keyword>
            <keyword>Niwot Ridge LTER Site</keyword>
            <keyword>LTER</keyword>
            <keyword>Colorado</keyword>
            <keyword>K+</keyword>
            <keyword>Krummholz</keyword>
            <keyword>leeward</keyword>
            <keyword>nitrogen</keyword>
            <keyword>plant species</keyword>
            <keyword>plant species richne</keyword>
            <keyword>tree island</keyword>
            <keyword>tundra</keyword>
            <keyword>windward</keyword>
        </keywords>
    </document>
    </resultset>   
       
3. Using :command:`curl` to query PASTA for all documents containing the term "sediment" in the
   title or the term "disturbance" in the keyword field (``q=title:sediment+OR+keyword:disturbance``) 
   and limiting the returned fields to the packageid and keyword (``fl=packageid,keyword``) with up to
   1000 matches (``rows=1000``). Note that because the ``keyword`` field is a multi-value
   field, its elements are nested inside a parent ``keywords`` element.
     
   (Note: *For brevity, only two matching documents are displayed in the search results shown below.*)::

     curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax\
       &q=title:sediment+OR+keyword:disturbance&fl=packageid,keyword\
       &sort=score,desc&sort=packageid,asc&debug=false&start=0&rows=1000"

     <resultset numFound='12248' start='0' rows='1000'>
        <document>
           <packageid>knb-lter-jrn.210228001.53</packageid>
           <keywords>
              <keyword>LTAR</keyword>
              <keyword>LTER</keyword>
              <keyword>Disturbance</keyword>
              <keyword>Soil</keyword>
              <keyword>ongoing</keyword>
              <keyword>Aeolian (Wind Related)</keyword>
              <keyword>wind</keyword>
           </keywords>
        </document>
        <document>
           <packageid>knb-lter-vcr.241.2</packageid>
           <keywords>
              <keyword>Disturbance</keyword>
              <keyword>System State/Condition</keyword>
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
Data descendants are data packages that are known to be derived, in whole or in part, from the specified 
source data package.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/descendants/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/descendants/eml/{scope}/{identifier}/{revision}>`_

Examples
""""""""
  
1. Using :command:`curl` to list data descendants of a data package::

     curl -X GET https://pasta.lternet.edu/package/descendants/eml/knb-lter-xyz/1/1

     <?xml version="1.0" encoding="UTF-8"?>
     <dataDescendants>
         <dataDescendant>
             <packageId>edi.9999.1</packageId>
             <title>Fictitious Title of a PASTA Data Set</title>
             <url>https://pasta.lternet.edu/package/metadata/eml/edi/9999/1</url>
         </dataDescendant>
     </dataDescendants>

*List Data Sources*
^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Sources operation, specifying the scope, identifier, and revision values in the URI.
For each data source, its package identifier, title, and URL values are included (if applicable) as
documented in the metadata for the specified data package. Data sources can be either 
internal or external to PASTA. Internal data sources include a "packageId" value and a URL to the 
source metadata. For data sources external to PASTA, the "packageId" element will be empty 
and a URL value may or not be documented.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/sources/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/sources/eml/{scope}/{identifier}/{revision}>`_

Examples
""""""""
  
1. Using :command:`curl` to list data sources of a data package::

     curl -X GET https://pasta.lternet.edu/package/sources/eml/edi/9999/1

     <?xml version="1.0" encoding="UTF-8"?>
     <dataSources>
         <dataSource>
             <packageId>knb-lter-xyz.1.1</packageId>
             <title>A multi-scaled geospatial and temporal database</title>
             <url>https://pasta.lternet.edu/package/metadata/eml/knb-lter-xyz/1/1</url>
         </dataSource>
         <dataSource>
             <packageId></packageId>
             <title>Fictitious Title of an External Data Set</title>
             <url>https://someplace.elsewhere.edu/some-metadata.xml</url>
         </dataSource>
     </dataSources>

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

*List User Data Packages*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List all data packages (including their revision values) uploaded to the repository by
a particular user, specified by a distinguished name. Data packages that were uploaded
by the specified user but have since been deleted are excluded from the list.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/user/{dn} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/deleted>`_

Examples
""""""""
  
1. Using :command:`curl` to list all (undeleted) data packages uploaded by user ucarroll with distinguished name uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org::

     curl -X GET https://pasta.lternet.edu/package/user/uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org
     
     knb-lter-lno.1.1
     knb-lter-nwk.1865.1
     knb-lter-nwk.1865.2
     knb-lter-nwk.3135.1


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

*List Recent Changes*
^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Recent Changes operation, listing all data package insert, update, and
delete operations, optionally specifying the date and time to and/or from which the 
changes should be listed. An optional scope value can be specified to filter
results for a particular data package scope (e.g. scope=edi).
If "fromDate" and "toDate" are omitted, lists the complete set of changes recorded in PASTA'a resource
registry. If a "scope" value is omitted, results are returned for all
data package scopes that exist in the resource registry. Multiple instances of
the scope parameter are not supported (only the last scope value specified will be used). 
The list of changes is returned in XML format. Inserts and updates are recorded
in "dataPackageUpload" elements, while deletes are recorded in "dataPackageDelete"
elements. (See example below)

REST API
""""""""

`GET : https://pasta.lternet.edu/package/changes/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/changes/eml>`_

Examples
""""""""
  
1. Using :command:`curl` to list data packages that PASTA is working on uploading::

     curl -X GET https://pasta.lternet.edu/package/changes/eml?fromDate=2017-02-10T12:00:00&toDate=2017-02-11T12:00:00&scope=knb-lter-nwk

     <dataPackageChanges>
        <dataPackageUpload>
            <packageId>knb-lter-nwk.1225.1</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1225</identifier>
            <revision>1</revision>
            <serviceMethod>createDataPackage</serviceMethod>
            <date>2017-02-10 16:48:56.368</date>
        </dataPackageUpload>
        <dataPackageDelete>
            <packageId>knb-lter-nwk.1225.1</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1225</identifier>
            <revision>1</revision>
            <serviceMethod>deleteDataPackage</serviceMethod>
            <date>2017-02-10 16:49:06.83</date>
        </dataPackageDelete>
        <dataPackageUpload>
            <packageId>knb-lter-nwk.1226.1</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1226</identifier>
            <revision>1</revision>
            <serviceMethod>createDataPackage</serviceMethod>
            <date>2017-02-10 16:49:53.201</date>
        </dataPackageUpload>
        <dataPackageUpload>
            <packageId>knb-lter-nwk.1226.2</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1226</identifier>
            <revision>2</revision>
            <serviceMethod>updateDataPackage</serviceMethod>
            <date>2017-02-10 16:50:22.802</date>
        </dataPackageUpload>
        <dataPackageDelete>
            <packageId>knb-lter-nwk.1226.1</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1226</identifier>
            <revision>1</revision>
            <serviceMethod>deleteDataPackage</serviceMethod>
            <date>2017-02-10 16:50:51.111</date>
        </dataPackageDelete>
        <dataPackageDelete>
            <packageId>knb-lter-nwk.1226.2</packageId>
            <scope>knb-lter-nwk</scope>
            <identifier>1226</identifier>
            <revision>2</revision>
            <serviceMethod>deleteDataPackage</serviceMethod>
            <date>2017-02-10 16:50:51.111</date>
        </dataPackageDelete>
     </dataPackageChanges>


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

Read Data Entity ACL operation, specifying the scope, identifier, revision, and entity identifier of the data entity object 
whose Access Control List (ACL) is to be read in the URI, returning an XML string representing the ACL 
for the data entity. Please note: only a very limited set of users are authorized to use this service method.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/acl/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/acl/eml/{scope}/{identifier}/{revision}/{entityId}>`_

*Read Data Entity Checksum*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Checksum operation, specifying the scope, identifier, revision, and entity identifier of the data entity 
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

*Read Data Entity Resource Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Entity Resource Metadata operation, specifying the scope, identifier, revision, and entity identifier of the data entity object 
whose resource metadata is to be read in the URI, returning an XML string representing the resource metadata 
for the data entity.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/data/rmd/eml/{scope}/{identifier}/{revision}/{entityId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/data/rmd/eml/{scope}/{identifier}/{revision}/{entityId}>`_

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
be read in the URI, returning a resource map with reference URLs to each of the metadata, data, 
and quality report resources that comprise the data package.

Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest revision, respectively.

When the "?ore" query parameter is appended to the request URL, an OAI-ORE compliant resource map in RDF-XML format is returned.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/eml/{scope}/{identifier}/{revision}>`_

Examples
""""""""
  
1. Using :command:`curl` to read a data package resource map::

     curl -X GET https://pasta.lternet.edu/package/eml/knb-lter-nin/1/1

     https://pasta-d.lternet.edu/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2
     https://pasta-d.lternet.edu/package/metadata/eml/knb-lter-nin/1/1
     https://pasta-d.lternet.edu/package/report/eml/knb-lter-nin/1/1
     https://pasta-d.lternet.edu/package/eml/knb-lter-nin/1/1

2. Using :command:`curl` to read a data package resource map, using the "?ore" query parameter to specify that the resource map should be returned as an OAI-ORE compliant RDF-XML document::

     curl -X GET https://pasta.lternet.edu/package/eml/knb-lter-nin/1/1?ore
     
     <?xml version="1.0" encoding="UTF-8"?>
     <rdf:RDF
        xmlns:cito="http://purl.org/spar/cito/"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:dcterms="http://purl.org/dc/terms/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:ore="http://www.openarchives.org/ore/terms/"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rdfs1="http://www.w3.org/2001/01/rdf-schema#"
     >
       <rdf:Description rdf:about="https://pasta-d.lternet.edu/package/eml/knb-lter-nin/1/1">
         <rdf:type rdf:resource="http://www.openarchives.org/ore/terms/ResourceMap"/>
         <dcterms:created>2013-05-10T22:27:29.763</dcterms:created>
         <dcterms:modified>2013-05-10T22:27:29.763</dcterms:modified>
         <dcterms:creator rdf:resource="http://edirepository.org"/>
         <ore:describes rdf:resource="https://pasta-d.lternet.edu/package/eml/knb-lter-nin/1/1#aggregation"/>
         <dcterms:identifier>doi:10.6073/pasta/3bcc89b2d1a410b7a2c678e3c55055e1</dcterms:identifier>
         <dc:format>application/rdf+xml</dc:format>
       </rdf:Description>
       <rdf:Description rdf:about="https://pasta-d.lternet.edu/package/eml/knb-lter-nin/1/1#aggregation">
         <rdf:type rdf:resource="http://www.openarchives.org/ore/terms/Aggregation"/>
         <ore:aggregates rdf:resource="https://pasta-d.lternet.edu/package/eml/metadata/knb-lter-nin/1/1"/>
         <ore:aggregates rdf:resource="https://pasta-d.lternet.edu/package/eml/data/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2"/>
         <ore:aggregates rdf:resource="https://pasta-d.lternet.edu/package/eml/report/knb-lter-nin/1/1"/>
       </rdf:Description>
       <rdf:Description rdf:about="https://pasta-d.lternet.edu/package/eml/metadata/knb-lter-nin/1/1">
         <dcterms:identifier>https://pasta-d.lternet.edu/package/eml/metadata/knb-lter-nin/1/1</dcterms:identifier>
         <cito:documents rdf:resource="https://pasta-d.lternet.edu/package/eml/data/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2"/>
         <cito:documents rdf:resource="https://pasta-d.lternet.edu/package/eml/report/knb-lter-nin/1/1"/>
       </rdf:Description>
       <rdf:Description rdf:about="https://pasta-d.lternet.edu/package/eml/data/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2">
         <dcterms:identifier>https://pasta-d.lternet.edu/package/eml/data/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2</dcterms:identifier>
         <cito:isDocumentedBy rdf:resource="https://pasta-d.lternet.edu/package/eml/metadata/knb-lter-nin/1/1"/>
       </rdf:Description>
       <rdf:Description rdf:about="https://pasta-d.lternet.edu/package/eml/report/knb-lter-nin/1/1">
         <dcterms:identifier>https://pasta-d.lternet.edu/package/eml/report/knb-lter-nin/1/1</dcterms:identifier>
         <cito:isDocumentedBy rdf:resource="https://pasta-d.lternet.edu/package/eml/metadata/knb-lter-nin/1/1"/>
       </rdf:Description>
       <rdf:Description rdf:about="http://edirepository.org">
         <foaf:name>Environmental Data Initiative</foaf:name>
         <foaf:mbox>info@edirepository.org</foaf:mbox>
       </rdf:Description>
       <rdf:Description rdf:about="http://www.openarchives.org/ore/terms/ResourceMap">
         <rdfs1:label>ResourceMap</rdfs1:label>
         <rdfs1:isDefinedBy>http://www.openarchives.org/ore/terms/</rdfs1:isDefinedBy>
       </rdf:Description>
       <rdf:Description rdf:about="http://www.openarchives.org/ore/terms/Aggregation">
         <rdfs1:label>Aggregation</rdfs1:label>
         <rdfs1:isDefinedBy>http://www.openarchives.org/ore/terms/</rdfs1:isDefinedBy>
       </rdf:Description>
     </rdf:RDF>


*Read Data Package From DOI*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package From DOI operation, specifying the DOI of the data package to 
be read in the URI, returning a resource map with reference URLs to each of the metadata, data, 
and quality report resources that comprise the data package.

The DOI is specified in the "shoulder", "pasta", and "md5" path segments of the URI (see example below).

When the "?ore" query parameter is appended to the request URL, an OAI-ORE compliant resource map in RDF-XML format is returned.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/doi/{shoulder}/{pasta}/{md5} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/doi/{shoulder}/{pasta}/{md5}>`_

Examples
""""""""

1. Using :command:`curl` to read a data package resource map by specifying the DOI
as three path segments in the URL::

     curl -X GET https://pasta.lternet.edu/package/doi/doi:10.6073/pasta/0675d3602ff57f24838ca8d14d7f3961

     https://pasta.lternet.edu/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2
     https://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1
     https://pasta.lternet.edu/package/report/eml/knb-lter-nin/1/1
     https://pasta.lternet.edu/package/eml/knb-lter-nin/1/1
     
     The three path segments of the DOI are separated by forward slashes. In the above example, they are:
     a. shoulder value, in this example, "doi:10.6073". (For test DOIs, the shoulder is "doi:10.5072".)
     b. pasta literal, in this example, "pasta". (For test DOIs, the pasta literal is "FK2".)
     c. md5 value, in this example, "0675d3602ff57f24838ca8d14d7f3961". Each data package has a unique md5 value.

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

Examples
""""""""
  
1. Using :command:`curl` to read a data package archive and redirect the output to a file::

    curl -s -X GET https://pasta.lternet.edu/package/archive/eml/knb-lter-nin/1/1/archive_knb-lter-nin.1.1_15494687022457218 > knb-lter-nin.1.1.zip


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

*Read Data Package Resource Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Resource Metadata operation, specifying the scope, identifier, and revision of the data package 
whose resource metadata is to be read in the URI, returning an XML string representing the 
resource metadata for the data package.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/rmd/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/rmd/eml/{scope}/{identifier}/{revision}>`_

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

*Read Data Package Report Resource Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Data Package Report Resource Metadata operation, specifying the scope, identifier, and revision of the data 
package report whose resource metadata is to be read in the URI, returning an XML string 
representing the resource metadata for the data package report resource.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/report/rmd/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/report/rmd/eml/{scope}/{identifier}/{revision}>`_

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

Read Metadata (EML) operation, specifying the scope, identifier, and revision of the EML document to be read in the URI.

Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest revision, respectively.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/eml/{scope}/{identifier}/{revision}>`_

*Read Metadata Dublin Core*
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata (Dublin Core) operation, specifying the scope, identifier, and revision of the Dublin Core metadata to be read in the URI.

Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest revision, respectively.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/dc/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/dc/{scope}/{identifier}/{revision}>`_

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

*Read Metadata Resource Metadata*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Read Metadata Resource Metadata operation, specifying the scope, identifier, and revision of the data package metadata 
whose resource metadata is to be read in the URI, returning an XML string representing the resource metadata
for the data package metadata resource.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/metadata/rmd/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/metadata/rmd/eml/{scope}/{identifier}/{revision}>`_

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

.. _reservations:

Identifier Reservation Services
-------------------------------

Web service methods whereby an end user may reserve data package identifiers for future upload to PASTA.


*Create Reservation*
^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Create Reservation operation, creates a new reservation in PASTA for the 
specified user on the next reservable identifier for the specified scope. The 
integer value of the reserved identifier (as assigned by PASTA) is returned in 
the web service response body. User authentication is required.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/reservations/eml/{scope} <https://pasta.lternet.edu/package/docs/api#POST%20:%20/reservations/eml/{scope}>`_

Examples
""""""""
  
1. Using :command:`curl` to reserve the next available identifier for the specified scope ("edi")::

     curl -i -u uid=jsmith,o=LTER,dc=ecoinformatics,dc=org:SOME_PASSWORD -X POST "https://pasta.lternet.edu/package/reservations/eml/edi"

     HTTP/1.1 201 Created

     12

     In the example above, user "jsmith" creates a reservation on the next
     available identifier for the "edi" scope. PASTA assigns the value "12",
     meaning that data package identifier "edi.12" is now reserved for future 
     upload by user "jsmith". Only user "jsmith" will be allowed to upload
     data packages with identifier "edi.12".


*Delete Reservation*
^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Delete Reservation operation, deletes an existing reservation from PASTA. The same
user who originally authenticated to create the reservation must authenticate to delete it,
otherwise a "401 Unauthorized" response is returned. When successfully deleted,
a "200 OK" response is returned, and the integer value of the deleted 
reservation identifier value is returned in the web service response body.

REST API
""""""""

`DELETE : https://pasta.lternet.edu/package/reservations/eml/{scope}/{identifier} <https://pasta.lternet.edu/package/docs/api#DELETE%20:%20/reservations/eml/{scope}/{identifier}>`_

Examples
""""""""
  
1. Using :command:`curl` to delete an existing reservation for scope ("edi") and identifier ("12")::

     curl -i -u uid=jsmith,o=LTER,dc=ecoinformatics,dc=org:SOME_PASSWORD -X DELETE "https://pasta.lternet.edu/package/reservations/eml/edi/12"

     HTTP/1.1 200 OK

     12

     In the example above, user "jsmith" deletes a reservation on document
     identifier "edi.12". Because user "jsmith" previously created this
     reservation, only user "jsmith" is allowed to delete it.


*List Active Reservations*
^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Active Reservations operation, lists the set of data package identifiers that 
users have actively reserved in PASTA. Note that data package identifiers that have been 
successfully uploaded into PASTA are no longer considered active reservations and 
thus are not included in this list.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/reservations/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/reservations/eml>`_


Examples
""""""""
  
1. Using :command:`curl` to list active reservations::

     curl -X GET https://pasta.lternet.edu/package/reservations/eml

     <reservations>
       <reservation>
         <docid>edi.99</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 14:11:48.234</dateReserved>
       </reservation>
       <reservation>
         <docid>edi.100</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 14:14:49.205</dateReserved>
       </reservation>
       <reservation>
         <docid>edi.7</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 16:03:44.48</dateReserved>
       </reservation>
       <reservation>
         <docid>edi.10</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 16:16:29.321</dateReserved>
       </reservation>
       <reservation>
         <docid>edi.11</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 16:16:49.304</dateReserved>
       </reservation>
       <reservation>
         <docid>edi.12</docid>
         <principal>uid=LNO,o=LTER,dc=ecoinformatics,dc=org</principal>
         <dateReserved>2017-01-23 16:16:51.857</dateReserved>
       </reservation>
     </reservations>


*List Reservation Identifiers*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Reservation Identifiers operation, lists the set of numeric identifiers for 
the specified scope that end users have actively reserved for future upload to PASTA.
The numeric identifiers are listed one per line.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/reservations/eml/{scope} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/reservations/eml/{scope}>`_


Examples
""""""""
  
1. Using :command:`curl` to list reservation identifiers for a specified scope::

     curl -X GET https://pasta.lternet.edu/package/reservations/eml/edi

     7
     10
     11
     12
     99
     100

.. _system-monitoring:

System Monitoring Services
--------------------------

Web service methods for monitoring the state of data packages being processed in PASTA.


*List Working On*
^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Working On operation, lists the set of data packages that PASTA is currently working on inserting or updating. 
(Note that data packages currently being evaluated by PASTA are not included in the list.)

REST API
""""""""

`GET : https://pasta.lternet.edu/package/workingon/eml <https://pasta.lternet.edu/package/docs/api#GET%20:%20/workingon/eml>`_

Examples
""""""""
  
1. Using :command:`curl` to list data packages that PASTA is working on uploading::

     curl -X GET https://pasta.lternet.edu/package/workingon/eml

     <workingOn>
       <dataPackage>
         <packageId>edi.9.1</packageId>
         <startDate>2016-12-21 10:43:24.923</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nin.1.2</packageId>
         <startDate>2016-12-08 16:58:29.307</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nin.1.4</packageId>
         <startDate>2016-12-08 17:20:59.998</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1836.1</packageId>
         <startDate>2016-12-12 16:54:09.269</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1837.1</packageId>
         <startDate>2016-12-12 16:55:05.453</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1837.2</packageId>
         <startDate>2016-12-12 16:55:36.232</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1838.1</packageId>
         <startDate>2016-12-12 16:58:01.403</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1844.1</packageId>
         <startDate>2017-01-23 16:41:32.349</startDate>
       </dataPackage>
       <dataPackage>
         <packageId>knb-lter-nwk.1849.1</packageId>
         <startDate>2017-01-24 13:37:29.09</startDate>
       </dataPackage>
     </workingOn>

.. _journal-citations:


Journal Citation Services
--------------------------

Web service methods for creating, reading, and deleting journal citation entries associated with data packages.


*Create Journal Citation*
^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Create Journal Citation operation, creates a new journal citation entry in PASTA. An XML document containing metadata for the journal citation must be supplied in the HTTP request body.

REST API
""""""""

`POST : https://pasta.lternet.edu/package/citation/eml <https://pasta.lternet.edu/package/docs/api#POST%20:%20/citation/eml>`_

Examples
""""""""
  
1. Using :command:`curl` to create a journal citation with the XML metadata stored in a file::  
  
    curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
       -H "Content-Type: application/xml" --data-binary @journalCitation.xml \
       -X POST https://pasta.lternet.edu/package/citation/eml

  Where file journalCitation.xml contains the following XML: ::

    <?xml version="1.0" encoding="UTF-8"?>
    <journalCitation>   
        <packageId>edi.1000.1</packageId>
        <articleDoi>10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae</articleDoi>
        <articleTitle>Tree Survey in Southern Arizona</articleTitle>
        <articleUrl>http://treejournal.com/articles/12345</articleUrl>
        <journalTitle>The Tree Journal</journalTitle>
        <relationType>IsCitedBy</relationType>
    </journalCitation>


*Delete Journal Citation*
^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Delete Journal Citation operation, deletes the journal citation entry with the specified ID from the journal citation table.
Requires authentication by the owner of the journal citation entry.

REST API
""""""""

`DELETE : https://pasta.lternet.edu/package/citation/eml/{journalCitationId} <https://pasta.lternet.edu/package/docs/api#DELETE%20:%20/citation/eml/{journalCitationId}>`_

Examples
""""""""
  
1. Using :command:`curl` to delete the journal citation with identifier value 15, owned by user "ucarroll"::  

    curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" \
         -X DELETE https://pasta.lternet.edu/package/citation/eml/15


*Get Journal Citation*
^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

Get Journal Citation operation, returns an XML metadata document for the journal citation with the specified integer ID value.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/citation/eml/{journalCitationId} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/citation/eml/{journalCitationId}>`_

Examples
""""""""
  
1. Using :command:`curl` to access the journal citation with identifier value 15::  

    curl -X GET https://pasta.lternet.edu/package/citation/eml/15

    <?xml version="1.0" encoding="UTF-8"?>
    <journalCitation>
        <journalCitationId>15</journalCitationId>
        <principalOwner>uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</principalOwner>
        <dateCreated>2017-12-21T14:28:26.235</dateCreated>
        <packageId>edi.1000.1</packageId>
        <articleDoi>10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae</articleDoi>
        <articleTitle>Tree Survey in Southern Arizona</articleTitle>
        <articleUrl>http://treejournal.com/articles/12345</articleUrl>
        <journalTitle>The Tree Journal</journalTitle>
        <relationType>IsCitedBy</relationType>
    </journalCitation>


*List Data Package Citations*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Data Package Citations operation, specifying the data package scope, identifier, and revision values to match in the URI.
Returns a list of journal citations as an XML metadata document.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/citations/eml/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/citation/eml/{scope}/{identifier}/{revision}>`_

Examples
""""""""
  
1. Using :command:`curl` to access the list of journal citations for the data package with package ID "edi.1000.1" ::  

    curl -X GET https://pasta.lternet.edu/package/citations/eml/edi/1000/1

    <?xml version="1.0" encoding="UTF-8"?>
    <journalCitations>
        <journalCitation>
            <journalCitationId>15</journalCitationId>
            <principalOwner>uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</principalOwner>
            <dateCreated>2017-12-21T14:28:26.235</dateCreated>
            <packageId>edi.1000.1</packageId>
            <articleDoi>10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae</articleDoi>
            <articleTitle>Tree Survey in Southern Arizona</articleTitle>
            <articleUrl>http://treejournal.com/articles/12345</articleUrl>
            <journalTitle>The Tree Journal</journalTitle>
            <relationType>IsCitedBy</relationType>
        </journalCitation>
        <journalCitation>
            <journalCitationId>18</journalCitationId>
            <principalOwner>uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</principalOwner>
            <dateCreated>2017-12-26T14:28:26.235</dateCreated>
            <packageId>edi.1000.1</packageId>
            <articleDoi>10.5072/FK2/07bccc7b0cb2a2d5f6fe672cb4b36dea</articleDoi>
            <articleTitle>Mesquites of the Southwest</articleTitle>
            <articleUrl>http://swtrees.com/articles/68999</articleUrl>
            <journalTitle>Trees of the Southwest</journalTitle>
            <relationType>IsCitedBy</relationType>
        </journalCitation>
    </journalCitations>


*List Principal Owner Citations*
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""

List Principal Owner Citations operation, returns journal citations metadata for all entries owned by the specified principal owner.

REST API
""""""""

`GET : https://pasta.lternet.edu/package/citations/eml/{principalOwner} <https://pasta.lternet.edu/package/docs/api#GET%20:%20/citation/eml/{principalOwner}>`_

Examples
""""""""
  
1. Using :command:`curl` to access the list of journal citations owned by user "ucarroll" ::  

    curl -X GET https://pasta.lternet.edu/package/citations/eml/uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org

    <?xml version="1.0" encoding="UTF-8"?>
    <journalCitations>
        <journalCitation>
            <journalCitationId>15</journalCitationId>
            <principalOwner>uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</principalOwner>
            <dateCreated>2017-12-21T14:28:26.235</dateCreated>
            <packageId>edi.1000.1</packageId>
            <articleDoi>10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae</articleDoi>
            <articleTitle>Tree Survey in Southern Arizona</articleTitle>
            <articleUrl>http://treejournal.com/articles/12345</articleUrl>
            <journalTitle>The Tree Journal</journalTitle>
            <relationType>IsCitedBy</relationType>
        </journalCitation>
        <journalCitation>
            <journalCitationId>18</journalCitationId>
            <principalOwner>uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</principalOwner>
            <dateCreated>2017-12-26T14:28:26.235</dateCreated>
            <packageId>edi.1000.1</packageId>
            <articleDoi>10.5072/FK2/07bccc7b0cb2a2d5f6fe672cb4b36dea</articleDoi>
            <articleTitle>Mesquites of the Southwest</articleTitle>
            <articleUrl>http://swtrees.com/articles/68999</articleUrl>
            <journalTitle>Trees of the Southwest</journalTitle>
            <relationType>IsCitedBy</relationType>
        </journalCitation>
    </journalCitations>


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
