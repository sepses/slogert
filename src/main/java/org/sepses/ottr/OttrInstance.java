package org.sepses.ottr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class OttrInstance {
    public String uri;
    public List<String> parameters;

    public OttrInstance() {
        parameters = new ArrayList<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringJoiner sj = new StringJoiner(",", "(", ")");
        parameters.forEach(parameter -> sj.add(parameter));
        sb.append(uri).append(sj).append(". \n");
        return sb.toString();
    }
}
