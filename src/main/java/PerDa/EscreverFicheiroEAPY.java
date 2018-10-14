/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerDa;

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
public class EscreverFicheiroEAPY {

    private int key = 1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String logName = "Logs/" + sdf.format(timestamp) + "_Extensao.csv";
    private String webName = "PerDaInterface/public_html/Extensao.csv";

    public void EscreverFicheiroEAPY() {
    }

    public void escreverCSV(String path, String fileType, int fileNr, String dataType, int risco)
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
        String label = "RPT" + keyAux;
        
        if(path.contains("jdbc"))
            label = "URL" + keyAux;

        //String str = keyAux + ";" + label + ";" + path.replace(";", ":") + ";" + fileType + ";" + fileNr + ";" + dataType+ ";" + risco;
        String strLog = label + ";" + fileType + ";" + path.replace(";", ":") + ";" + fileNr + ";" + dataType+ ";" + risco;

        BufferedWriter writerWEB = new BufferedWriter(new FileWriter(webName, true));
        writerWEB.append(strLog);
        writerWEB.append('\n');
        this.key++;
        writerWEB.close();
        
        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append(strLog);
        writerLog.append('\n');
        writerLog.close();
        
    }

    public void cabecalhoCSV()
            throws IOException {

        String str = "Key;Label;Repository;FileType;FileNumber;DataType;Risk";
        String strLog = "Key;FileType;Repository;FileNumber;DataType;Risk";

        BufferedWriter writerWEB = new BufferedWriter(new FileWriter(webName, true));
        writerWEB.append(strLog);
        writerWEB.append('\n');
        writerWEB.close();
        
        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append(strLog);
        writerLog.append('\n');
        writerLog.close();
    }

    public void cabecalhoCSVDatabase()
            throws IOException {

        String str = "Key;Label;Url;TableName;ColumnsNumber;DataType;Risk";
        String strLog = "Key;TableName;Repository;ColumnsNumber;DataType;Risk";

        BufferedWriter writerWEB = new BufferedWriter(new FileWriter(webName, true));
        writerWEB.append(strLog);
        writerWEB.append('\n');
        writerWEB.close();
        
        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append(strLog);
        writerLog.append('\n');
        writerLog.close();
    }

}
