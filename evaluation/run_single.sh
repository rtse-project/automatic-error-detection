#!/bin/bash

#smt.evaluation.Main
#ComputeNFileProject
MAIN=smt.evaluation.Main
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export LD_LIBRARY_PATH=${DIR}
java -cp "checker.jar:com.microsoft.z3.jar" ${MAIN} $1 $2 $3 &> logs/logging_${1}.log