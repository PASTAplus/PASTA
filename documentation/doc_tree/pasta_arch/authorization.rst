=============
Authorization
=============

Authorization is the process to determine whether a user or group may access (or execute) a protected resource. In the PASTA+ environment, a protected resource consists of either (1) a physical object (e.g., data or metadata files residing on the file-system) or (2) an API method. Authorization is based on the access control rule for the protected resource and the user requesting access to that resource. All users and groups are denied access to protected resources unless provided explicit "allow" access through an access control rule.

Access Control Rules
--------------------

The access control rule is a declarative statement that describes the permissions and associated principals for a specific protected resource. The rule itself is declared as an **allow** or a **deny** rule, meaning that the following rule definition explicitly permits or blocks a level of access to the resource, respectively. Permissions are defined as an hierarchical enumeration of **read**, **write**, or **all** (the permission **changePermisssion** is equivalent to **all**) privileges; for an **allow** rule, **read** permission has the least privilege, while **all** permission has the most privilege (this hierarchy is reversed for **deny** rules). Principals are simple string values that map to the unique identifier of an :doc:`authenticated user </doc_tree/pasta_arch/gatekeeper>`. For example, a common user identifier used in PASTA+ is the form of an LDAP distinguished name: ``uid=ucarroll,o=EDI,dc=edirepository,dc=org``. The principal may also be a group identifier, ``authenticated``, or role, such as the anonymous user ``public``.

Access control rules in the PASTA+ environment are specified using the syntax of the `"access element" <https://knb.ecoinformatics.org/#external//emlparser/docs/eml-2.1.1/./eml-access.html>`_ as declared in the Ecological Metadata Language (EML) version 2.1.1. In fact, access rules for physical objects must be declared in a valid EML document that describes the data package that is uploaded to the EDI data repository. EML is specified as an XML schema, and the corresponding schema definition for the *access element* is graphically depicted below:

.. image:: images/access_rule_schema.png

Access rules may also declare the order of which **allow** or **deny** rules are processed; in other words, should the authorization processor process all **allow** rules first, followed by **deny** rules, or process in the reverse order. Processing **order** is declared in the attributes section of the XML and can be defined as either *allowFirst* or *denyFirst*. A required attribute of the access rule is the **authSystem**, which declares the authorization system for which this rule is appropriate. An example access rule follows::

 <access order="allowFirst" authSystem="EDI">
    <allow>
        <principal>uid=ucarroll,o=EDI,dc=edirepository,dc=org</principal>
        <permission>all</permission>
    </allow>
    <allow>
        <principal>public</principal>
        <permission>read</permission>
    </allow>
 </access>
