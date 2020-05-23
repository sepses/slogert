# SLOGERT v0.7.0

A **S**emantic **LOG E**xt**R**action **T**emplating (SLOGERT) Approach

We aim to automatically extract and enrich low-level log data into an RDF Knowledge Graphs.
To this end, we provide a set of regex to detect a number of concepts (e.g., IP, URL, User, Domain, and Port).

Technology-wise, we utilise **LogPai** for automatically detect log patterns and extracting parameters, 
**Standord NLP** for parameter type detection and keyword extractions, 
as well as **OTTR Engine** for RDF generation. 

Unfortunately, it's not so easy to combine **LogPai** and **OTTR Engine** together, 
and therefore you still need to do it in several steps as follows.

1) Run LogPai to generate `<logname>_structured.csv` and `<logname>_templates.csv`. 
    Examples of such files are available on the `scenario/input` folder.  
2) Compile this project (`mvn clean install`)
3) You can set properties for extraction in the config file (e.g., number of loglines produced per file). 
    Examples of config and template files are available on the `src/test/resources` folder 
    (e.g., `auth-config.yaml`for auth log data). 
4) Transform the CSVs into OTTR format using config file. By default, the following script should work to work on the example file. 
    (```java -jar target/slogert-0.7.0-jar-with-dependencies.jar -c src/test/resources/auth-config.yaml```)
5) After the transformation finished, it will provide you with the command to execute lutra, such as the following: (```
    java -jar exe/lutra.jar --library scenario/config-base.ottr --libraryFormat stottr --inputFormat 
    stottr scenario/output/auth.log_structured.ottr --mode expand --fetchMissing > scenario/output/auth.ttl```) 
6) Execute the transformation of the OTTR files into RDF graph using the command above from command line.
7) We provided example data in the scenario folder for testing. you can execute it by executing running the script 
    ```./scenario/scenario.sh``` from the root folder.  
        
## Scenario execution 

A script for executing transformation data for scenario has been created in the scenario folder `scenario/scenario.sh`    
to run it, after you compile the project (`mvn clean install`), run `./scenario/scenario.sh` from project folder.

           
