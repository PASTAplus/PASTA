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
^^^^^^^^^^^^^^^^^^^^^^^

Description
"""""""""""
Gets a list of zero or more audit records matching the query parameters as specified in the request.

REST API
""""""""

`GET : https://pasta.lternet.edu/audit/report <https://pasta.lternet.edu/audit/docs/api#GET%20:%20report>`_
