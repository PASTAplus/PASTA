create schema eventmanager authorization pasta;
create sequence eventmanager.subscription_id_seq;
create table eventmanager.emlsubscription (
   subscription_id    numeric    default nextval('eventmanager.subscription_id_seq') primary key,
   date_created       timestamp  not null,
   active             boolean    not null,
   creator            text       not null,
   scope              text,
   identifier         integer,
   revision           integer,
   url                text       not null
);
