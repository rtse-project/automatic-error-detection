#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
file="projects.csv"
WHERE="repositories"
#clean
#cd ${WHERE}
#rm -rf */
#cd ..
#clone
while IFS='' read -r line || [[ -n "$line" ]]; do
    name=`echo ${line} | cut -d ';' -f 1`
    url=`echo  ${line} | cut -d ';' -f 3`
    echo ${name} ${url}
    folder=${DIR}/${WHERE}/${name}/
    if [ ! -d "$folder" ] ; then
        git clone --depth 1 ${url} ${folder}
    fi
done <"$file"