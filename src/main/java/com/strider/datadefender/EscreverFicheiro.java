/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import static java.util.Collections.max;
import java.util.Random;

/**
 * @author ruimfsantos
 */
public class EscreverFicheiro {

    private int key = 0;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String fileName = "Logs/" + sdf.format(timestamp) + ".csv";

    public void EscreverFicheiro() {
    }

    public void escreverCSV(String path, String fileType, int fileNr, String dataType)
            throws IOException {

        /* Random rand = new Random();
        int randomNum = rand.nextInt((90 - 65) + 1) + 65;
        String label = Character.toString((char) randomNum);
        for (int i = 0; i < 2; i++) {
            randomNum = rand.nextInt((90 - 65) + 1) + 65;
            label += Character.toString((char) randomNum);
        }*/
        String keyAux;

        if (key < 10) {
            keyAux = "00000" + key;
        } else if (key < 100) {
            keyAux = "0000" + key;
        } else if (key < 1000) {
            keyAux = "000" + key;
        } else if (key < 10000) {
            keyAux = "00" + key;
        } else if (key < 100000) {
            keyAux = "0" + key;
        } else {
            keyAux = "" + key;
        }

        String label = "LBL" + keyAux;

        String str = keyAux + ";" + label + ";" + path + ";" + fileType + ";" + fileNr + ";" + dataType;

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(str);
        writer.append('\n');

        this.key++;

        writer.close();
        
    }

    public void cabecalhoCSV()
            throws IOException {

        String str = "key;Label;Path;FileType;FileNumber;DataType";

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(str);
        writer.append('\n');

        writer.close();
    }

    public void cabecalhoCSVDatabase()
            throws IOException {

        String str = "key;Label;Table;Column;Number of Rows;DataType";

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(str);
        writer.append('\n');

        writer.close();
    }

}
