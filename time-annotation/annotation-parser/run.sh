#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
${DIR}/jflex.sh
${DIR}/jyacc.sh
