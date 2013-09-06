CREATE SCHEMA authtoken AUTHORIZATION pasta;


CREATE TABLE authtoken.tokenstore (
  user_id VARCHAR(248) NOT NULL,          -- user id, the primary key
  token VARCHAR(1024) NOT NULL,           -- base64 encoded auth token
  date_created TIMESTAMP DEFAULT now(),   -- insertion/update date/time
  CONSTRAINT token_store_pk PRIMARY KEY (user_id)
);
