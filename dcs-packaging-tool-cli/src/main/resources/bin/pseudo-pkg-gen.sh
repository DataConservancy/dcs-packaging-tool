# Copyright 2015 Johns Hopkins University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#!/bin/bash

JAVA_BIN_DIR=bin
JAVA_BIN=java
JAVA_OPTS=""

EXECUTABLE_JAR=${project.artifactId}-standalone.jar

if [ -d $JAVA_HOME ] ; then
  JAVA_BIN="$JAVA_HOME/bin/$JAVA_BIN"
fi

JAVA_CMD="$JAVA_BIN $JAVA_OPTS"
COMMAND_LINE="$JAVA_CMD -cp $EXECUTABLE_JAR org.dataconservancy.packaging.tool.cli.PseudoPackageGeneratorApp $@"

which $JAVA_BIN 2>&1 > /dev/null

if [ $? != 0 ] ; then
  echo "Did not find $JAVA_BIN on the command path."
  echo "Please be sure that the path to your Java executable is set on your command path."
  exit 1
fi

$COMMAND_LINE
