# SLOGERT v0.9.0

-- **S**emantic **LOG E**xt**R**action **T**emplating (SLOGERT) --

## General Introduction 

SLOGERT aims to automatically extract and enrich low-level log data into an RDF Knowledge Graph that conforms to our [LOG Ontology](https://w3id.org/sepses/ns/log#). It integrates

 - [LogPai](https://github.com/logpai/logparser) for *event pattern* detection and *parameter* extractions from log lines
 - [Stanford NLP](https://stanfordnlp.github.io/CoreNLP/) for *parameter type* detection and *keyword* extraction, and 
 - [OTTR Engine](https://ottr.xyz/#Lutra) for RDF generation. 
 - [Apache Jena](https://jena.apache.org) for RDF data manipulation.

We have tested our approach on text-based logs produced by Unix OSs, in particular: 
  
  - Apache,
  - Kernel,
  - Syslog,
  - Auth, and 
  - FTP logs.

In our latest evaluation, we are testing our approach with the [AIT log dataset](https://zenodo.org/record/4264796), which contains additional logs from non-standard application, such as [suricata](https://suricata-ids.org/) and [exim4](https://ubuntu.com/server/docs/mail-exim4). 

## Workflow

Prerequisites for running SLOGERT

- `Java 11` (for Lutra)
- `Apache Maven`
- `Python 2` (for LogPai)
    - the default setting is to use `python` command to invoke Python 2
    - if this is not the case, modification on the `LogIntializer.java` is needed.
    
     

SLOGERT works in the following flow, notes that the result from each step will be structured accordingly (in the output folder) after the execution is finished.

  - **1-init**: collect all files with name `<source>`, write it into a single file    
      * **1.1 device identification**: add device info as the first keyword in the logline    
      * **1.2 preprocess**: slice large log files with more than `<linePerBatch>` lines into several files

  - **2-logpai**: param detection - process each file through LogPai to detect parameters and log templates

  - **3-ottr**: TURTLE template identification & OTTR instance generation   
      * **3.1 template generation**
        * **param identification** - recognizing parameters produced by logpai with stanford NLP and regex    
        * **NER rules building** - read config and create rules for Stanford NLP    
        * **template KG building** - represent the log templates in turtle format and add meaning     
        * **ottr template** - represent the log templates as OTTR      
      * **3.2 instance generation**
        * **ottr instance** - conversion of loglines into OTTR intances
  
  - **4-turtle**: ttl conversion - running lutra engine to convert OTTR instances into RDF graphs

## How to run

*  Compile this project (`mvn clean install`)
*  You can set properties for extraction in the config file (e.g., number of loglines produced per file). Examples of config and template files are available on the `src/test/resources` folder (e.g., `auth-config.yaml`for auth log data). 
*  Transform the CSVs into OTTR format using the config file. By default, the following script should work on the example file. (```java -jar target/slogert-<SLOGERT-VERSION>-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml```)
