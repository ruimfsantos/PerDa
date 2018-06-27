/*
 * 
 * Copyright 2014-2016, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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

package PerDa.extensions;

import PerDa.functions.CoreFunctions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Matthew Eaton
 */
public class BiographicFunctions extends CoreFunctions {

    private static final Logger log = getLogger(BiographicFunctions.class);
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     */
    public String randomStringSIN() {
        final Random random = new Random();
        int[] sinDigits = new int[9];
        for (int i = 0; i < sinDigits.length; i++) {
            sinDigits[i] = random.nextInt(9);
        }

        while (!isValidSIN(sinDigits)) {
            sinDigits = increment(sinDigits);
        }

        final StringBuilder sin = new StringBuilder(9);
        for (final int digit : sinDigits) {
            sin.append(String.valueOf(digit));
        }
        
        log.debug("Generated SIN:[" + sin.toString() +"]");
        
        return sin.toString();

    }
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     */
    public int randomNumericSIN() {
        final Random random = new Random();
        int[] sinDigits = new int[9];
        for (int i = 0; i < sinDigits.length; i++) {
            sinDigits[i] = random.nextInt(9);
        }

        while (!isValidSIN(sinDigits)) {
            sinDigits = increment(sinDigits);
        }

        final StringBuilder sin = new StringBuilder(9);
        for (final int digit : sinDigits) {
            sin.append(String.valueOf(digit));
        }
        
        log.debug("Generated SIN:[" + sin.toString() +"]");
        
        return Integer.valueOf(sin.toString());

    }    
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     * @throws java.text.ParseException
     */
    public java.sql.Date randomBirthDate() throws java.text.ParseException{

        final GregorianCalendar gc = new GregorianCalendar();
        final int year = randBetween(1900, 2016);
        gc.set(GregorianCalendar.YEAR, year);
        final int dayOfYear = randBetween(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        
        final String birthDate = prependZero(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "-" + 
               prependZero((gc.get(GregorianCalendar.MONTH) + 1)) + "-" + 
               gc.get(GregorianCalendar.YEAR);
        log.debug("BirthDate:[" + birthDate +"]");
        
        final DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        final java.sql.Date date = new java.sql.Date(format.parse(birthDate).getTime());
       
        log.debug("Generated BirthDate:[" + date.toString() +"]");

        return date;

    }
    
    private static String prependZero(final int num) {
        String dayStr;
        if (num <=9) {
            dayStr = "0" + String.valueOf(num);
        } else {
            dayStr = String.valueOf(num);
        }
        return dayStr;
    }

    private static int randBetween(final int start, final int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }    

    /**
     * Increment array of integers by 1, starting from end of array
     * @param sinDigits Array of integers representing a sin
     * @return Incremented array of integers
     */
    private int[] increment(int[] sinDigits) {
        for (int i=8; i>=0; i--) {
            if (sinDigits[i] < 9) {
                sinDigits[i]++;
                break;
            } else {
                sinDigits[i] = 0;
            }
        }

        return sinDigits;
    }

    /**
     * Check if array of integers represents a valid sin
     * @param sinDigits Array of integers representing a sin
     * @return true if valid sin, false if not
     */
    private boolean isValidSIN(final int[] sinDigits) {
        int check = 0;

        for (int i=0; i<sinDigits.length; i++) {
            if ((i % 2) == 0) {
                // multiple by 1
                check += sinDigits[i];
            } else {
                // multiple by 2
                int tempCheck = sinDigits[i] * 2;
                if (tempCheck > 9) {
                    tempCheck = (tempCheck / 10) + (tempCheck % 10);
                }
                check += tempCheck;
            }
        }
        return (check % 10) == 0;
    }

    /**
     * Check if String represents a valid sin
     * @param sin String representing a sin
     * @return true if valid sin, false if not
     */
    public boolean isValidSIN(final String sin) {
        if (sin == null || sin.length() != 9 ) {
            return false;
        }
        int[] sinDigits = new int[sin.length()];

        for (int i=0; i<sin.length(); i++) {
            sinDigits[i] = Integer.parseInt(sin.substring(i, i+1));
        }

        return (isValidSIN(sinDigits));

    }
}