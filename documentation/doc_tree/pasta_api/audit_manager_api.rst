Audit Manager API
=================

Introduction
------------

The Audit Manager web service allows other PASTA web services to create, and users to access, PASTA audit logs.

Audit Manager Services
----------------------

*Create Audit Record*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Creates a new logged entry in the Audit Manager's logging database.

REST API
""""""""
`POST : https://pasta.lternet.edu/audit <https://pasta.lternet.edu/audit/docs/api#POST>`_


*Get Audit Record*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Gets a single audit record based on the audit identifier value specified in the path.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/report/{oid} <https://pasta.lternet.edu/audit/docs/api#GET%20:%20report/{oid}>`_

*Get Audit Report*
^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Gets an audit report, an XML list of zero or more audit records matching the query parameters as specified in the request.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/report <https://pasta.lternet.edu/audit/docs/api#GET%20:%20report>`_

*Get Audit Count*
^^^^^^^^^^^^^^^^^

Description
"""""""""""
Returns a count of the number of audit records matching the query parameters as specified in the request.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/count <https://pasta.lternet.edu/audit/docs/api#GET%20:%20count>`_

*Get DocId Reads*
^^^^^^^^^^^^^^^^^

Description
"""""""""""
Returns an XML-formatted list that summarizes all the successful reads (total reads and non-robot reads) for all the resources of
a given PASTA document ID, where a document ID is of the format "scope.identifier" (excludes revision).

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/reads/{scope}/{identifier} <https://pasta.lternet.edu/audit/docs/api#GET%20:%20reads/{scope}/{identifier}>`_

Examples
""""""""
  
1. Using :command:`curl` to list resource reads for document identifier "knb-lter-nwk.3120". Note that results from multiple revisions of "knb-lter-nwk.3120" are included in the output.::

     curl -X GET https://pasta.lternet.edu/audit/reads/edi/0

     <?xml version="1.0" encoding="UTF-8"?>
     <resourceReads>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/report/eml/knb-lter-nwk/3120/1</resourceId>
        <resourceType>report</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>1</revision>
        <totalReads>2</totalReads>
        <nonRobotReads>2</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/eml/knb-lter-nwk/3120/1</resourceId>
        <resourceType>dataPackage</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>1</revision>
        <totalReads>2</totalReads>
        <nonRobotReads>2</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/1/8d2b8f34c7316b6035b48de2b9ecc1aa</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>1</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/1/a9201a0755fc45ae514abb12469c03a0</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>1</revision>
        <totalReads>2</totalReads>
        <nonRobotReads>2</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/metadata/eml/knb-lter-nwk/3120/1</resourceId>
        <resourceType>metadata</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>1</revision>
        <totalReads>2</totalReads>
        <nonRobotReads>2</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/2/a9201a0755fc45ae514abb12469c03a0</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/eml/knb-lter-nwk/3120/2</resourceId>
        <resourceType>dataPackage</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/2/8d2b8f34c7316b6035b48de2b9ecc1aa</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     </resourceReads>
     
     
*Get PackageId Reads*
^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Returns an XML-formatted list that summarizes all the successful reads (total reads and non-robot reads) for all the resources of
a given PASTA package ID, where a package ID is of the format "scope.identifier.revision".

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/reads/{scope}/{identifier}/{revision} <https://pasta.lternet.edu/audit/docs/api#GET%20:%20reads/{scope}/{identifier}/{revision}>`_

Examples
""""""""
  
1. Using :command:`curl` to list resource reads for package identifier "knb-lter-nwk.3120.2".::

     curl -X GET https://pasta.lternet.edu/audit/reads/edi/0/1

     <?xml version="1.0" encoding="UTF-8"?>
     <resourceReads>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/2/a9201a0755fc45ae514abb12469c03a0</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/eml/knb-lter-nwk/3120/2</resourceId>
        <resourceType>dataPackage</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     <resource>
        <resourceId>https://pasta.lternet.edu/package/data/eml/knb-lter-nwk/3120/2/8d2b8f34c7316b6035b48de2b9ecc1aa</resourceId>
        <resourceType>data</resourceType>
        <scope>knb-lter-nwk</scope>
        <identifier>3120</identifier>
        <revision>2</revision>
        <totalReads>1</totalReads>
        <nonRobotReads>1</nonRobotReads>
     </resource>
     </resourceReads>


*Get Recent Uploads*
^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Gets a list of zero or more audit records of either recently inserted or recently updated data packages, as specified in the request.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/recent-uploads <https://pasta.lternet.edu/audit/docs/api#GET%20:%20recent-uploads>`_
