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
  sha1_checksum CHAR(40),                                   -- SHA-1 checksum of this resource, a 40-character string
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


CREATE TABLE datapackagemanager.data_cache_registry (
  PACKAGE_ID VARCHAR(64),                                   -- package Id
  SCOPE VARCHAR(64),                                        -- scope
  IDENTIFIER INT8,                                          -- identifier
  REVISION INT8,                                            -- revision
  ENTITY_ID VARCHAR(256),                                   -- entity id
  ENTITY_NAME VARCHAR(256),                                 -- entity name
  DATA_FORMAT VARCHAR(64),                                  -- data format
  DATE_CREATED DATE,                                        -- date created
  UPDATE_DATE DATE                                          -- update date
);


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
