package org.sepses.slogert.config;

import java.util.ArrayList;
import java.util.List;

public class LogFormat {
    public String id;
    public String ottrBaseTemplate;
    public String header;
    public String host;
    public String content;
    public String time;
    public List<LogFormatProperty> parameters;
    public List<LogFormatFunction> functions;

    public LogFormat() {
        parameters = new ArrayList<>();
        functions = new ArrayList<>();
    }

}
