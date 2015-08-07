# compareBuild

This repository contains an exercise taken from [Learning Concurrent Programming in Scala](https://www.packtpub.com/application-development/learning-concurrent-programming-scala) by Aleksandar Prokopec

The exercise proposes to implement a text shell that asks an URL and returns on screen the source contained into. All the code must be based on concurrent package avoiding synchronization based on shared memory, resource lock and time waiting.

The scope of this repository is analize code developing with concurrent package provided in :

* Scala 2.11
* Java 8

The original exercise asks to use only `Future` and `Promise` for Scala so for the Java 8 side I have approched with `CompletableFuture`.

## How to run code

* Scala side can be executed directly with command `scala ch4ex1.scala`
* Java 8 side can be executed with commands `javac ch4ex1.java` and `java ch4ex1`

#### Note on the code 

* As a student with every exercise solution, it can be improved so feedback are wellcome!
* As a programmer in a typically working scenario, I am sure that with difficulty concurrent packages are adopted directly and preferably other layer/framework are adopted to handle concurrency.
* It's my opinion that how works concurrent packages and how them handle async operation should be a must for a developer also when demand to other layer/framework concurrency.
