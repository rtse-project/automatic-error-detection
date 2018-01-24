#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
file="projects.csv"
WHERE="repositories"

# Fresh version where to write
rm -rf generated_run_*.sh
rm -rf logs
rm -rf output

mkdir logs
mkdir output

# Split every #core-1
N_CPUS=`getconf _NPROCESSORS_ONLN`
echo Detected ${N_CPUS}
let "N_CPUS=N_CPUS-8"
echo Using ${N_CPUS} instances



i=${N_CPUS}
counter=0
while IFS='' read -r line || [[ -n "$line" ]]; do
    name=`echo ${line} | cut -d ';' -f 1`
    echo $name
    rm -f ${name}.csv
    rm -f logging_${name}.log
    path=${DIR}/${WHERE}/${name}/
    if [ "${i}" -eq "${N_CPUS}" ]; then
       let "i=0"
       let "counter=counter+1"
       touch generated_run_${counter}.sh
       chmod +x generated_run_${counter}.sh
    fi
    echo screen -d -m bash ./run_single.sh ${name} ${path} output >> generated_run_${counter}.sh
    let "i=i+1"
done <"$file"