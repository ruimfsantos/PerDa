/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerDa;

import static PerDa.utils.AppProperties.loadProperties;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import static java.util.Collections.max;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.cli.CommandLine;
import static thredds.featurecollection.FeatureCollectionConfig.PartitionType.file;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author ruimfsantos
 */
public class EscreverFicheiroResumo {

    private static final Logger log = getLogger(DataDefender.class);

    private int key = 1;

    private int numeroFicheirosEncontrados = 1;
    private int totalCampos = 1;
    

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String logName = "Logs/" + sdf.format(timestamp) + "_Resumo.csv";
    private String webName = "PerDaInterface/public_html/Resumo.csv";

    public void EscreverFicheiroResumo() {
    }

    public void escreverCSVResumo(String extensao, String path, String ficheiro, String tipoEntidade, int risco, int numeroResultados, int tokenNr)
            throws IOException, DataDefenderException {

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

        // Cálculos para densidade de Risco
        DecimalFormat dr = new DecimalFormat("#0.00000");
        float nr = (float) 1 / numeroResultados;
        float rsk = (float) risco / 5;

        int countWord = 0;
        
        try {
            final InputStream stream = new FileInputStream(path + ficheiro);
            InputStreamReader input = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(input);
            String line;

            while ((line = reader.readLine()) != null) {

                if (!(line.equals(""))) {
                    // \\s+ is the space delimiter in java
                    String[] wordList = line.split("\\s+");

                    countWord += wordList.length;
                }

            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        float racioToken = (float) tokenNr / countWord;

        final Properties sizeRacioProperties = loadProperties("sizeracio.properties");
        double sizeFile_01 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_01"));
        double sizeFile_02 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_02"));
        double sizeFile_03 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_03"));
        double countWord_01 = Double.parseDouble(sizeRacioProperties.getProperty("countWord_01"));
        double countWord_02 = Double.parseDouble(sizeRacioProperties.getProperty("countWord_02"));
        double racioSizeFile;

        if (countWord < countWord_01) {
            racioSizeFile = sizeFile_01;
        } else if (countWord < countWord_02) {
            racioSizeFile = sizeFile_02;
        } else {
            racioSizeFile = sizeFile_03;
        }

        String auxDensidadeRisco = dr.format(nr * rsk * racioSizeFile);

        // Palavra a escrever no CSV Resumo
        String str = keyAux + ";" + label + ";" + extensao + ";" + path.replace(";", ":") + ";" + ficheiro + ";" + tipoEntidade + ";" + risco + ";" + auxDensidadeRisco + ";" + tokenNr + ";" + countWord;

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
    
    public void escreverCSVResumoBD(String url, String tabela, String tipoDados, int risco, int numeroResultados, int totalCampos, int numeroColunas)
            throws IOException, DataDefenderException {

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

        if (url.contains("jdbc")) {
            label = "URL" + keyAux;
        }

        // Cálculos para densidade de Risco
        DecimalFormat dr = new DecimalFormat("#0.00000");
        float nr = (float) 1 / numeroResultados;
        float rsk = (float) risco / 5;

        

        float racioToken = (float) numeroResultados / totalCampos;

        final Properties sizeRacioProperties = loadProperties("sizeracio.properties");
        double sizeFile_01 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_01"));
        double sizeFile_02 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_02"));
        double sizeFile_03 = Double.parseDouble(sizeRacioProperties.getProperty("sizeFile_03"));
        double countWord_01 = Double.parseDouble(sizeRacioProperties.getProperty("countWord_01"));
        double countWord_02 = Double.parseDouble(sizeRacioProperties.getProperty("countWord_02"));
        double racioSizeFile;

        if (totalCampos < countWord_01) {
            racioSizeFile = sizeFile_01;
        } else if (totalCampos < countWord_02) {
            racioSizeFile = sizeFile_02;
        } else {
            racioSizeFile = sizeFile_03;
        }

        String auxDensidadeRisco = dr.format(nr * rsk * racioSizeFile);

        // Palavra a escrever no CSV Resumo
        String str = keyAux + ";" + label + ";"  + url.replace(";", ":") + ";" + tabela + ";" + tipoDados + ";" + risco + ";" + auxDensidadeRisco + ";" + numeroResultados + ";" + totalCampos;

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

    public void escreverFicheirosLocalizados(String tempoLocalizacao, String tempoAnalise) throws IOException {
        BufferedWriter writerWeb = new BufferedWriter(new FileWriter(webName, true));
        writerWeb.append("Número de ficheiros localizados:;" + this.getNumeroFicheirosEncontrados()+";Tempo de localização:;"+ tempoLocalizacao+";tempo analise:;"+tempoAnalise);
        writerWeb.append('\n');
        writerWeb.close();

        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append("Número de ficheiros localizados:;" + this.getNumeroFicheirosEncontrados()+";Tempo de localização:;"+ tempoLocalizacao+";tempo analise:;"+tempoAnalise);
        writerLog.append('\n');
        writerLog.close();

    }
   

    public int getNumeroFicheirosEncontrados() {
        return numeroFicheirosEncontrados;
    }

    public void setNumeroFicheirosEncontrados(int numeroFicheirosEncontrados) {
        this.numeroFicheirosEncontrados = numeroFicheirosEncontrados;
    }

  

    public void cabecalhoCSVResumoFile()
            throws IOException {

        String str = "Key;Label;Tipo Ficheiro;Pasta;Ficheiro;Tipo de Dado;Clas. Risco;Densidade Risco;Nr Dados;Tamanho Documento";

        BufferedWriter writerWebFile = new BufferedWriter(new FileWriter(webName, true));
        writerWebFile.append(str);
        writerWebFile.append('\n');
        writerWebFile.close();

        BufferedWriter writerLogFile = new BufferedWriter(new FileWriter(logName, true));
        writerLogFile.append(str);
        writerLogFile.append('\n');
        writerLogFile.close();
    }

    public void cabecalhoCSVResumoDB()
            throws IOException {

        String str = "Key;Label;URL;Tabela;Tipo de Dado;Clas. Risco;Densidade Risco;Campos Analisados;Total Campos";

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
