package org.sepses.config;

import org.sepses.event.LogEventTemplate;
import org.sepses.ottr.OttrTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExtractionConfig {

    public String source;
    public String sourceLogpai;
    public String sourceLogpaiTemplate;

    public String logFormat;
    public String logSourceType;

    public Boolean isOverrideExisting;
    public Integer paramExtractAttempt;

    public String targetOttr;
    public String targetOttrTurtle;

    public String targetOttrBase;
    public String targetStanfordNer;
    public String targetConfigTurtle;
    public String targetConfigTimer;

    public List<LogFormat> logFormats;
    public List<Parameter> nerParameters;
    public List<Parameter> nonNerParameters;
    public List<Namespace> namespaces;
    public List<OttrTemplate> ottrTemplates;

    public Map<String, LogEventTemplate> logEventTemplates;

    public LogFormat logFormatInstance;
    public OttrTemplate logFormatOttrBase;

    public ExtractionConfig() {
        logFormats = new ArrayList<>();
        nerParameters = new ArrayList<>();
        nonNerParameters = new ArrayList<>();
        namespaces = new ArrayList<>();
        ottrTemplates = new ArrayList<>();
        logEventTemplates = new LinkedHashMap<>();
    }
}
