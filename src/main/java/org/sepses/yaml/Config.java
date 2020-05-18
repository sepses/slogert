package org.sepses.yaml;

import java.util.List;

public class Config {
    public String logData;
    public String logType;
    public String logName;
    public String logTemplate;
    public Boolean isOverride;

    public String targetData;
    public String targetOttr;
    public String targetTurtle;
    public String targetTemplate;

    public String targetNer;
    public String templateRdf;
    public String slogertTrig;

    public List<ConfigNS> ottrNS;
    public List<ConfigParameter> nerParameters;
    public List<ConfigParameter> nonNerParameters;

    public InternalLogType internalLogType;
}
