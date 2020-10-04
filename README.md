# sewer

[![CodeFactor](https://www.codefactor.io/repository/github/proximyst/sewer/badge/main)](https://www.codefactor.io/repository/github/proximyst/sewer/overview/main)

*sewer: "where the shit goes down"*

This is a library for designing and executing pipelines with type safety in
mind. The pipelines are named and use Java 8 features such as lambdas for
cleaner code.

## Dependency

The library is available on Bintray:

```kotlin
val sewerVersion = "0.7.0"

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
  .<String, String>builder("ensure int", Module.filtering(in -> in.chars().allMatch(Character::isDigit)))
  .module("parse", Module.immediatelyWrapped(Integer::parseInt))
  .module("square", Module.immediatelyWrapped(i -> i * i))
  .build();

system.pump("123").join().isSuccessful() // => true
system.pump("123").join().asOptional().get() // => 123
system.pump("cool").join().isSuccessful() // => false
system.pump("cool").join().mayContinue() // => false

system.pump("-5").join().isSuccessful() // => false; uh oh! A bug!
system.pump("-5").join().getPipeName() // => "parse" - luckily we know where the bug is.
```
