/*
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

import PerDa.DataDefenderException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.Random;




/**
 * Most of the code is copied from this page: http://www.rgagnon.com/javadetails/java-0288.html
 *
 * Modified by Armenak Grigoryan and Redglue
 */
public class ApplicationLock {
    private final String appName;
    private File file;
    private FileChannel channel;
    private FileLock lock;


    private static final Logger log = getLogger(ApplicationLock.class);

    /**
     * Constructor
     *
     * @param appName application name
     */
    public ApplicationLock(final String appName) {
        this.appName = appName;
    }

    /**
     * Returns true if there is another instance of the application is running.
     * Otherwise returns false.
     *
     * @return boolean
     * @throws PerDa.DataDefenderException
     */
    public boolean isAppActive() throws DataDefenderException {
        try {
            Random rand = new Random();
            int n = rand.nextInt(100) + 1;
            file = new File
                 (System.getProperty("user.home"), appName + "_" + n + ".tmp");
            channel = new RandomAccessFile(file, "rw").getChannel();
            log.info("");
            log.debug("Creating lock file " + file.getName());

            try {
                lock = channel.tryLock();
                log.debug("Locking file ...\n");
                log.info("");
            } catch (OverlappingFileLockException | IOException e) {
                // already locked
                log.error("File  " + file.getName() + " already locket");
                log.info("");
                closeLock();
                return true;
            }

            if (lock == null) {
                closeLock();
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                    // destroy the lock when the JVM is closing
                    @Override
                    public void run() {
                        try {
                            log.debug("Closing lock file");
                            log.info("");
                            closeLock();
                            deleteFile();
                        } catch (DataDefenderException ae) {
                            log.error("Problem closing file lock");
                            log.info("");
                        }
                    }
                });
            return false;
        } catch (FileNotFoundException fnfe) {
            closeLock();
            return true;
        }
    }

    private void closeLock() throws DataDefenderException {
        try {
            lock.release();
        } catch (IOException e) {
            throw new DataDefenderException("Problem releasing file lock", e);
        }

        try {
            channel.close();
        } catch (IOException e) {
            throw new DataDefenderException("Problem closing channel", e);
        }
    }

    private void deleteFile() {
        file.delete();
    }

}
