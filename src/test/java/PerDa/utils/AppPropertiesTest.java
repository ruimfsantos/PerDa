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
package PerDa.utils;

import static PerDa.utils.AppProperties.loadProperties;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import PerDa.DataDefenderException;


/**
* @author Akira Matsuo
*/
public class AppPropertiesTest {
    
    @Test(expected=DataDefenderException.class)
    public void testLoadPropertiesNoFile() throws DataDefenderException {
        loadProperties("/do/not/exist.properties");
    }

    @Test
    public void testLoadPropertiesValid() throws DataDefenderException {
        final String path = this.getClass().getClassLoader().getResource("AppPropertiesTest.properties").getPath();
        final Properties props = loadProperties(path);
        assertNotNull(props);
        assertEquals("yyy", props.get("xxx"));
    }
}
