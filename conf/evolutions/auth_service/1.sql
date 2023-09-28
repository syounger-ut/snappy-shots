-- Users schema

-- !Ups

CREATE TABLE users (
    id integer PRIMARY KEY,
    name varchar(40)
);

-- !Downs

DROP TABLE users;
