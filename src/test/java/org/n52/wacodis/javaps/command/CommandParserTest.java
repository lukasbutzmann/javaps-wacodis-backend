/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wacodis.javaps.command;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:arne.vogt@hs-bochum.de">Arne Vogt</a>
 */
public class CommandParserTest {
    
    public CommandParserTest() {
    }

    /**
     * Test of parseCommand method, of class CommandParser.
     */
    @Test
    public void testParseCommand() {
        ProcessCommand pc = new ProcessCommand("mvn");
       
        CommandParameter p1 = new CommandParameter();
        p1.setParameter("-version");
        pc.addParameter(p1);
        
        CommandParser cp = new CommandParser(pc);
        ProcessBuilder pb = cp.parseCommand();

        if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
            assertEquals("cmd.exe /c mvn -version", String.join(" ", pb.command()));
        else{
            assertEquals("mvn -version", String.join(" ", pb.command()));
        }
    }
    
}
