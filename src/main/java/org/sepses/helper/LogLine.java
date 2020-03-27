package org.sepses.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LogLine {

    protected Integer counter;
    protected String dateTime;
    protected String content;
    protected List<String> parameters;
    protected Map<String, String> specialParameters;
    protected String logpaiEventId;
    protected String templateHash;

    protected LogLine() {
        parameters = new ArrayList<>();
        specialParameters = new HashMap<>();
    }

    public Integer getCounter() {
        return counter;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getTemplateHash() {
        return templateHash;
    }

    public String getContent() {
        return content;
    }

    public List<String> getParameters() {
        return parameters;
    }

    protected void setParameters(String parameterString) {
        parameters.clear();

        // normalize the parameter list into ', ' format
        String paramStringValue = parameterString.replaceAll("\", '", "', '");
        paramStringValue = paramStringValue.replaceAll("', \"", "', '");

        if (paramStringValue.length() > 4) { // basically if it's not empty
            String rawParams = paramStringValue.trim().substring(2, parameterString.length() - 2);
            String[] params = rawParams.split("', '");
            for (String param : params) {
                parameters.add(param);
            }
        }
    }

    public Map<String, String> getSpecialParameters() {
        return specialParameters;
    }

    public String getLogpaiEventId() {
        return logpaiEventId;
    }

}
