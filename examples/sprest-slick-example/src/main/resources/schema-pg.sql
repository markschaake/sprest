-- Postgres SQL script to generate a database and tables usable by the application.
-- You may want to change the owner of the database - in this script the owner is `postgres`



-- Database: sprest_slick_example

-- DROP DATABASE sprest_slick_example;

CREATE DATABASE sprest_slick_example
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'C'
       LC_CTYPE = 'C'
       CONNECTION LIMIT = -1;


-- Table: todos

-- DROP TABLE todos;

CREATE TABLE todos
(
  id serial NOT NULL,
  text character varying(255) NOT NULL,
  is_complete boolean DEFAULT false,
  CONSTRAINT todos_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE todos
  OWNER TO postgres;


-- Table: reminders

-- DROP TABLE reminders;

CREATE TABLE reminders
(
  id serial NOT NULL,
  remind_at timestamp without time zone NOT NULL,
  title character varying(255) NOT NULL,
  body text,
  CONSTRAINT reminders_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE reminders
  OWNER TO postgres;
