#!/bin/bash

# Script to generate the sensitivie-species.xml file.
#
# Requires a config file to be present in /data/sds/sds-config.properties
# Code must be built with `mvn clean install` 
# Will output the raw XML string to stdout

# debug (to print out the file names) - set to 1 for true or 0 for false
debug=0
cd target

# These two lines are used to find the most recent version jar files (by filename sort) in target dir (with and without assembly)
jar_file=$(find . -maxdepth 1 -type f -regex '\./sds.*\.jar' | sort | grep -v 'assembly' | head -n 1) 
assembly_file=$(find . -maxdepth 1 -type f -regex '\./sds.*\.jar' | sort | grep 'assembly' | head -n 1)

if [ -z "$assembly_file" ] && [ -z "$jar_file" ]; then
  echo "No file matching the pattern was found."
elif [ $debug -eq 1 ]; then
  echo "File found: $jar_file"
  echo "File found: $assembly_file"
else
  # Create the lib directory and run the SensitiveSpeciesXmlBuilder "main" class
  jar xf $assembly_file lib lib 
  java -Xmx2g -Xms2g -classpath "${jar_file}:lib/*" au.org.ala.sds.util.SensitiveSpeciesXmlBuilder # > sensitive-species-data.xml
fi

cd ..