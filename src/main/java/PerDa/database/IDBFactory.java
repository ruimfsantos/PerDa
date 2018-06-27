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

package PerDa.database;

import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import PerDa.database.metadata.IMetaData;
import PerDa.database.metadata.MSSQLMetaData;
import PerDa.database.metadata.MySQLMetaData;
import PerDa.database.metadata.OracleMetaData;
import PerDa.database.sqlbuilder.ISQLBuilder;
import PerDa.database.sqlbuilder.MSSQLSQLBuilder;
import PerDa.database.sqlbuilder.MySQLSQLBuilder;
import PerDa.database.sqlbuilder.OracleSQLBuilder;
import PerDa.utils.ICloseableNoException;

/**
 * Aggregate all the various db factories.
 * Will handle the 'pooling' of connections (currently only one).
 * All clients should close the connection by calling the close() method of the AutoCloseable interface.
 * @author Akira Matsuo
 */
public interface IDBFactory extends ICloseableNoException {
    
    Connection getConnection();
    Connection getUpdateConnection();
    IMetaData fetchMetaData() throws DatabaseAnonymizerException;
    ISQLBuilder createSQLBuilder();
    String getVendorName();
    String getUrl();
    
    // Implements the common logic of get/closing of connections
    static abstract class DBFactory implements IDBFactory {
        private static final Logger log = getLogger(DBFactory.class);
        private final Connection connection;
        protected Connection updateConnection;
        private final String vendor;
        private final String url;
        
        DBFactory(String vendorName, String url) throws DatabaseAnonymizerException {
            log.info("    - Connecting to database...");
            connection = createConnection();
            updateConnection = connection;
            vendor = vendorName;
            this.url = url;
        }
        
        public abstract Connection createConnection() throws DatabaseAnonymizerException;
        @Override
        public Connection getConnection() {
            return connection;
        }
        @Override
        public Connection getUpdateConnection() {
            return updateConnection;
        }
        @Override
        public String getVendorName() {
            return vendor;
        }
        @Override
        public String getUrl() {
            return url;
        }
        
        
        @Override
        public void close() {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }
    
    /**
     * Create db factory for given rdbms. Or illegal argument exception.
     * @param dbProps
     * @return db factory instance
     * @throws DatabaseAnonymizerException 
     */
    static IDBFactory get(final Properties dbProps) throws DatabaseAnonymizerException {
        String vendor = dbProps.getProperty("vendor");
        String url = dbProps.getProperty("url");
        
        if ("mysql".equalsIgnoreCase(vendor) || "h2".equalsIgnoreCase(vendor)) {
            return new DBFactory(vendor, url) {
                {
                    updateConnection = createConnection();
                }
                @Override
                public Connection createConnection() throws DatabaseAnonymizerException {
                    return new MySQLDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new MySQLMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MySQLSQLBuilder(dbProps);
                }
            };
        } else if ("mssql".equalsIgnoreCase(vendor)){
            return new DBFactory(vendor, url) {
                @Override
                public Connection createConnection() throws DatabaseAnonymizerException {
                    return new MSSQLDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new MSSQLMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MSSQLSQLBuilder(dbProps);
                }
            };
        } else if ("oracle".equalsIgnoreCase(vendor)) {
            return new DBFactory(vendor, url) {
                @Override
                public Connection createConnection() throws DatabaseAnonymizerException {
                    return new OracleDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new OracleMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new OracleSQLBuilder(dbProps);
                }
            };
        }
        
        throw new IllegalArgumentException("    - Database " + vendor + " is not supported\n");
    }
}

