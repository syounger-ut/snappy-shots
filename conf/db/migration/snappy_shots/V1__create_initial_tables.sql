CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email varchar(40) UNIQUE NOT NULL,
    password varchar(100) NOT NULL
);
