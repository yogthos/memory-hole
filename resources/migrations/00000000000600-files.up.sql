CREATE TABLE files
(file_id serial NOT NULL,
 name  VARCHAR(150) UNIQUE NOT NULL,
 type  VARCHAR(50) NOT NULL,
 create_date timestamp not null default (now() at time zone 'utc'),
 data  BYTEA,
 CONSTRAINT pk_files PRIMARY KEY (file_id)
 ) WITH ( OIDS=FALSE );


