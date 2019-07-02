/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class ProcessCommand extends AbstractProcessCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCommand.class);

    public ProcessCommand(String processApplication) {
        super(processApplication);
    }

    @Override
    public ProcessResult execute() throws InterruptedException {
        CommandParser parser = new CommandParser(this);
        int returnCode = -1;

        ProcessResult result = new ProcessResult(returnCode, "");
        try {
            Process process = parser.parseCommand().start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    StrBuilder builder = new StrBuilder();

                    try {
                        while ((line = input.readLine()) != null) {
                            builder.appendln(line);
                        }
                        result.setOutputMessage(builder.toString());
                        LOGGER.debug(result.getOutputMessage());

                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                        LOGGER.debug("Error while reading process output", ex);
                    }
                }
            }).start();

            returnCode = process.waitFor();
            result.setResultCode(returnCode);

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.debug("Error while prcessing command", ex);
        } finally {
            return result;
        }
    }

}
