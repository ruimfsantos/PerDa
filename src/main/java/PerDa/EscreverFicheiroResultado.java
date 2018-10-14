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
public class EscreverFicheiroResultado {

    private int key = 1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String logName = "Logs/" + sdf.format(timestamp) + "_Resultado.csv";
    private String webName = "PerDaInterface/public_html/Resultado.csv";

    public void EscreverFicheiroResultado() {
    }

    public void escreverCSVResultado(String path, String entityType, int risco, double probabilidade, String model, int tokenNr, String entidade)
            throws IOException {

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

        if (path.contains("jdbc")) {
            label = "URL" + keyAux;
        }

        String str = keyAux + ";" + label + ";" + path.replace(";", ":") + ";" + entityType + ";" + risco + ";" + probabilidade + ";" + model
                + ";" + tokenNr + ";" + entidade;

        BufferedWriter writerWeb = new BufferedWriter(new FileWriter(webName, true));
        writerWeb.append(str);
        writerWeb.append('\n');
        this.key++;
        writerWeb.close();
        
        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append(str);
        writerLog.append('\n');
        writerLog.close();

    }

    public void cabecalhoCSVResultadoFile()
            throws IOException {

        String str = "Key;Label;File;EntityType;Risk;Prob;Model;TokenNr;Entity";

        BufferedWriter writerWebFile = new BufferedWriter(new FileWriter(webName, true));
        writerWebFile.append(str);
        writerWebFile.append('\n');
        writerWebFile.close();
        
        BufferedWriter writerLogFile = new BufferedWriter(new FileWriter(logName, true));
        writerLogFile.append(str);
        writerLogFile.append('\n');
        writerLogFile.close();
    }

    public void cabecalhoCSVResultadoDB()
            throws IOException {

        String str = "Key;Label;Table;EntityType;Risk;Prob;Model;ColumnLines;Column";

        BufferedWriter writerWebDB = new BufferedWriter(new FileWriter(webName, true));
        writerWebDB.append(str);
        writerWebDB.append('\n');
        writerWebDB.close();
        
        BufferedWriter writerLogDB = new BufferedWriter(new FileWriter(logName, true));
        writerLogDB.append(str);
        writerLogDB.append('\n');
        writerLogDB.close();
    }
}
