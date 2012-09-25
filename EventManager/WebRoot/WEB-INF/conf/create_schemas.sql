create schema eventmanager authorization pasta;
create table eventmanager.emlsubscription (
   subscriptionId     bigserial  primary key,
   active             boolean    not null,
   creator            text       not null,
   scope              text,
   identifier         integer,
   revision           integer,
   url                text       not null
);
