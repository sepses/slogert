package org.sepses.yaml;

import java.util.List;

public class Config {
    public String logData;
    public String logType;
    public String logTemplate;
    public String logBaseTemplate;
    public Boolean isOverride;

    public String targetTemplate;
    public String targetData;
    public String targetNer;
    public String targetOttr;
    public String targetTurtle;


    public List<ConfigNS> ottrNS;
    public List<ConfigParameter> parameters;
}
