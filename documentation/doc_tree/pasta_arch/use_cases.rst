=================
Primary Use Cases
=================

User Management
---------------

High-level description of the user management use cases.

Use-case attributes
~~~~~~~~~~~~~~~~~~~

* Users are identified by a unique identifier, referred to as the
  user's *UID*.
* Groups consist of zero or more users and are identified by a
  unique identifier, referred to as the group's *GID*.
* Users may belong to a given group (groups are semantically
  equivalent to roles).
* Users may belong to one or more groups simultaneously.
* Users may map different UIDs to a single "real" identity.

Use-case scenarios
~~~~~~~~~~~~~~~~~~

#. Create a new user
#. Create a new group (not implemented)
#. Add a user to a group (not implemented)
#. Remove a user from a group (not implemented)
#. Delete a user
#. Delete a group (not implemented)
#. List all users in the system
#. List all groups in the system (not implemented)
#. List users in a group (not implemented)
#. List groups a user belongs to (not implemented)
#. A user logs into the system
#. A user logs out of the system
#. A user changes their password
#. A user changes their email address
#. A user changes their real name

Data Package Management
-----------------------

High-level description of the data package management use cases:

Use-case attributes
~~~~~~~~~~~~~~~~~~~

* A data package is identified by a unique identifier, referred to as the
  package's *PID*.
* A data package is associated with a single user, referred to as the
  *principal owner* of the package.
* A data package must be described by an Ecological Metadata Language
  (EML) document.
* A data package must describe one or more data objects.
* A "private" data package is one that is accessible only to the principal
  owner of the data package.
* Accessibility to a data package must be explicitly granted within the EML
  document associated with the data package.
* A "publicly accessible" data package is one that is accessible to
  all users of the system.

Use-case scenarios
~~~~~~~~~~~~~~~~~~

#. Create a new user

Audit Log Management
--------------------

High-level description of the audit log management use cases:

Use-case attributes
~~~~~~~~~~~~~~~~~~~

* An audit log entry contains the following attributes and attribute types:

    #. oid integer (primary key),
    #. entryTime timestamp,
    #. service string,
    #. category string,
    #. serviceMethod string,
    #. entryText string,
    #. resourceId string,
    #. statusCode integer,
    #. userid string,
    #. userAgent string,
    #. groups string,
    #. authSystem string

Use-case scenarios
~~~~~~~~~~~~~~~~~~

#. Create a new user

