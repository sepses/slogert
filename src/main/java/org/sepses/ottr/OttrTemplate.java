package org.sepses.ottr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class OttrTemplate {

    public String uri;
    public List<String> parameters;
    public List<String> functions;

    public OttrTemplate() {
        parameters = new ArrayList<>();
        functions = new ArrayList<>();
    }

    private String toFunction() {
        StringBuilder sb = new StringBuilder();
        StringJoiner sj = new StringJoiner(",", "(", ")");
        sb.append(uri);
        parameters.forEach(p -> {
            String[] keyValue = p.split(" ");
            sj.add(keyValue[1]);
        });
        return sb.append(sj).toString();
    }

    // TODO: hackish.. refactor later.
    public void appendTemplate(OttrTemplate template) {
        int i = parameters.size();
        String functionString = template.toFunction();
        for (String parameter : template.parameters) {
            if (parameter.equals("ottr:IRI ?id")) {
                // add id for the first time
                if (i == 0)
                    parameters.add(parameter);
            } else {
                String[] keyValue = parameter.split(" ");
                String numbered = keyValue[1] + i++; // making sure that there is no duplicates
                // add new parameters
                parameters.add(numbered);
                functionString = functionString.replaceAll("\\?\\b" + keyValue[1] + "\\b", numbered);
            }
        }
        // add new function
        functions.add(functionString);
    }
}
