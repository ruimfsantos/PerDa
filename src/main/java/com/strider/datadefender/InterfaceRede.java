/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author ruimfsantos
 */
public class InterfaceRede {

    private static final Logger log = getLogger(InterfaceRede.class);

    public InterfaceRede() {
    }

    public void obterSharedFolders() {

        log.info("    - Interfaces de Redes Partilhadas:");
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) ias.nextElement();
                    if (ia instanceof Inet4Address) {
                        log.info("         " + ia.getHostAddress());

                    }
                }

            }
        } catch (SocketException ex) {

        }
        log.info("");

        try {
            log.info("    - Recursos Partilhados:");
            String line = null;
            String[] commands = new String[]{"cmd", "/C", "net share"};
            Process child = Runtime.getRuntime().exec(commands);
            InputStream ins = child.getInputStream();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(ins));
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            String aux = "";
            while (!(line = buffReader.readLine()).trim().substring(0, 9).equals(
                    "O comando")) {
                aux = line.replace(" ", "#");
                int i = aux.indexOf("#");

                while (aux.charAt(i) == '#') {
                    i++;
                }
                aux = aux.substring(i);
                i = aux.indexOf("#");
                aux = aux.substring(0, i);
                log.info("         " + aux);

            }
        } catch (Exception exp) {
            log.info("         Ouch - " + exp);
        }
        log.info("");

    }

//    public void gerarFileProperties(ArrayList<String> diretorios) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter("filediscovery_tg.properties"));
//
//        writer.write("# Definição do limite dos resultados a descobrir\n");
//        writer.append("probability_threshold=0.50\n");
//        writer.append("tokens=D:/Prototipo/binfiles/pt-token.bin\n");
//        writer.append("sentences=D:/Prototipo/binfiles/pt-sent.bin\n");
//        writer.append("# Tipo de Modelos NER/NLP\n");
//        writer.append("Nome=D:/Prototipo/binfiles/pt-ner-nome.bin\n");
//        writer.append("Name=D:/Prototipo/binfiles/en-ner-name.bin\n");
//        writer.append("#Apelido=D:/Prototipo/OpenNLP-CLI_Testes/pt-sdtApedidos.bin\n");
//        writer.append("# Modelos NER/NLP a a incluir na análise. De acordo com os nomes anteriores\n");
//        writer.append("models=Name\n");
//        writer.append("# Dicionários a selecionar\n");
//        writer.append("dictionary_path=D:/Prototipo/dict_xml/Nome.xml\n");
//        writer.append("# Definição da pasta, subpastas e/ou rede\n");
//        writer.append("directories=");
//        for (String diretorio : diretorios) {
//            writer.append(diretorio + ",");
//        }
//        writer.append("\n");
//        writer.append("# Extensão do(s) ficheiro(s) a descobrir e analisar\n");
//        writer.append("inclusions=docx\n");
//        writer.append("files_excluded=a.txt\n");
//        writer.append("# Limitar a procura dos ficheiros encontrados\n");
//        writer.append("# Possiveis modos de procura\n");
//        writer.append("NERmodel=NERDictionary,NEREntropy,NERRegex\n");
//
//        writer.close();
//
//    }
    void gerarFileProperties(ArrayList<String> diretorios) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
