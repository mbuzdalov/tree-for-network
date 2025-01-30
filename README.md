# Build the Best Tree for a Given Network Description

This repository is dedicated to investigation of various algorithms to solve the following problem.
We are given a description of what is going to be transferred over a network:
for each pair of nodes `a` and `b`, we know that `W_ab` packets are going to be transferred between then in unit time,
We want to build a binary tree over all these nodes 
(that is, a connected graph with a degree of each vertex not exceeding 3) 
such that, assuming the distance between the nodes `a` and `b` along this tree is `D_ab`,
the sum of `W_ab * D_ab` over all node pairs is minimized. 

This problem is NP-hard, so we compare various random searches, local searches, heuristic and evolutionary algorithms.

## Running experiments

To build the jar file, run `mvn package`. The file will appear as `target/tree-for-network-1.0-SNAPSHOT.jar`.
The project does not have any non-test dependencies, so this jar file is enough for running.
The project uses some Java 17 features, such as the new `java.util.RandomGenerator` interface,
and Maven builds for Java 17 too.

The entry point is `com.github.mbuzdalov.tree4network.Main`, and it is designed to perform a single run
of a single algorithm on a single instance in a single thread (not including garbage collection threads).
The callers may have fine-grained control over what exactly is run how exactly (which CPUs to use, etc.).

The command line parameters are `<algo> <file> <runID> <timeout> <fitness-log>`, all mandatory, where:
* `<algo>` is the name of the algorithm to run
* `<file>` is the dataset file (datasets used in the paper are in the `data` directory in the project root)
* `<runID>` is an arbitrary string that distinguishes this run from others of the same algo and same dataset, also used in seeding the random number generator
* `<timeout>` is the time limit in seconds (e.g. 7200 for 2 hours)
* `<fitness-log>` is the file where fitness updates get logged in a CSV format.

The algorithm names can be seen in the `Main.java` file, and are also printed as a part of the help message.

The standard output is normally just one line that shows the final stats.
Algorithms with crossovers produce a lot more debug information at the moment,
but the very last line of the output will be the same final stats.
