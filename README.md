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