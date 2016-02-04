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

*Get Recent Uploads*
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Gets a list of zero or more audit records of either recently inserted or recently updated data packages, as specified in the request.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/recent-uploads <https://pasta.lternet.edu/audit/docs/api#GET%20:%20recent-uploads>`_
