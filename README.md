# Purpose

The following project contains the code of the approach proposed for [FormaliSE 2018](https://www.icse2018.org/track/Formalise-2018-papers)


# How to install
In order to package the project call 
```bash
mvn clean package
```

If you do not like tests:
```bash
mvn clean package -DskipTests
```

# How to use

## Dependency 

First, you need to install Z3 following [this link](https://github.com/Z3Prover/z3).
It is important to compile Z3 with the flag `--java`.
Once Z3 is compiled, copy the following files in the same folder of the jar file created in the **How to install** section:

 - com.microsoft.z3.jar
 - z3 executable
 - dynamic libraries (e.g. *libz3java.dylib* on OSX)

## Running it on file

First you need to index the project with 
```java
java -cp usage-0.5.jar smt.Indexing [project_name] [root_path]
```
This will execute the approach explained in the [SCAM 2017 paper](https://serg.aau.at/pub/GiovanniLiva/WebHome/Extracting_Timed_Automata_from_Java_Methods.pdf)
and it will extract user defined time related methods.

If you are not interested in compute user-defined time method, you can directly execute the verification of a file with the following command:
```java
java -cp usage-0.5.jar smt.SingleFile [file_path] [project_root] [project_name]
```

## Run the Experiment
Inside the `evaluation` folder there are data (except the open source projects) and files used in our experiments.
Currently, the scripts to automatically clone the projects and run the evaluation support only macOS.
The experiment can be run executing the **run.sh** script.

File `summary` contains a summary of our experiment in a excel table format. Folder `results` contains the output of our tool per project. 


