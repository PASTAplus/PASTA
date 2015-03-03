CREATE SCHEMA authtoken AUTHORIZATION pasta;


CREATE TABLE authtoken.tokenstore (
  user_id VARCHAR(248) NOT NULL,          -- user id, the primary key
  token VARCHAR(1024) NOT NULL,           -- base64 encoded auth token
  date_created TIMESTAMP DEFAULT now(),   -- insertion/update date/time
  CONSTRAINT token_store_pk PRIMARY KEY (user_id)
);


CREATE TABLE authtoken.saved_data (
  user_id VARCHAR(248) NOT NULL,          -- user id
  scope VARCHAR(100) NOT NULL,            -- the scope
  identifier INT8 NOT NULL,               -- the identifier
  revision INT8 NOT NULL,                 -- the revision
  date_created TIMESTAMP DEFAULT now()    -- insertion date/time
);
