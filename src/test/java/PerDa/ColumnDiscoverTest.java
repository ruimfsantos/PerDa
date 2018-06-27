/*
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
package PerDa;

import PerDa.ColumnDiscoverer;
import PerDa.AnonymizerException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import PerDa.database.H2DB;
import PerDa.database.metadata.MatchMetaData;
import PerDa.requirement.Requirement;
import PerDa.utils.RequirementUtils;

/**
 * @author Akira Matsuo
 */
public class ColumnDiscoverTest extends H2DB {
    
    private static final String FILE_NAME = "target/test-classes/utest-coldis-req.xml";

    
    @SuppressWarnings("serial")
    private final Properties sampleCProps = new Properties() {{ setProperty("fname", "true" ); }};
    @SuppressWarnings("serial")
    private final Properties badCProps = new Properties() {{ setProperty("la colonna non esiste", "true" ); }};

    @Test
    public void testWithColumns() throws AnonymizerException, IOException { 
        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
        final List<MatchMetaData> suspects = discoverer.discover(factory, sampleCProps, new HashSet<String>());
        assertEquals(1, suspects.size());
        assertEquals("ju_users.fname", suspects.get(0).toString());
        assertEquals("null.ju_users.fname(varchar)", suspects.get(0).toVerboseStr());
    }

    @Test
    public void testWithTablesColumnsAndRequirements() throws AnonymizerException, IOException { 
        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
        final List<MatchMetaData> suspects = discoverer.discover(factory, sampleCProps, 
            new HashSet<String>(Arrays.asList("ju_users")));
        assertEquals(1, suspects.size());
        assertEquals("ju_users.fname", suspects.get(0).toString());
        assertEquals("null.ju_users.fname(varchar)", suspects.get(0).toVerboseStr());
        
        new File(FILE_NAME).delete(); // try to delete file if it exists
        discoverer.createRequirement(FILE_NAME);
        // sanity check requirement
        final Requirement requirement = RequirementUtils.load(FILE_NAME);
        assertNotNull(requirement);
        assertEquals("Autogenerated Template Client", requirement.getClient());
        assertEquals(1, requirement.getTables().size());
        assertEquals("ju_users", requirement.getTables().get(0).getName());
        assertEquals(1, requirement.getTables().get(0).getColumns().size());
        assertEquals("fname", requirement.getTables().get(0).getColumns().get(0).getName());
    }

//    @Test
//    public void testWithBadTablesColumns() throws AnonymizerException { 
//        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
//        final List<MatchMetaData> suspects = discoverer.discover(factory, sampleCProps, 
//            new HashSet<String>(Arrays.asList("il tavolo non esiste")));
//        assertTrue(suspects.isEmpty());
//    }

//    @Test
//    public void testWithTablesBadColumns() throws AnonymizerException { 
//        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
//        final List<MatchMetaData> suspects = discoverer.discover(factory, badCProps, 
//            new HashSet<String>(Arrays.asList("ju_users")));
//        assertTrue(suspects.isEmpty());
//    }
}
