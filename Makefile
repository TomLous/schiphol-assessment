SHELL := /bin/bash

.PHONY: download-data
download-data:
	@echo "Downloading routes.dat"
	@mkdir -p data/routes/
	@-rm -rf data/routes 2> /dev/null
	@curl --create-dirs -L "https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat" -o data/routes/routes.dat

.PHONY: build-jar
build-jar:
	sbt assembly

.PHONY: run-batch
run-batch:
	@logfile=`pwd`/analytics/src/main/resources/log4j.properties;\
	spark-submit \
			--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file://$$logfile -Dconfig.resource=application.local.conf -DLogLevel=DEBUG" \
			--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file://$$logfile -Dconfig.resource=application.local.conf -DLogLevel=DEBUG" \
			--class xyz.graphiq.schiphol.analytics.jobs.BatchJob \
			output/bin/analytics-assembly-0.1.0.jar


.PHONY: run-streaming
run-streaming:
	@logfile=`pwd`/analytics/src/main/resources/log4j.properties;\
	spark-submit \
			--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file://$$logfile -Dconfig.resource=application.local.conf -DLogLevel=DEBUG" \
			--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file://$$logfile -Dconfig.resource=application.local.conf -DLogLevel=DEBUG" \
			--class xyz.graphiq.schiphol.analytics.jobs.StreamingJob \
			output/bin/analytics-assembly-0.1.0.jar


# Guard to check ENV vars
guard-%:
	@ if [ -z '${${*}}' ]; then echo 'Environment variable $* not set.' && exit 1; fi

# Catch all for module name arguments
%:
	@:
