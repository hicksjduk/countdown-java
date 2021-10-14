# countdown-java

A Java solver for the numbers game as played on 
[the TV show "Countdown"](https://www.channel4.com/programmes/countdown).

In each game, six source numbers are chosen at random (they may include up to two instances
of each number between 1 and 10 inclusive, and up to one instance each of 25, 50, 75 and 100);
a three-digit target number is generated;
and the objective is to make an arithmetic expression which uses some or all of the
source numbers, together with the four basic arithmetic operations, to make a total that
is as close as possible to the target number. 

A solution scores 10 points if its value is exactly equal to the
target number; 7 if it differs from the target number by between 1 and 5 inclusive;
5 if it differs from the target number by between 6 and 10 inclusive; and nothing if it
differs by more than 10. However, if the two players come up with solutions that
differ from the target number by different amounts, only the one with the closer solution
scores points.

This solver finds the best possible solution that differs from the target number by 10 or less.
One solution is regarded as being better than another if it differs from the target number by a 
smaller amount, or if it differs by the same amount but uses fewer source numbers. If there is 
no solution that differs by 10 or less, no solution is returned.

## Running

The solver is packaged in a runnable JAR file called `countdown.jar`. It can be
run from the command line with the command:

```
java -jar countdown.jar <args>
```

Alternatively, the solver can be run in the IDE of your choice by running the `Solver` class as a Java application and specifying the correct arguments.

The argument list must conform to the following rules, otherwise an error message is issued
and the solver is not run:
* At least one argument must be specified.
* Every argument must be a non-negative integer (it must consist entirely of numeric digits).
* If there is exactly one argument, this denotes that the source and target numbers should be chosen
randomly. The argument specified must be in the range 0 to 4 inclusive, and specifies the number of 
large source numbers (greater than 10) to be chosen; the remainder are chosen from among the small
numbers.
* If there is more than one argument, this denotes that the first argument is the target number, and the
other arguments the source numbers, to be used. Each number must be in the valid range for that type
of number, and the number of occurrences of each source number must also be valid.

If the argument list is valid, the solver is run with the specified numbers if there is more than one argument, 
or with randomly-generated numbers if there is just one.

## Testing

The tests in this repository use BDD (behaviour-driven development) tests, specified in feature
files written in [the Gherkin language](https://cucumber.io/docs/gherkin/reference/), and using 
[the Java implementation of Cucumber](https://github.com/cucumber/cucumber-jvm) to run them.

The following steps are required in order to enable the automatic discovery and running of
tests with Cucumber in a Maven-based Java project such as this one:
* Include in the `pom.xml` file the following minimum Cucumber artifacts (the groupId in all cases
is `io.cucumber`):
    * `cucumber-junit-platform-engine`
    * `cucumber-junit`
    * `cucumber-java`
    * `cucumber-picocontainer`
* In the `src/main/test` subdirectory of the project, create a package which
contains at least one Java source file. The class defined in one of these source files needs to be annotated
with `@Cucumber`, and the source file(s) must contain 'glue code' that specifies the implementation of steps in the feature files defined below. In a simple case, the class annotated with 
`@Cucumber` (which is thereby defined as a test class that can be used to discover and run Cucumber
tests) can be the same as the one that contains the glue code.
* In the `src/main/resources` subdirectory of the project, create a folder structure that corresponds
to the naming of the package that contains the glue code. In this folder, create one or more feature files.

Once this is done, the tests can be discovered and run using the command `mvn test`, or by running
in the IDE of your choice the file whose class is annotated with `@Cucumber`.

**Warning:** If you use version 7.0.0 (or later) of Cucumber, the `@Cucumber` annotation is deprecated,
and the documentation of what to use instead is very unclear, and references annotations that don't
exist in the JUnit Jupiter library. Therefore, this project has reverted to version 6.11.0, and until a better solution is found will continue to use that annotation.

### Step implementation notes

The recommended way for data to be shared between steps is to use dependency injection. Cucumber integrates with a wide variety of DI frameworks; if one of these is not being used in a project, a very simple
mechanism is to use pico-container, as is done in this project.

When the pico-container integration is enabled (by including the `cucumber-picocontainer` artifact
in the classpath), if any class loaded by Cucumber has a constructor that takes parameters, values for those
parameters are automatically created and injected into the class. If there are two or more classes that 
contain step definitions and take parameters of the same type, for each test the same objects are injected into all of the instances of those classes.

In this package, there is only one class (`SolverTest`) that defines step definitions; one object of that class is created
for each test, and each has a different object (of type `TestContext`) injected into it.

## Packaging

The application can be packaged into a JAR file using the Maven build target
`assembly:assembly`. For a clean build (not dependent on previous builds),
that target can optionally be preceded by the `clean` target.

For a clean build from the command line (with the root directory of the
project as the current working directory), the command to do this is

```
mvn clean assembly:assembly
```

The `assembly:assembly` step packages the application and its
dependencies into a JAR file called `countdown.jar`, which is placed in the `target` directory.