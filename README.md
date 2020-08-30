# sewer

[![CodeFactor](https://www.codefactor.io/repository/github/proximyst/sewer/badge/main)](https://www.codefactor.io/repository/github/proximyst/sewer/overview/main)

*sewer: "where the shit goes down"*

This is a library for designing and executing pipelines with type safety in
mind. The pipelines are named and use Java 8 features such as lambdas for
cleaner code.

## Dependency

The library is available on Bintray:

```kotlin
val sewerVersion = "0.2.0"

repositories {
    maven("https://dl.bintray.com/proximyst/sewer")
}

dependencies {
    api("com.proximyst.sewer:sewer:$sewerVersion")
}
```

## Usage

The library focuses in the `SewerSystem`:

```java
SewerSystem<String, Integer> system = SewerSystem
    .<String, Integer>builder("parse", Integer::parseInt, str -> str.chars().allMatch(Character::isDigit))
    .pipe("square", i -> i * i)
    .build();

system.pump("123").asSuccess().getResult() // => 123
system.pump("123").isSuccessful() // => true
system.pump("cool").isFiltered() // => true
system.pump("cool").isFailure() // => true

system.pump("-5").isFailure() // => true; uh oh! A bug!
system.pump("-5").getPipeName() // => "parse" - luckily we know where the bug is.

SewerSystem<Integer, Integer> system = SewerSystem.<Integer, Integer>builder("never successful", i -> {
  throw new RuntimeException();
}).build();

PipeResult<Integer> result = system.pump(1); // We can also store the result.
result.isExceptional() // => true
result.isFailure() // => true
result.asExceptional().getException() instanceof RuntimeException // => true
```
