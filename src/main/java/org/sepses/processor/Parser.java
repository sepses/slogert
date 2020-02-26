package org.sepses.processor;

import org.sepses.helper.Template;

import java.io.IOException;
import java.util.Map;

public interface Parser {

    void createOrUpdateTemplate(String logpaiStructure, String logpaiData, Boolean isOverride) throws IOException;

    String parseLogpaiData(String logpaiData) throws IOException;

    Map<String, Template> getHashTemplates();
}
