[![Gitter](https://img.shields.io/gitter/room/gitterHQ/gitter.svg)](https://gitter.im/lagom/lagom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)[<img src="https://img.shields.io/travis/typesafehub/activator-lagom-java.svg"/>](https://travis-ci.org/typesafehub/activator-lagom-java)
# eCommerce system with Lagom Framework

### installation:
check whether you have JDK 8
```sh
$ java -version
java version "1.8.0_74"
Java(TM) SE Runtime Environment (build 1.8.0_74-b02)
Java HotSpot(TM) 64-Bit Server VM (build 25.74-b02, mixed mode)
$ javac -version
javac 1.8.0_74
```
download activator from [here](https://downloads.typesafe.com/typesafe-activator/1.3.12/typesafe-activator-1.3.12-minimal.zip) and add it to your PATH.

```sh
$ git clone https://github.com/AmirHMousavi/4DV609-eCommerce.git
$ cd 4DV609-eCommerce
$ activator
if you want to import the project in Eclipse
$ eclipse
else
$ runAll
```
### Importing the project in an IDE
Import the project as an sbt project in your IDE.
This project uses the [Immutables](https://immutables.github.io) library, be sure to consult [Set up Immutables in your IDE](http://www.lagomframework.com/documentation/1.0.x/ImmutablesInIDEs.html).
