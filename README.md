# Green Bunny

This project is extremely young and documentation is under heavy development. Nothing to see
here yet, move along.

This section will be updated as the API and documentation mature.


## Maven Artefacts (Dependency Info)

Project artifacts are released to [Clojars](http://clojars.org).

### Gradle

Add the following repository definition to your `build.gradle`:

``` groovy
repositories {
    maven {
        url "http://clojars.org/repo"
    }
}
```

The most recent release is

``` groovy
compile "com.novemberain.green-bunny:green-bunny:1.0.0-rc1"
```

### Maven

Add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

The most recent release is

``` xml
<dependency>
  <groupId>com.novemberain.green-bunny</groupId>
  <artifactId>green-bunny</artifactId>
  <version>1.0.0-rc1</version>
</dependency>
```


## Continuous Integration

[![Build Status](https://travis-ci.org/michaelklishin/green_bunny.svg?branch=master)](https://travis-ci.org/michaelklishin/green_bunny)


## License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


## Copyright

Michael Klishin, 2014.
