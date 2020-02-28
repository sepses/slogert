package org.sepses.yaml;

import java.util.List;

public class Config {
    public String creator;
    public String lastUpdated;
    public String baseTemplate;
    public String logTemplate;
    public String logData;
    public String logType;
    public String targetTemplate;
    public String targetData;
    public String targetNer;
    public String targetOttr;
    public String targetTurtle;
    public Boolean isOverride;
    public List<NameSpace> ottrNS;
    public List<Parameter> parameters;
}
