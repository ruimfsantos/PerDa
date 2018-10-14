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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import static java.util.Collections.max;
import java.util.Random;

/**
 * @author ruimfsantos
 */
public class EscreverFicheiroGovernacao {

    private int key = 1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String logName = "Logs/" + sdf.format(timestamp) + "_Governacao.csv";
    private String webName = "PerDaInterface/public_html/Governacao.csv";

    public void EscreverFicheiroGovernacao() {
    }

    public void escreverCSVGovernacao(String label, String fileType, int risco, String repositorio, String path, String tipoEntidade)
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

        // Palavra a escrever no CSV Resumo
        String str = keyAux + ";" + label + ";" + fileType + ";" + risco + ";" + repositorio + ";" + path.replace(";", ":") + ";" + tipoEntidade;

        BufferedWriter writerLog = new BufferedWriter(new FileWriter(logName, true));
        writerLog.append(str);
        writerLog.append('\n');
        this.key++;
        writerLog.close();

        BufferedWriter writerWeb = new BufferedWriter(new FileWriter(webName, true));
        writerWeb.append(str);
        writerWeb.append('\n');
        writerWeb.close();

    }

    public void cabecalhoCSVGovernacaoFile()
            throws IOException {

        String strLogFile = "Key;Label;extensão.TipoFicheiro;classificação.Risco;pertence.PartilhaFicheiros;estrutura.Directório;contém.TipoDados";

        BufferedWriter writerLogFile = new BufferedWriter(new FileWriter(logName, true));
        writerLogFile.append(strLogFile);
        writerLogFile.append('\n');
        writerLogFile.close();

        BufferedWriter writerWebFile = new BufferedWriter(new FileWriter(webName, true));
        writerWebFile.append(strLogFile);
        writerWebFile.append('\n');
        writerWebFile.close();
    }

    public void cabecalhoCSVGovernacaoDB()
            throws IOException {

        String strLogDB = "Key;Label;classe.TipoColunas;classificação.Risco;pertence.BaseDados;estrutura.Tabela;contém.TipoDados";

        BufferedWriter writerLogDB = new BufferedWriter(new FileWriter(logName, true));
        writerLogDB.append(strLogDB);
        writerLogDB.append('\n');
        writerLogDB.close();

        BufferedWriter writerWebDB = new BufferedWriter(new FileWriter(webName, true));
        writerWebDB.append(strLogDB);
        writerWebDB.append('\n');
        writerWebDB.close();
    }

}
