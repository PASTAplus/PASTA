Test suite for PASTA

1) knb-lter-nin.x.y are the documents for the North Inlet LTER data;
   knb-lter-nin.0.y is a time-series incremental build-up of data from 1982 to
   1992 - this is series is good for testing revision updates.

2) knb-lter-atz.x.y is a specific series for testing authorization.
   - knb-lter-atz.1.1 allows access for user "public" to all resources.
   - knb-lter-atz.2.1 removes the user "public" from ACLs.
   - knb-lter-atz.3.1 explicityly "denys" the user "public" in ACLs.
   - knb-lter-atz.4.1 removes the user "public" from the data entity ACL.
