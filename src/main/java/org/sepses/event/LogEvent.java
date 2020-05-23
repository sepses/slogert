package org.sepses.event;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.sepses.config.ExtractionConfig;
import org.sepses.config.LogFormat;
import org.sepses.config.LogFormatFunction;
import org.sepses.config.Parameter;
import org.sepses.helper.Utility;
import org.sepses.ottr.OttrInstance;
import org.sepses.rdf.LOG;
import org.sepses.rdf.LOGEX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class LogEvent {

    public static final String SERVICES_NS_INSTANCE = "https://w3id.org/sepses/id/service/";
    public static final String SERVICES_NS_INSTANCE_PREFIX = "svid";
    public static final String LOGPAI_LINE_ID = "LineId";
    public static final String LOGPAI_EVENT_ID = "EventId";
    public static final String LOGPAI_EVENT_TEMPLATE = "EventTemplate";
    public static final String LOGPAI_PARAMETER_LIST = "ParameterList";
    public static final String UNKNOWN_PARAMETER = "unknown";

    private static final Logger log = LoggerFactory.getLogger(LogEvent.class);

    public final String idString;
    public final String hostString;
    public final String timestamp;
    public final String content;
    public final String templateHash;

    public List<String> contentParameters;
    public List<String> templateParameters;

    public LogEvent(CSVRecord record, ExtractionConfig config) {

        idString = UUID.randomUUID().toString();
        hostString = record.get(config.logFormatInstance.host);
        content = record.get(config.logFormatInstance.content);
        timestamp = getDate(record, config.logFormatInstance);
        templateHash = Utility.createHash(record.get(LOGPAI_EVENT_TEMPLATE));
        contentParameters = setParameters(record.get(LOGPAI_PARAMETER_LIST));
        templateParameters = new ArrayList<>();
        config.logFormatInstance.parameters.forEach(p -> {
            templateParameters.add(p.column);
        });
        config.logFormatInstance.functions.forEach(function -> executeFunction(function, record));
    }

    /**
     * Execute function as defined in the configuration file.
     *
     * @param function {@link LogFormatFunction}
     * @param record   {@link CSVRecord}
     */
    private void executeFunction(LogFormatFunction function, CSVRecord record) {

        String[] keys = function.columns.split(",");
        List<String> values = new ArrayList<>();
        for (String key : keys)
            values.add(record.get(key));

        try {
            Method method = this.getClass().getMethod(function.function, List.class);
            method.invoke(this, values);
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException Error");
            log.error(e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException Error");
            log.error(e.getMessage());
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException Error");
            log.error(e.getMessage());
        }
    }

    /**
     * Transform this LogEvent into an {@link OttrInstance} class instance
     *
     * @param config
     * @return OttrInstance
     */
    public OttrInstance toOttrInstance(ExtractionConfig config) {
        OttrInstance ottr = new OttrInstance();

        ottr.uri = Utility.getPrefixedName(LOGEX.LogEventTemplate, LOGEX.NS_INSTANCE_PREFIX, templateHash);

        ottr.parameters.add(Utility.getPrefixedName(LOG.Event, LOG.NS_INSTANCE_PREFIX, idString));
        ottr.parameters.add(Utility.getPrefixedName(LOG.Host, LOG.NS_INSTANCE_PREFIX, hostString));
        ottr.parameters.add("\"" + hostString + "\"");
        ottr.parameters.add("\"" + timestamp + "\"");
        ottr.parameters.add("\"" + Utility.cleanContent(content) + "\"");
        ottr.parameters.add(ottr.uri);
        ottr.parameters.add(Utility
                .getPrefixedName(LOG.Source, LOG.NS_INSTANCE_PREFIX, Utility.cleanUriContent(config.source)));

        templateParameters.forEach(tp -> {
            String value = tp;
            // TODO: fix this
            if (!value.startsWith(LOG.NS_INSTANCE_PREFIX + ":") && !value
                    .startsWith(LOGEX.NS_INSTANCE_PREFIX + ":")) {
                value = "\"" + value + "\"";
            }
            ottr.parameters.add(value);
        });

        processOttrParameters(config, ottr);

        return ottr;
    }

    /**
     * Processing OttrInstance parameters
     * TODO: handle hardcode functionalities
     *
     * @param config
     * @param ottr
     */
    private void processOttrParameters(ExtractionConfig config, OttrInstance ottr) {

        LogEventTemplate let = config.logEventTemplates.get(templateHash);
        Map<String, Parameter> map = new HashMap<>();
        Stream<Parameter> pStream = Stream.concat(config.nerParameters.stream(), config.nonNerParameters.stream());
        pStream.forEach(parameter -> {
            let.parameters.stream().forEach(letParam -> {
                if (letParam.equals(parameter.id)) {
                    map.put(letParam, parameter);
                }
            });
        });
        for (int i = 0; i < let.parameters.size(); i++) {
            Parameter ottrParam = map.get(let.parameters.get(i));

            String value = Utility.cleanParameter(contentParameters.get(i));

            if (ottrParam == null || ottrParam.function.equals("literal")) {

                ottr.parameters.add("\"" + value + "\"");

            } else if (ottrParam.function.startsWith("object")) {
                String[] functions = ottrParam.function.split(" ")[1].split(":");
                String iri = LOG.NS_INSTANCE_PREFIX + ":" + functions[1] + "_" + Utility.cleanUriContent(value);

                ottr.parameters.add(iri);
                ottr.parameters.add("\"" + value + "\"");

            } else if (ottrParam.function.equals("filePathSplit")) {
                String iri = Utility.getPrefixedName(LOG.File, LOG.NS_INSTANCE_PREFIX, value);
                String[] values = value.split("/");
                List<String> list = Arrays.asList(values);
                String path = "\"\"";

                if (!list.isEmpty()) {
                    path = "\"" + list.get(list.size() - 1) + "\"";
                }

                ottr.parameters.add(iri);
                ottr.parameters.add("\"" + value + "\"");
                ottr.parameters.add(path);

            } else if (ottrParam.function.equals("splitUrlParameter")) {
                String[] values = value.split("\\?");
                String url = "";
                String param = "";

                if (values.length > 0) {
                    url = values[0];
                    if (values.length > 1) {
                        param = values[1];
                    }
                }

                String iri = Utility.getPrefixedName(LOG.URL, LOG.NS_INSTANCE_PREFIX, url);
                ottr.parameters.add(iri);
                ottr.parameters.add("\"" + url + "\"");
                ottr.parameters.add("\"" + param + "\"");

            } else if (ottrParam.function.equals("ipWithPrefix")) {

                String ip = Utility.getIp(value);
                String iri = Utility.getPrefixedName(LOG.IPv4, LOG.NS_INSTANCE_PREFIX, ip); // default instance
                ottr.parameters.add(iri);
                ottr.parameters.add("\"" + ip + "\"");

            } else if (ottrParam.function.equals("portCreation")) {
                String portURI = Utility.getPrefixedName(LOG.Port, LOG.NS_INSTANCE_PREFIX, value);
                String linkedPortURI = SERVICES_NS_INSTANCE_PREFIX + ":Port_" + value;

                ottr.parameters.add(portURI);
                ottr.parameters.add(linkedPortURI);
                ottr.parameters.add(value);

            } else {
                ottr.parameters.add("\"" + value + "\"");

            }
        }
    }

    /**
     * Get pid and pname from a composite variable.
     *
     * @param values
     */
    public void getPidPname(List<String> values) {
        List<String> result = new ArrayList<>();

        if (!values.isEmpty()) {
            String value = values.get(0);
            String[] processValues = value.split("\\[");
            String pname = processValues[0];
            String pid = "";
            if (processValues.length > 1) {
                pid = processValues[1].replaceAll("[^\\d.]", "");
            }
            result.add(pname);
            result.add(pid);

        }

        templateParameters.addAll(result);
    }

    /**
     * Process ip from a variable.
     *
     * @param values
     */
    public void getIp(List<String> values) {
        List<Pair<String, String>> result = new ArrayList<>();
        String ipURL = "";
        String ipString = "";

        if (!values.isEmpty()) {
            ipString = values.get(0);
            ipURL = Utility.getPrefixedName(LOG.IPv4, LOG.NS_INSTANCE_PREFIX, ipString);
        }

        templateParameters.add(ipURL);
        templateParameters.add(ipString);
    }

    /**
     * handle heterogeneous date creation based on different {@link LogFormat}.
     * <p>
     * TODO: rework this later
     *
     * @param record
     * @param format
     * @return String date
     */
    public String getDate(CSVRecord record, LogFormat format) {
        String result = null;

        if (format.id.equals("ftp")) {
            String[] vars = format.time.split(",");
            result = Utility.getDate(record.get(vars[0]), record.get(vars[1]), record.get(vars[2]),
                    record.get(vars[3]));
        } else if (format.id.equals("unix")) {
            String[] vars = format.time.split(",");
            result = Utility.getDate(record.get(vars[0]), record.get(vars[1]), record.get(vars[2]));
        } else if (format.id.equals("apache")) {
            String[] vars = format.time.split(",");
            result = Utility.getDate(record.get(vars[0]), record.get(vars[1]));
        } else if (format.id.equals("audit")) {
            result = Utility.getDate(record.get(format.time));
        } else {
            log.error("*** Unknown Log Format!! ***");
        }

        return result;
    }

    /**
     * Extract parameters from LogPai parameter list.
     *
     * @param parameterString
     * @return @{@link List} of string that contains all parameters
     */
    private List<String> setParameters(String parameterString) {

        List<String> result = new ArrayList<>();

        String paramStringValue = parameterString.replaceAll("\", '", "', '");
        paramStringValue = paramStringValue.replaceAll("', \"", "', '");

        if (paramStringValue.length() > 4) { // basically if it's not empty
            String rawParams = paramStringValue.trim().substring(2, parameterString.length() - 2);
            String[] params = rawParams.split("', '");
            for (String param : params) {
                String[] spaceParams = param.split(" +");
                for (String spaceParam : spaceParams) {
                    result.add(spaceParam);
                }
            }
        }

        return result;
    }

}
