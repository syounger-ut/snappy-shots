# Snappy shots

Create shareable photo albums for your family & friends.

## Setup

The project is built with Sbt, Scala, and ScalaJS.

Compile:
```
$ sbt compile
$ npm install
```

Run the server:
```
# Default port 9000
$ sbt "snappyShots/run 8080"
```

Run the client:
```
# Requires two windows to develop
# Terminal one
$ sbt "~fastLinkJS"

#Terminal two
$ npm run dev # default port 5173
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

New migration files are added to `./jvm/conf/db/migration/snappy_shots`. The file must be prefixed with `Vxx__<script_name>.sql`. The `xx` is a version number, and must be unique and sequential.
