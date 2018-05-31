/*
 * 
 * Copyright 2014-2017, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.report;

import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.database.sqlbuilder.ISQLBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public class ReportUtil {
    
    private static final Logger log = getLogger(ReportUtil.class);

    public static int rowCount(final IDBFactory factory, final String tableName) {
        
        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        final String table = sqlBuilder.prefixSchema(tableName);

        // Getting number of records in the table                
        final String queryCount = sqlBuilder.buildSelectWithLimit("SELECT count(*) " + " FROM " + table, 0);
        log.debug("Executing query against database: " + queryCount);

        int rowCount = 0;
        try (Statement stmt = factory.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(queryCount); ) 
        {
            resultSet.next();
            rowCount = resultSet.getInt(1);
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }        
        
        return rowCount;
    }
    
    public static List<String> sampleData(final IDBFactory factory, final String tableName, final String columnName) {
        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        
        final String querySample = sqlBuilder.buildSelectWithLimit(
            "SELECT " + columnName + 
            " FROM " + tableName + 
            " WHERE " + columnName   + " IS NOT NULL ", 5);                
            log.debug("Executing query against database: " + querySample);

        final List<String> sampleDataList = new ArrayList<String>();
        try (Statement stmt = factory.getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery(querySample);) {
            while (resultSet.next()) {
               sampleDataList.add(resultSet.getString(1)); 
            }                    
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }        
        
        return sampleDataList;
    }    
}
