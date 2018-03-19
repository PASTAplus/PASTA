create schema auditmanager;
create sequence auditmanager.eventlog_oid_seq;
create table auditmanager.eventlog (
   oid numeric default nextval('auditmanager.eventlog_oid_seq') primary key,
   entryTime timestamp not null,
   service varchar(32) not null,
   category varchar(8) not null,
   serviceMethod varchar(128),
   entryText text,
   resourceId varchar(128),
   statusCode numeric,
   userid varchar(128),
   groups varchar(512),
   authSystem varchar(128)
);
CREATE INDEX entrytime_idx ON auditmanager.eventlog ( entryTime );
CREATE INDEX servicemethod_idx ON auditmanager.eventlog ( serviceMethod );
CREATE INDEX resourceid_idx ON auditmanager.eventlog ( resourceId );
CREATE INDEX userid_idx ON auditmanager.eventlog ( userid );
CREATE INDEX statuscode_idx ON auditmanager.eventlog ( statusCode );
CREATE INDEX category_idx ON auditmanager.eventlog ( category );

CREATE TYPE auditmanager.resource_type AS ENUM ('archive', 'data', 'dataPackage', 'metadata', 'report');
CREATE TABLE auditmanager.resource_reads (
   resource_id varchar(128) primary key,
   resource_type auditmanager.resource_type NOT NULL,        -- resource type
   scope VARCHAR(100) NOT NULL,                              -- the scope
   identifier INT8 NOT NULL,                                 -- the identifier
   revision INT8 NOT NULL,                                   -- the revision
   total_reads INT8 default 0,                               -- the total number of reads
   non_robot_reads INT8 default 0                            -- reads not by a robot
);
CREATE INDEX resource_id_idx ON auditmanager.resource_reads ( resource_id );
CREATE INDEX scope_idx ON auditmanager.resource_reads ( scope );

