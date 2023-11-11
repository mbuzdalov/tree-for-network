# Build the Best Tree for a Given Network Description

This repository is dedicated to investigation of various algorithms to solve the following problem.
We are given a description of what is going to be transferred over a network:
for each pair of nodes `a` and `b`, we know that `W_ab` packets are going to be transferred between then in unit time,
We want to build a binary tree over all these nodes 
(that is, a connected graph with a degree of each vertex not exceeding 3) 
such that, assuming the distance between the nodes `a` and `b` along this tree is `D_ab`,
the sum of `W_ab * D_ab` over all node pairs is minimized. 

This problem is NP-hard, so we compare various random searches, local searches, heuristic and evolutionary algorithms.
