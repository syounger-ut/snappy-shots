# Snappy shots

Create shareable photo albums for your family & friends.

## Setup

The project is built with Sbt and Scala.

Compile:
```
$ sbt compile
```

Run the app:
```
# Default port 9000
$ sbt run
```

## Tests

Run the project tests:
```
$ sbt test
```

Run test coverage:
```
$ sbt clean coverage test coverageReport
```

To see the coverage report, it is available in:
`./target/<scala-version>/scoverage-report/index.html`

## Database migrations

The project uses Flyway for database migrations. To run the migrations:
```
$ sbt flywayMigrate
```

New migration files are added to `./conf/db/migration/auth_service`. The file must be prefixed with `Vxx__<script_name>.sql`. The `xx` is a version number, and must be unique and sequential.
