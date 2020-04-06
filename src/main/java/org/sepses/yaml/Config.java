package org.sepses.yaml;

import java.util.List;

public class Config {
    public String logData;
    public String logType;
    public String logTemplate;
    public String logBaseTemplate;
    public Boolean isOverride;

    public String targetData;
    public String targetOttr;
    public String targetTurtle;
    public String targetNer;
    public String targetTemplate;

    public List<ConfigNS> ottrNS;
    public List<ConfigParameter> parameters;
    public List<ConfigParameter> internalParameters;

    public InternalLogType internalLogType;
}
