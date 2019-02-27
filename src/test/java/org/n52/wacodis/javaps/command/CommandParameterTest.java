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
public class CommandParameterTest {
    
    public CommandParameterTest() {}

    /**
     * Test of toString method, of class CommandParameter.
     */
    @Test
    public void testToString() {
        CommandParameter testParam = new CommandParameter("-key", "value");
        CommandParameter unamedParam = new CommandParameter("", "value");
        CommandParameter flag = new CommandParameter("-key", "");
        
        assertEquals("-key value", testParam.toString());
        assertEquals("value", unamedParam.toString());
        assertEquals("-key", flag.toString());
    }
    
    
}
