package org.sepses.processor;

import org.sepses.helper.Template;

import java.io.IOException;
import java.util.Map;

public interface Parser {

    /**
     * Creating or loading a set of {@link Template} from input files specified within the configuration file.
     *
     * @throws IOException
     */
    void createOrUpdateTemplate() throws IOException;

    /**
     * Creating an OTTR map file from input files specified within the configuration file.
     *
     * @throws IOException
     */
    void generateOttrMap() throws IOException;

    /**
     * Creating an OTTR instance data from input files as specified within the configuration file.
     *
     * @throws IOException
     */
    void parseLogpaiData() throws IOException;
}
