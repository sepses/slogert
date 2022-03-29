# SLOGERT v1.0.0-SNAPSHOT

-- **S**emantic **LOG E**xt**R**action **T**emplating (SLOGERT) --

* [General Introduction](#general-introduction)
* [Workflow](#workflow)
  - [Initialization](#-initialization-)
  - [A1 - Extraction Template Generation](#-a1---extraction-template-generation-)
  - [A2 - Template Enrichment](#-a2---template-enrichment-)
  - [A3 - RDFization](#a3---rdfization)
  - [KG Generation Algorithm](#kg-generation-algorithm)
* [How to run](#how-to-run)
* [SLOGERT configurations](#slogert-configurations)
	+ [Main Configuration](#main-configuration)
	+ [I/O Configuration](#i-o-configuration)

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

In our latest evaluation, we are testing our approach with the [AIT log dataset](https://zenodo.org/record/4264796), which contains additional logs from non-standard application, such as [suricata](https://suricata-ids.org/) and [exim4](https://ubuntu.com/server/docs/mail-exim4). In this repository, we include a small excerpt of the AIT log dataset in the `input` folder as example log sources.

## Workflow
    
![ ](https://raw.githubusercontent.com/sepses/slogert/master/figures/slogert.jpg)
<p align="center">**Figure 1**. SLOGERT KG generation workflow.</p>     

SLOGERT pipeline can be described in several steps, which main parts are shown in Figure 1 above and will be described as the following: 

#### Initialization

* Load `config-io` and `config.yaml`
* Collect target `log files` from the `input folder` as defined in `config-io`. 
  We assume that each top-level folder within input folder represent a single log source
* Aggregate collected log files into single file.
* Add log-source information to each log lines,
* If log lines exceed the configuration limit (e.g., 100k), split the aggregated log file into a set of `log-files`.
	
*Example results of this step is available in `output/auth.log/1-init/` folder* 

#### A1 - Extraction Template Generation

* Initialize `extraction_template_generator` with `config-io` to register extraction patterns
* For each `log-file` from `log-files`
	* Generate a list of `<extraction-template, raw-result>` pairs using `extraction_template_generator`
	
*NOTE: We use **LogPAI** as `extraction_template_generator`*   
*Example results of this step is available in `output/auth.log/2-logpai/` folder* 

#### A2 - Template Enrichment

*  Load existing `RDF_templates` list
*  Load `regex_patterns` from `config` list for parameter recognition
*  Initialize `NLP_engine` engine 
*  For each `extraction-template` from the list of `<extraction-template, raw-result>` pairs
	* Transform `extraction-template` into an `RDF_template_candidate`
	* if `RDF_templates` does not contain `RDF_template_candidate `
		* **[A2.1 - RDF template generation]**
          * For each `parameter` from `RDF_template_candidate`
              * If `parameter` is `unknown`
                  * **[A2.2 - Template parameter recognition]**
                    *  Load `sample-raw-results` from `raw-results`
                    *  Recognize `parameter` from `sample-raw-results` using `NLP_engine` and `regex_patterns` as `parameter_type`
                    *  Save `parameter_type` in `RDF_template_candidate`
                  * **[A2.2 - end]**		
          * **[A2.3 - Keyword extraction]**
            * Extract `template_pattern` from `RDF_template_candidate`
            * Execute `NLP_engine` engine on the `template_pattern` to retrieve `template_keywords`
            * Add `template_keywords` as keywords in `RDF_template_candidate` 
          * **[A2.3 - end]**
          * **[A2.4 - Concept annotation]**
            * Load `concept_model` containing relevant concept in the domain
            * For each `keyword` from `template_keywords `
                * for each `concept` in `concept_model`
                    * if `keyword` contains `concept`
                        * Add `concept ` as concept annotation in `RDF_template_candidate` 
          * **[A2.4 - end]**
          * add `RDF_template_candidate` to `RDF_templates` list
		* **[A2.1 - end]**

*NOTE: We use **Stanford NLP** as our `NLP_engine`*  
*Example results (i.e., `RDF_templates`) of this step is available as `output/auth.log/auth.log-template.ttl`*

#### A3 - RDFization

* Initialize `RDFizer_engine`
* Generate `RDF_generation_template` from `RDF_templates` list
* for each `raw_result` from `raw_results` list
	* Generate `RDF_generation_instances` from `RDF_generation_template` and `raw_result`
	* Generate `RDF_graph` from `RDF_generation_instances` and `RDF_generation_template` using `RDFizer_engine`

*NOTE: We use **LUTRA** as our `RDFizer_engine`*   
*Example `RDF_generation_template` and `RDF_generation_instances` are available in the `output/auth.log/3-ottr/` folder.*    
*Example results of this step is available in the `output/auth.log/4-ttl/` folder* 

### KG Generation Algorithm
    
<p align="center">
  <img width="460" src="https://raw.githubusercontent.com/sepses/slogert/master/figures/algorithm.png">
</p>
<p align="center"><b>Figure 2</b>. SLOGERT KG generation algorithms.</p>     

For those that are interested, we also provided an explanation of the KG generation in a form of Algorithm as shown in the Figure 2 above.  

## How to run
Prerequisites for running SLOGERT

- `Java 11` (for Lutra)
- `Apache Maven`
- `Python 2` with `pandas` and `python-scipy` installed (for LogPai)
    - the default setting is to use `python` command to invoke Python 2
    - if this is not the case, modification on the `LogIntializer.java` is needed.

We have tried and and tested SLOGERT on Mac OSX and Ubuntu with the following steps:

*  Compile this project (`mvn clean install` or `mvn clean install -DskipTests` if you want to skip the tests)
*  You can set properties for extraction in the config file (e.g., number of loglines produced per file). Examples of config and template files are available on the `src/test/resources` folder (e.g., `auth-config.yaml`for auth log data). 
*  Transform the CSVs into OTTR format using the config file. By default, the following script should work on the example file. (```java -jar target/slogert-<SLOGERT-VERSION>-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml```)
*  The result would be produced in the `output/` folder


## How to run in Docker
Prerequisites for running SLOGERT inside Docker container:
- Docker installed

There are two alternatives to run SLOGERT inside Docker container:
1) Use Intellij Run Configurations -> "Build SLOGERT container";
2) Execute following commands inside a project folder, where `Dockerfile` is located. Before execution replace
   `<path-to-the-project>` with a path where project is located.
``` docker 
docker container rm slogert_container
docker image rm slogert_image
docker build -t slogert_image .
docker run  \
    -v <path-to-the-project>/slogert/output_docker/m2:/root/.m2 \
    -v <path-to-the-project>/slogert/src/main/resources/config-io.yaml:/usr/local/slogert/src/main/resources/config-io.yaml \
    -v <path-to-the-project>/slogert/output_docker:/usr/local/slogert/output \
    -v <path-to-the-project>/slogert/output_docker/error.log:/usr/local/slogert/error.log \
    slogert_image
```


## SLOGERT configurations

Slogert configuration is divided into two parts: main configuration `config.yaml` and the input parameter `config-io.yaml`

### Main Configuration

There are several configuration that can be adapted in the main configuration file `src/main/resources/config.yaml`. We will briefly described the most important configuration options here.

* **logFormats** to describe information that you want to extract from a log source. 
  The logFormat contain references to the *ottrTemplate* to build the `RDF_generation_template` for RDFization step.
  We created a universal logFormat for *any* log data, which assume that the data already enriched with `Host`, `HostIp`, `LogType` and `DateTime` are special components provided by pre-processor, e.g., Kibana.
* **nerParameters** to register patterns that will used by StanfordNLP for recognizing log template parameter types. 
* **nonNerParameters** to register standard regex patterns for template parameter types that can't be easily detected using StanfordNLP. Both *nerParameters* and *nonNerParameters* are contains reference for ottr template generation.
* **ottrTemplates** to register `RDF_generation_template` building block necessary for the RDFization process.


### I/O Configuration

The I/O configuration aim to describe log-source specific information that are not suitable to be added into `config.yaml`. An example of this IO configuration is `src/test/resources/auth-config.yaml` for auth log. We will describe the most important configuration options in the following:

* **source**: the name of source file to be searched for in the input folder.
* **format**: the basic format of the log file, which will be used by `extraction_template_generator` in process A1.
* **logFormat**: types of the logfile. this value of this property should be registered in the `logFormats` within `config.yaml` for SLOGERT to work.
* **isOverrideExisting**: whether SLOGERT should use load `RDF_templates` or to override them.
* **paramExtractAttempt**: how many log lines should be processed to determine the `parameter_type` of a `RDF_template_candidate`. 
* **logEventsPerExtraction**: how many log lines should be processed in a single batch of execution. 