/*
 * 
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.strider.datadefender.functions;

import com.strider.datadefender.AnonymizerException;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class UtilsTest extends TestCase {
    
    /**
     * Initializes logger
     */
    private static final Logger log = getLogger(UtilsTest.class);    
    
    private static final String FULL_CLASS_NAME  = "com.strider.dataanonymizer.functions.CoreFunctions";
    private static final String EXPECTED_RESULTS_1 = "com.strider.dataanonymizer.functions";
    private static final String EXPECTED_RESULTS_2 = "CoreFunctions";
    
    public UtilsTest(final String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getClassName method, of class Utils.
     * @throws com.strider.datadefender.AnonymizerException
     */
    public void testGetClassName() throws AnonymizerException {
        log.info("Executing testGetClassName ...");
        
        log.debug("Parameter: " + FULL_CLASS_NAME);
        log.debug("Expected result:" + EXPECTED_RESULTS_1);

        final String result = Utils.getClassName(FULL_CLASS_NAME);
        
        assertEquals(EXPECTED_RESULTS_1, result);
    }
    
    /**
     * Test of getMethodName method, of class Utils.
     * @throws com.strider.datadefender.AnonymizerException
     */
    public void testGetMethodName() throws AnonymizerException {        
        log.debug("Parameter: " + FULL_CLASS_NAME);
        log.debug("Expected result:" + EXPECTED_RESULTS_2);

        final String result = Utils.getMethodName(FULL_CLASS_NAME);
        
        assertEquals(EXPECTED_RESULTS_2, result);
    }    
}
