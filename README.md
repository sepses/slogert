# SLOGERT
A **S**emantic **LOG E**xt**R**action **T**emplating (SLOGERT) Approach

We tried to automatically extract and enrich low-level log data into an RDF Knowledge Graphs.
To this end, we provide a set of regex to detect a number of concepts (e.g., IP, URL, User, Domain, and Port).

Technology-wise, we utilise **LogPai** for automatically detect log patterns and extracting parameters and 
**OTTR Engine** for RDF generation. Unfortunately, it's not so easy to combine these two tools together, 
and therefore you still need to do it in several steps as follows.

1) Run LogPai to generate `<logname>_structured.csv` and `<logname>_templates.csv`
2) Compile this project (`mvn clean install`)
3) Transform the CSVs into OTTR format (```
    java -jar target/slogert-1.0-SNAPSHOT-jar-with-dependencies.jar 
        -t <logname>_templates.csv 
        -l <logname>_structured.csv 
        -o unix```)
4) Transform the OTTR files into RDF graph (```
    java -jar exe/lutra.jar 
        --library <logname>_templates.csv.ottr --libraryFormat stottr
        --inputFormat stottr  <logname>_structured.csv.ottr 
        --mode expand --fetchMissing > output.ttl```) 
        
           
