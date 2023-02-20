=============================
Standard Operating Procedures
=============================

The PASTA repository software stack is maintained and provided by EDI. Although
operational aspects of the repository are codified into software, many processes
are controlled through policy decisions of EDI. The following section attempts
to blend the software constraints into those dictated by policy with this set of
*Standard Operating Procedures*.

The EDI data repository follows a *metadata-driven* workflow that consumes
`Ecological Metadata Standard <https://eml.ecoinformatics.org/>`_ (EML) science
metadata, along with science data, and produces a data package that is archived
in the repository. That data package is then made available to the public
through the PASTA+ REST API.

.. toctree::
   :hidden:

   data_package_definition
   archive_life_cycle
