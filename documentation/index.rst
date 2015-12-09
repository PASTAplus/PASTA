.. PASTA documentation master file, created by
   sphinx-quickstart on Fri Jun  5 12:05:43 2015.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

############################
User/Developer Documentation
############################

This website is the official documentation of the *LTER Network Information System* and, more specifically, 
the *Provenance Aware Synthesis Tracking Architecture* (PASTA). The site is divided into *five* primary 
categories designed to inform the user of different aspects of the LTER NIS and PASTA.

To assist the weary web traveler in deciding what category to peruse, a short synopsis of each section follows:

#. **LTER Network Information System:** Overview and motivation for the LTER Network Information System - the LTER Data Cooperative, data producers, and data consumers.

#. **LTER Data Portal Users Guide:** The LTER Data Portal is the official web-browser interface for users to upload, search, and download LTER data products. The Data Portal is considered a reference implementation of an application that utilizes the PASTA Application Program Interface (API).

#. **PASTA API:** Documentation for the PASTA API and general information and suggestions for software developers who would like to build an application that interacts with the PASTA API.

#. **PASTA Developers Guide:** Design documentation of the PASTA framework for developers who plan on extending, fixing, or refactoring the core components of PASTA and their associated web-services. Documentation for `DataONE`_'s Generic Member Node deployment and GMN's adapter for harvesting data from PASTA.

#. **General FAQ:** A list of the most commonly asked questions and their answers about the LTER NIS and PASTA.


.. _DataONE: https://dataone.org

.. toctree::
   :hidden:
   :numbered: 4
   :maxdepth: 3
   
   LTER NIS </doc_tree/lter_nis/index>
   LTER Data Portal </doc_tree/portal/index>
   /doc_tree/pasta_api/index
   /doc_tree/pasta_arch/index
   /doc_tree/help/index
   
:ref:`search`
