# SLOGERT v1.2.0-SNAPSHOT 
A **S**emantic **LOG E**xt**R**action **T**emplating (SLOGERT) Approach

We aim to automatically extract and enrich low-level log data into an RDF Knowledge Graphs.
To this end, we provide a set of regex to detect a number of concepts (e.g., IP, URL, User, Domain, and Port).

Technology-wise, we utilise **LogPai** for automatically detect log patterns and extracting parameters, 
**Standord NLP** for parameter type detection and keyword extractions, 
as well as **OTTR Engine** for RDF generation. 

Unfortunately, it's not so easy to combine **LogPai** and **OTTR Engine** together, 
and therefore you still need to do it in several steps as follows.

1) Run LogPai to generate `<logname>_structured.csv` and `<logname>_templates.csv`. 
    Examples of such files are available on the `src/test/resources` folder.  
2) Compile this project (`mvn clean install`)
3) Transform the CSVs into OTTR format using config file (I/O settings) and template file (parameter extractions). 
    Examples of config and template files are available on the `src/test/resources` folder 
    (e.g., `apache-config.yaml` and `apache-template.yaml` for apache log data).
    By default, the following script should work to work on the example file. 
    (```java -jar target/slogert-1.2.0-SNAPSHOT-jar-with-dependencies.jar -c apache-config.yaml -t apache-template.yaml```)
4) After the transformation finished, it will provide you with the command to execute lutra, such as the following: (```
    java -jar exe/lutra.jar --library scenario/output/apache-log_templates.ottr --libraryFormat stottr --inputFormat stottr 
    scenario/output/apache-access.log_structured.ottr --mode expand --fetchMissing > scenario/output/apache-access.log_structured.ttl```) 
5) Execute the transformation of the OTTR files into RDF graph using the command above from command line.  
        
           
