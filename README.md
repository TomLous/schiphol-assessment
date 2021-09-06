# Schiphol Data Engineer Assessment

## Assignment - 'Live' data analysis

### Introduction
We would like you to create a simple “real-time” stream processing pipeline. We will use a fixed flight route dataset as a source. Our pipeline should provide us the most popular source airports per time window.
Here is a link with a dataset which you use for this assignment: <https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat> . 
The documentation for this data can be found here: <https://openflights.org/data.html .

### Assignment
Using the flight routes dataset you will stream the given data into our pipeline, process it and display results. 
Keep in mind the resulting implementation needs to be somehow demoable. 
Use tooling you think is best fitted for the task (e.g. Docker, public cloud, etc.) and give us an explanation why you decided for it.
For the sake of this assignment, we limit the choice of processing framework to Apache Spark. 
We strongly suggest the use of Spark Structured Streaming over Spark Streaming. 
Use either Python or Scala.

Tasks:

1. Create a batch Spark job that read in the routes dataset. It should create an overview of the top 10 airports used as source airport. Write the output to a filesystem.
2. Use Spark structured streaming to change your job into a streaming job, and use the dataset file as a source.
3. Next change your streaming job so the aggregations are done using sliding windows. Pick any window and sliding interval. The end result should be the top 10 airports used as source airport within each window. When choosing the window interval, keep the size of the dataset in mind.
4. Productionize your code by adding unit tests.

We would like to be able to check your assignment. Code produced plus a description of your steps and thinking process should be the output of your assignment.

The goal of the exercise is not to get perfect solutions but rather to test candidate’s ability to implement the assignment, flexibility, willingness to learn and explain technical solutions they implemented.

## Solution

### Structure

Tthe project consists of 3 submodules, to be abstracted away in separate libraries

1. `util`, which is project independent useful tooling for Spark Jobs
2. `model`, which contains case classes / schema definitions and some transformers that are closely related to schema transformers. This should live near a schema registry and either feed the registry or be feed by it. 
3. `analytics`, which contains the actual code to do the analytics required in this assignment.

### Configuration

Configuration for the analytics jobs is done via an `application.conf` file in the `/analitics/src/main/resources` folder.

You can create different configuration files for each environment.

To select a configuration file pass `-Dconfig.resource=application.???.conf` as VM parameters to the job.

Secrets should not be stored in here, but reference by ENV variable: `key = ${?SECRET_KEY}`, where `SECRET_KEY` is an ENV var.

> For this assignment the current `application.local.conf` should suffice as is.

### Setup

##### 1. Have the correct JVM

For this project we need JVM version 11. Make sure that is either the default and/or correctly set in `JAVA_HOME` env

[Jenv](https://github.com/jenv/jenv) is a great solution for this.

##### 2. Download the dataset locally

Just run `make download-data` and it will do this automatically.
Optionally download manually and change the `application.local.conf`

##### 3.  Test the code

First you may want to run `sbt clean compile`

But mainly run `sbt test` to run the entire test suite

##### 4.  Build the binary

Run `sbt analytics/assembly` to build the fat jar for this module

### Running

Make sure you have spark 3+ (pref 3.1.2) installed locally to run the freshly minted jar on this cluster

Ignore warnings about `WARNING: An illegal reflective access operation has occurred`, which is a JDK 11 / Spark 3 issue, which will be resolved (hopefully)


####  Run Batch

`make run-batch` will use the `jar` and run `xyz.graphiq.schiphol.analytics.jobs.BatchJob` based on the configuration in `application.local.conf`

The output will be stored (as specified in the conf) in `output/output-batch`

eg. use `cat output/output-batch/part-????.csv` to show data

####  Run Streaming

`make run-streaming` will trigger the streaming job located in `xyz.graphiq.schiphol.analytics.jobs.StreamingJob`

The output will be stored as parquet (as specified in the conf) in `output/output-streaming`

To follow the progress of the job check the [Spark UI](http://localhost:4040/)

To inspect the content use [parquet-tools](https://formulae.brew.sh/formula/parquet-tools)

`parquet-tools cat --json output/output-streaming/part-0000-???.parquet | jq`

Just Ctrl-C to quit the streaming job. 

For a fresh run make sure to delete the checkpoint & target directory.

### Productionize

For production just run the build commands in CI/CD and push the jar to an artifactory
