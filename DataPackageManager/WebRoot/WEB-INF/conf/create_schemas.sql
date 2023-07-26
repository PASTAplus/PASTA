CREATE SCHEMA datapackagemanager AUTHORIZATION pasta;

CREATE TYPE datapackagemanager.resource_type AS ENUM ('data', 'dataPackage', 'metadata', 'report');
CREATE TABLE datapackagemanager.resource_registry (
  resource_id VARCHAR(350) NOT NULL,                        -- resource id, the primary key
  doi VARCHAR(256),                                         -- digital object identifier (DOI)
  resource_type datapackagemanager.resource_type NOT NULL,  -- resource type
  resource_location VARCHAR(500),                           -- root location for this resource (referenced by entity resources only)
  package_id VARCHAR(100) NOT NULL,                         -- the EML 'packageId' attribute value
  scope VARCHAR(100) NOT NULL,                              -- the scope
  identifier INT8 NOT NULL,                                 -- the identifier
  revision INT8 NOT NULL,                                   -- the revision
  entity_id VARCHAR(256),                                   -- the entity id (as appears in the URL)
  entity_name VARCHAR(256),                                 -- the entity name (as appears in the EML)
  principal_owner VARCHAR(250) NOT NULL,                    -- the principal who owns this resource
  date_created TIMESTAMP NOT NULL,                          -- creation date/time
  date_deactivated TIMESTAMP,                               -- deactivation date/time; NULL indicates still active
  filename TEXT,                                            -- the filename of this resource
  md5_checksum CHAR(32),                                    -- MD5 checksum of this resource, a 32-character hexadecimal string
  sha1_checksum CHAR(40),                                   -- SHA-1 checksum of this resource, a 40-character hexadecimal string
  format_type VARCHAR(100),                                 -- the metadata format type, e.g. 'eml://ecoinformatics.org/eml-2.1.1'
  mime_type VARCHAR(100),                                   -- the mime type, e.g. 'text/csv'
  resource_size BIGINT,                                     -- the size of the resource in bytes
  data_format TEXT,                                         -- the data format as determined by parsing the EML
  CONSTRAINT resource_registry_pk PRIMARY KEY (resource_id)
);

CREATE TYPE datapackagemanager.order_type AS ENUM ('allowFirst', 'denyFirst');
CREATE TYPE datapackagemanager.access_type AS ENUM ('allow', 'deny');
CREATE TYPE datapackagemanager.permission AS ENUM ('read', 'write', 'changePermission');
CREATE SEQUENCE datapackagemanager.access_matrix_id_seq;
CREATE TABLE datapackagemanager.access_matrix (
  access_matrix_id INT8 default nextval('datapackagemanager.access_matrix_id_seq'), -- access matrix id, the primary key
  resource_id VARCHAR(350) NOT NULL,                                                -- resource id, a foreign key
  principal VARCHAR(250) NOT NULL,                                                  -- the principal for whom this access rule applies
  access_type datapackagemanager.access_type NOT NULL,                              -- the EML access type ('allow', 'deny')
  access_order datapackagemanager.order_type NOT NULL,                              -- the EML order attribute ('allowFirst', 'denyFirst')
  permission datapackagemanager.permission NOT NULL,                                -- the EML permission ('read', 'write', 'changePermission')
  CONSTRAINT access_matrix_pk PRIMARY KEY (access_matrix_id),
  CONSTRAINT access_matrix_resource_id_fk FOREIGN KEY (resource_id) REFERENCES datapackagemanager.resource_registry
);
CREATE INDEX resource_id_idx ON datapackagemanager.access_matrix ( resource_id );
CREATE INDEX principal_idx ON datapackagemanager.access_matrix ( principal );

CREATE SEQUENCE datapackagemanager.subscription_id_seq;
CREATE TABLE datapackagemanager.emlsubscription (
   subscription_id    numeric    default nextval('datapackagemanager.subscription_id_seq') primary key,
   date_created       timestamp  not null,
   active             boolean    not null,
   creator            text       not null,
   scope              text,
   identifier         integer,
   revision           integer,
   url                text       not null
);


CREATE TABLE datapackagemanager.prov_matrix (
  DERIVED_ID TEXT NOT NULL,                           -- packageId of derived data package
  DERIVED_TITLE TEXT NOT NULL,                        -- title of derived data package
  SOURCE_ID TEXT,                                     -- packageId of source data package
  SOURCE_TITLE TEXT NOT NULL,                         -- title of source data package
  SOURCE_URL TEXT                                     -- online URL of source data package
);


CREATE TYPE datapackagemanager.relation_type AS ENUM ('IsCitedBy', 'IsDescribedBy', 'IsReferencedBy');
CREATE SEQUENCE datapackagemanager.journal_citation_id_seq;
CREATE TABLE datapackagemanager.journal_citation (
  JOURNAL_CITATION_ID NUMERIC  DEFAULT NEXTVAL('datapackagemanager.journal_citation_id_seq') PRIMARY KEY,
  ARTICLE_DOI     VARCHAR(256),                       -- digital object identifier (DOI) of the journal article
  ARTICLE_TITLE   TEXT,                               -- title of the journal article
  ARTICLE_URL     TEXT NOT NULL,                      -- a URL to the journal article, possibly derived from the article DOI value
  PRINCIPAL_OWNER VARCHAR(250) NOT NULL,              -- distinguished name of the creator of this entry
  DATE_CREATED    TIMESTAMP    NOT NULL,              -- date that the identifier was reserved
  JOURNAL_TITLE   TEXT,                               -- name of the journal
  PACKAGE_ID      VARCHAR(100) NOT NULL,               -- packageId of source data package in PASTA
  RELATION_TYPE datapackagemanager.relation_type NOT NULL, -- type of relation between data package subject and citation object
  PUB_DATE        DATE                                -- journal publication date
);


CREATE TABLE datapackagemanager.journal_citation_author
(
    id                  SERIAL PRIMARY KEY,
    journal_citation_id INTEGER NOT NULL CONSTRAINT FK_JOURNAL_CITATION REFERENCES DATAPACKAGEMANAGER.JOURNAL_CITATION,
    given               VARCHAR(32),
    family              VARCHAR(32),
    suffix              VARCHAR(8),
    sequence            SMALLINT NOT NULL,
    orcid               VARCHAR(16)
);


CREATE TABLE datapackagemanager.working_on (
  scope VARCHAR(100) NOT NULL,                        -- the scope
  identifier INT8 NOT NULL,                           -- the identifier
  revision INT8 NOT NULL,                             -- the revision
  start_date TIMESTAMP NOT NULL,                      -- initiated date/time
  service_method VARCHAR(32),                         -- service method, e.g. 'evaluateDataPackage'
  end_date TIMESTAMP,                                 -- completed date/time
  interrupted BOOLEAN default false   -- set to true if end_date is NULL at server startup 
);

CREATE TABLE datapackagemanager.reservation (
   principal          VARCHAR(250) NOT NULL,              -- the principal for whom this reservation is made
   scope              VARCHAR(100) NOT NULL,              -- the scope of the reserved identifier
   identifier         INT8         NOT NULL,              -- the value of the reserved identifier
   date_reserved      timestamp    NOT NULL,              -- date that the identifier was reserved
   date_uploaded      timestamp,                          -- date that the data package was uploaded
   CONSTRAINT PK_RESERVATION PRIMARY KEY (SCOPE, IDENTIFIER)  -- two-column primary key
);
