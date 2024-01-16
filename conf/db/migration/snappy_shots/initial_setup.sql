-- **This should not be run as part of the auto flyway migrations, it is only for manual use when initially setting up the database.**
-- This script will create the database and user, and grant the user permissions to the database.

-- Login as the postgres user.
-- psql --username=postgres --dbname=postgres

-- Create an admin user role for the snappy shots database.
CREATE ROLE snappy_shots_admin CREATEDB CREATEROLE LOGIN PASSWORD '<enter password here>';

-- Login as the admin user.
-- psql --username=snappy_shots_admin --dbname=postgres

-- Once logged in, create the snappy shots database.
SELECT 'CREATE DATABASE snappy_shots'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'snappy_shots')\gexec