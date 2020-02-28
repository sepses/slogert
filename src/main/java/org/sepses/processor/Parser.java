package org.sepses.processor;

import org.sepses.helper.Template;

import java.io.IOException;
import java.util.Map;

public interface Parser {

    void createOrUpdateTemplate() throws IOException;

    void generateOttrMap() throws IOException;

    void parseLogpaiData() throws IOException;

    Map<String, Template> getHashTemplates();
}
