package org.sepses.helper;

import org.apache.commons.csv.CSVRecord;
import org.sepses.yaml.InternalLogType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    protected LogLine(CSVRecord record, InternalLogType iLogType) {
        parameters = new ArrayList<>();
        specialParameters = new LinkedHashMap<>();

        iLogType.components.stream().forEach(component -> {
            String data = record.get(component.column);
            data = Utility.cleanContent(data);
            specialParameters.put(component.column, data);
        });
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

        // 38,Mon,Mar,9,23:01:18,2020,pid,10737,
        // "[ftp_akep] FAIL DOWNLOAD: Client ""::ffff:10.0.0.9"", ""/files/t.txt"", 0.00Kbyte/sec",32eb2840,"[<*>] FAIL DOWNLOAD: Client ""::ffff:<*>""
        // , <*> <*>.00Kbyte/sec","['ftp_akep', '10.0.0.9', '""/files/t.txt"", 0']"
        // ['ftp_akep', 'DOWNLOAD:', 'ffff:10.0.0.9', '"/files/test2.txt", 11', '1.06Kbyte/sec']

        // normalize the parameter list into ', ' format
//        if(parameterString.equalsIgnoreCase("['ftp_akep', 'DOWNLOAD:', 'ffff:10.0.0.9', '\"/files/test2.txt\", 11', '1.06Kbyte/sec']")) {
//            System.out.println();
//        }
//        System.out.println(parameterString);

        String paramStringValue = parameterString.replaceAll("\", '", "', '");
        paramStringValue = paramStringValue.replaceAll("', \"", "', '");

        if (paramStringValue.length() > 4) { // basically if it's not empty
            String rawParams = paramStringValue.trim().substring(2, parameterString.length() - 2);
            String[] params = rawParams.split("', '");
            for (String param : params) {
                String[] spaceParams = param.split(" +");
                for (String spaceParam : spaceParams) {
                    parameters.add(spaceParam);
                }
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
