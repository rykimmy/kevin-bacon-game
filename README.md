# Kevin Bacon Game
Utilizes search methods to implement the Kevin Bacon game.

## Overview
The Kevin Bacon Game works by using degrees of separation between actors through co-starred movies. The game begins with Kevin Bacon set as the “center of the universe”. An actor who co-starred in a movie with Kevin Bacon would have a degree of separation of 1. Utilizing graphs, actors are set as vertices and the edges between actors are the movies they co-starred in. The game provides various functionality including: listing actors by degrees of separation; list actors with infinite degrees of separation (no connection to the "center of the universe"); the path from any actor to the "center of the universe"; setting a new "center of the universe"; finding the average separation to the "center of the universe"; and ranking all actors based on their average separation as "center of the universe".

## Execution
To start this program, run BaconGame.java's main method. The given files provide the database of actors and movies that are used to build the graphs. (Note: you may need to rename the pathnames of testfiles, depending on where you place them.)
