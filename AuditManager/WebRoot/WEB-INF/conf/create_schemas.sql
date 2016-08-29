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
