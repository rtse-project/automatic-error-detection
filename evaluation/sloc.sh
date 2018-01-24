#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
file="projects.csv"
WHERE="repositories"
MAIN=smt.evaluation.SLOCSlice
export LD_LIBRARY_PATH=${DIR}

# Fresh version where to write
rm -fR logs_sloc
mkdir logs_sloc


while IFS='' read -r line || [[ -n "$line" ]]; do
    name=`echo ${line} | cut -d ';' -f 1`
    echo $name
    path=${DIR}/${WHERE}/${name}/
    echo $path  
    java -cp "usage-0.5.jar:com.microsoft.z3.jar" ${MAIN} ${name} ${path} &> logs_sloc/${name}.log
done <"$file"