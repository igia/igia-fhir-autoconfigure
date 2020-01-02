# igia-fhir-autoconfigure

HAPI FHIR autoconfiguration classes for Spring Boot FHIR API facade.

## Usage

This library is a Spring Boot autoconfiguration library that will create a HAPI FHIR server with default configuration once the dependency is included in your Spring Boot application and the package 'io.igia.config.fhir' is added to component scan.
```
<dependency>
  <groupId>io.igia</groupId>
  <artifactId>igia-fhir-auto-configure</artifactId>
  <version>0.3.3</version>
</dependency>

```

### Configuration

See [usage](https://github.com/igia/igia-fhir-autoconfigure/blob/master/docs/usage.md) for configuration options.


## Building for production

To install the igia-fhir-autoconfigure library in your local maven repository, run:

    ./mvnw clean install

## Testing

To launch the library's tests, run:

    ./mvnw clean test

## Contributing

Please read [CONTRIBUTING](https://igia.github.io/docs/contributing/) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/igia/igia-fhir-autoconfigure/tags).

## Acknowledgments

* This project is based on the [HAPI FHIR Spring Boot library](https://github.com/jamesagnew/hapi-fhir/tree/master/hapi-fhir-spring-boot).

## License and Copyright

MPL 2.0 w/ HD  
See [LICENSE](LICENSE) file.  
See [HEALTHCARE DISCLAIMER](HD.md) file.  
© [Persistent Systems, Inc.](https://www.persistent.com)