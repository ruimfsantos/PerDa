/**
 * Copyright 2014-2015, Armenak Grigoryan, and individual contributors as indicated
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
 */
package com.strider.datadefender;

import static java.lang.Double.parseDouble;
import static org.apache.log4j.Logger.getLogger;
import static com.strider.datadefender.utils.AppProperties.loadProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import opennlp.tools.util.Span;

import com.strider.datadefender.file.metadata.FileMatchMetaData;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.RegexNameFinder;
import opennlp.tools.namefind.TokenNameFinder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * @author Armenak Grigoryan
 * @author Redglue
 * @author ruimfsantos
 */
public class FileDiscoverer extends Discoverer {

    private static final Logger log = getLogger(FileDiscoverer.class);

    private static String[] modelList;
    private final ArrayList<String> nomeFicheiros = new ArrayList<String>();
    protected List<FileMatchMetaData> fileMatches;
    private static String atributoDict;

    @SuppressWarnings("unchecked")
    public List<FileMatchMetaData> discover(final Properties dataDiscoveryProperties)
            throws AnonymizerException, IOException, SAXException, TikaException, NullPointerException, Exception, java.lang.IllegalArgumentException {

        /** Task of discover function.
         * Inicia o processo de descoberta dos dados não estruturados.
         * Chama discoverAgainstSingleModel.
         * Imprime os resultados no ficheiro .log e .csv
         */

        // Start count running time
        final long startTime = System.currentTimeMillis();

        log.info("");
        log.info("============================================================");
        log.info(" -> Unstruture Data discovery in process \n");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("   - Probability threshold: < " + probabilityThreshold + " >");

        List<FileMatchMetaData> finalList = new ArrayList<>();

        fileMatches = discoverAgainstSingleModel(dataDiscoveryProperties, probabilityThreshold);

        finalList = ListUtils.union(finalList, fileMatches);

        log.info("");
        log.info("============================================================");
        log.info("============================================================");
        log.info(" -> Quadro resumo dos resultados suspeitos:");
        log.info("    (ler 3 colunas: Pasta, Nome Ficheiro, [ Tipo de dados ])\n");
        
        String[][] auxLogs = new String[finalList.size()][4];
        int aux = 0;

        // Print the suspect results
        for (final FileMatchMetaData data : finalList) {

            log.info("    - "
                    + data.getDirectory() + ", "
                    + data.getFileName() + ", "
                    //   + probability + ", "
                    //   + data.getModel() + ", "
                    + "[ " + data.getDictionariesFound() + " ]");

            auxLogs[aux][0] = data.getDirectory();
            String[] auxFilename = data.getFileName().split("\\.");
            auxLogs[aux][1] = auxFilename.length > 1 ? auxFilename[auxFilename.length - 1] : auxFilename[0];
            auxLogs[aux][2] = data.getDictionariesFound();
            aux++;

        }
        
        // Last information. End Program. Print the execution time and date.
        final long endTime = System.currentTimeMillis();

        final NumberFormat formatter = new DecimalFormat("#0.00");
        log.info("");
        log.info("------------------------------------------------------------");
        log.info(" -> Unstruture data scan completed");
        log.info("");
        log.info("    Execution time: " + formatter.format((endTime - startTime) / 1000d) + " seconds");

        DateFormat dateFormat = new SimpleDateFormat("E, d MMM HH:mm:ss '('zZ')' y"); // semana, dia mês hora fuzo ano
        Date date = new Date();
        log.info("    Finished at   : " + dateFormat.format(date));
        log.info("============================================================");
        log.info("");
        
        // Apoio para escrita no csv
        int[] auxLogsContagem = new int[finalList.size()];

        for (int i = 0; i < finalList.size(); i++) {
            auxLogsContagem[i] = 1;
        }
        
        int j;
        boolean existeIgual = false;
        
        for (int i = 1; i < finalList.size(); i++) {
            for (j = i - 1; j >= 0; j--) {
                existeIgual = false;
                if (auxLogs[i][0].equals(auxLogs[j][0]) && auxLogs[i][1].equals(auxLogs[j][1]) && auxLogs[i][2].equals(auxLogs[j][2]) && auxLogsContagem[j] != 0) {
                    existeIgual = true;
                    break;
                }
            }
            if (existeIgual) {
                auxLogsContagem[i] = 0;
                auxLogsContagem[j]++;
            }
        }

        EscreverFicheiro ef = new EscreverFicheiro();

        ef.cabecalhoCSV();

        for (int i = 0; i < finalList.size(); i++) {

            if (auxLogsContagem[i] > 0) {
                ef.escreverCSV(auxLogs[i][0], auxLogs[i][1], auxLogsContagem[i], auxLogs[i][2]);
            }
        }

        return Collections.unmodifiableList(fileMatches);
    }

    // Task: function to getAllDictionaries and return a List of TokenNameFinders to feed the model
    public List<TokenNameFinder> getDictionariesFileForSearch(String[] dictionaryPathList)
            throws IOException {

        List<TokenNameFinder> findersDict = new ArrayList<>();
        File nodeDict;
        // add all dictionaries
        for (final String dictPath : dictionaryPathList) {
            nodeDict = new File(dictPath);
            final InputStream Dictstream = new FileInputStream(dictPath);

            Dictionary rawdict = new Dictionary(Dictstream);
            findersDict.add(new DictionaryNameFinder(rawdict, nodeDict.getName().replaceAll("\\.\\w+", "")));
            atributoDict = nodeDict.getName().replaceAll("\\.\\w+", "");
            log.info("");
            log.info("    - For Dictionary: " + atributoDict);
        }

        return findersDict;
    }

    // Task: Discovery files according select directory and filetype 
    public void descobertaFicheiros(final Properties fileDiscoveryProperties) throws IOException, NullPointerException {
        
        fileMatches = new ArrayList<>();
        String[] directoryList = null;
        String[] inclusionList = null;
        String[] files_excludedList = null;

        final String directories = fileDiscoveryProperties.getProperty("directories");
        final String inclusions = fileDiscoveryProperties.getProperty("inclusions");
        final String files_excluded = fileDiscoveryProperties.getProperty("files_excluded");

        directoryList = directories.split(",");
        inclusionList = inclusions.split(",");
        files_excludedList = files_excluded.split(",");

        // Let's iterate over directories
        File node;
        log.info("    - Tipo de ficheiros a avaliar: " + inclusions + "\n");
        log.info("    - Pasta a considerar: " + directories + "\n");

        for (final String directory : directoryList) {
            log.info("    - Listing files in folder and subfolders. Please wait ...");
            node = new File(directory);
            final List<File> files = (List<File>) FileUtils.listFiles(node, inclusionList, true);
            
            // Discovery all files to analyze and print
            for (final File fich : files) {
                final String file = fich.getName().toString();

                if (Arrays.asList(files_excludedList).contains(file)) {
                    log.info("*         Ignoring [" + fich.getCanonicalPath() + "]");
                    continue;
                }

                if (!nomeFicheiros.contains(fich.getCanonicalPath())) {
                    nomeFicheiros.add(fich.getCanonicalPath());
                }

                log.info("         Analyzing [" + fich.getCanonicalPath() + "]");
            }
            
            // Print de number of discovery files
            log.info("");
            log.info("      Localizado " + nomeFicheiros.size() + " ficheiro(s)");

        }
    }

    private List<FileMatchMetaData> discoverAgainstSingleModel(final Properties fileDiscoveryProperties, final double probabilityThreshold)
            throws AnonymizerException, IOException, SAXException, TikaException, NullPointerException, Exception {

        // Start running NLP algorithms for each column and collect percentage
        fileMatches = new ArrayList<>();

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        String[] dictionaryPathList = null;

        final String dictionaryPath = fileDiscoveryProperties.getProperty("dictionary_path");

        List<TokenNameFinder> findersDict = null;

        /** Possible values for field NERModel. Read the cases type bellow...
         * NEREntropy - Uses only NER MaxEntropy OpenNLP trained models
         * NERDictionary - Uses only Dictionary XML OPenNLP implementation
         * NERRegex - Uses only Regex OpenNLP models
         * NERPattern - Uses a free query (costumize) in text file
         */
        final String[] NERModel = fileDiscoveryProperties.getProperty("NERmodel").split(",");

        dictionaryPathList = dictionaryPath.split(",");

        // Let's iterate over directories
        Metadata metadata;

        ArrayList<String> DictionariesFound = null;
        List<Probability> probabilityList;
        List<Probability> probabilityListRegex;
        List<Probability> probabilityListDict;

        double averageProbability = 0;

        int aux = 1;

        ArrayList<Integer> posFicheiro = new ArrayList<Integer>();

        // Procura aleatoria dos ficheiros encontrados, avalia o limite definido e o nr ficheiros 
        for (int i = 0; i < Math.min(Integer.parseInt(fileDiscoveryProperties.getProperty("limit")), nomeFicheiros.size()); i++) {

            Random r = new Random();
            int Low = 0;
            int High = nomeFicheiros.size();
            aux = r.nextInt(High - Low) + Low;

            while (posFicheiro.contains(aux)) {
                aux = r.nextInt(High - Low) + Low;
            }

            posFicheiro.add(aux);
        }

        String ficheiro;
        for (int pos : posFicheiro) {
            ficheiro = nomeFicheiros.get(pos);

            final BodyContentHandler handler = new BodyContentHandler(-1);
            final AutoDetectParser parser = new AutoDetectParser();

            metadata = new Metadata();
            String handlerString = "";

            try {
                // read content file... Tentar melhorar para forçar a leitura a utf-8!!!!
                final InputStream stream = new FileInputStream(ficheiro);
                
                if (stream != null) {
                    parser.parse(stream, handler, metadata);
                    handlerString = handler.toString();
                }

            } catch (IOException e) {
                log.info("Unable to read " + ficheiro + ". Ignoring...");
            } catch (Throwable npe) {
                log.info("File error or not supported " + ficheiro + ". Ignoring...");
                continue;
            }

            try {
                String[] caminhoFicheiro = ficheiro.split("\\\\");
                String caminho = "";
                String nomeFicheiro = caminhoFicheiro[caminhoFicheiro.length - 1];
                for (int i = 0; i < caminhoFicheiro.length - 1; i++) {

                    caminho += caminhoFicheiro[i] + "\\";

                }

                log.info("");
                log.info("------------------------------------------------------------");
                log.info(" -> File Analysis: " + caminho + nomeFicheiro);
                log.info("------------------------------------------------------------");

                final FileMatchMetaData result = new FileMatchMetaData(caminho, nomeFicheiro);

                ArrayList<ResultadoSuspeito> listaResultadoSuspeito = new ArrayList<ResultadoSuspeito>();

                InputStream modelInToken = new FileInputStream(fileDiscoveryProperties.getProperty("tokens"));
                TokenizerModel modelToken = new TokenizerModel(modelInToken);
                Tokenizer tokenizer = new TokenizerME(modelToken);

                for (String modelo : NERModel) {

                    switch (modelo) {
                        
                        case "NEREntropy": // Start NEREntropy OpenNLP implementation
                            log.info("");
                            log.info(" >> Maximun Entropy tokenizer process starting ...");
                            log.debug("    - Content:\n" + handlerString + "\n");
                            log.debug("    - Content size: " + handlerString.length());

                            final String models = fileDiscoveryProperties.getProperty("models");
                            modelList = models.split(",");
                            log.debug("");
                            log.debug("    - MaxEnt Model list: " + Arrays.toString(modelList));

                            for (String model : modelList) {
                                log.info("");
                                log.info("    - Processing model: " + model);
                                final Model modelEntropy = createModel(fileDiscoveryProperties, model);
                                final String tokens[] = modelEntropy.getTokenizer().tokenize(handler.toString());
                                final Span nameSpans[] = modelEntropy.getNameFinder().find(tokens);
                                final double[] spanProbs = modelEntropy.getNameFinder().probs(nameSpans);

                                //display names
                                probabilityList = new ArrayList<>();

                                for (int i = 0; i < nameSpans.length; i++) {

                                    final String MaxEntResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s", 
                                            nameSpans[i].getStart(),
                                            nameSpans[i].getEnd(),
                                            model,
                                            //decimalFormat.format(spanProbs[i]),
                                            tokens[nameSpans[i].getStart()]);
                                    log.info(MaxEntResults);
                                    
                                    probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], round(spanProbs[i], 2)));

                                    // Buil list of results    
                                    boolean flag = false;

                                    for (ResultadoSuspeito rs : listaResultadoSuspeito) {
                                        if (rs.getEntidade().equals(tokens[nameSpans[i].getStart()])) {
                                            flag = true;
                                            if (rs.getProbabilidade() < spanProbs[i]) {
                                                rs.setTipoEntidade(modelEntropy.getName());
                                                rs.setNomeFicheiro(ficheiro);
                                                rs.setProbabilidade(round(spanProbs[i], 2));
                                                rs.setModelo(modelo);
                                            }

                                        }

                                    }

                                    if (!flag) {
                                        listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokens[nameSpans[i].getStart()], round(spanProbs[i], 2), modelo, modelEntropy.getName()));
                                    }

                                    modelEntropy.getNameFinder().clearAdaptiveData();
                                    averageProbability = calculateAverage(probabilityList);
                                }

                            }

                            break; // End of NEREntropy

                        case "NERDictionary": // Start Dictionary XML
                            DictionariesFound = new ArrayList<String>();
                            log.info("");
                            log.info(" >> Loading Dictionaries...");
                            findersDict = getDictionariesFileForSearch(dictionaryPathList);

                            log.debug("    - Content:\n" + handlerString + "\n"); // permitir o ver o conteúdo do documento
                            log.debug("    - Content size: " + handlerString.length());
                            probabilityListDict = new ArrayList<>();

                            final String tokensDict[] = tokenizer.tokenize(handler.toString());

                            // Applying Dictionary model
                            for (TokenNameFinder dictionaryNER : findersDict) {

                                final Span DictSpansOnly[] = dictionaryNER.find(tokensDict);

                                for (int i = 0; i < DictSpansOnly.length; i++) {

                                    DictionariesFound.add(DictSpansOnly[i].getType());
                                    
                                    final String DictionaryResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s", 
                                            DictSpansOnly[i].getStart(),
                                            DictSpansOnly[i].getEnd(),
                                            atributoDict,
                                            
                                            tokensDict[DictSpansOnly[i].getStart()]);
                                    log.info(DictionaryResults);
                                    
//                                    // Para dicionário está a ser assumido uma probabilidade de 0,85.
                                    // Embora reconheça o nome, o nome requer contextualização... logo assumo 85% de certeza
                                    // Tentar pensar em alterar a probabilidade para uma forma dinâmica!!!!
                                    probabilityListDict.add(new Probability(tokensDict[DictSpansOnly[i].getStart()], 0.85));

                                    // Buil list of results    
                                    boolean flag = false;

                                    for (ResultadoSuspeito rs : listaResultadoSuspeito) {
                                        if (rs.getEntidade().equals(tokensDict[DictSpansOnly[i].getStart()])) {

                                            flag = true;

                                            if (rs.getProbabilidade() < 0.85) {
                                                rs.setTipoEntidade(atributoDict);
                                                rs.setNomeFicheiro(ficheiro);
                                                rs.setProbabilidade(0.85);
                                                rs.setModelo(modelo);
                                            }
                                        }
                                    }

                                    if (!flag) {
                                        listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokensDict[DictSpansOnly[i].getStart()], 0.99, modelo, atributoDict));
                                    }

                                }

                                dictionaryNER.clearAdaptiveData();
                                averageProbability = calculateAverage(probabilityListDict);

                            }

                            break; // End of NERDictionary
                        
                        case "NERRegex": // REGEX OpenNLP implementation;
                            DictionariesFound = new ArrayList<String>();
                            final Properties RegexProperties = loadProperties("regex.properties");

                            probabilityListRegex = new ArrayList<>();

                            final String tokensRegex[] = tokenizer.tokenize(handler.toString());

                            // Aplicação do modelo REGEX
                            log.info("");
                            log.info(" >> Applying REGEX models...");

                            //Pattern[] patterns = suspList.stream().map(Pattern::compile).toArray(Pattern[]::new);
                            Enumeration<?> enumeration = RegexProperties.propertyNames();
                            Map<String, Pattern[]> regexMap = new HashMap<>();

                            while (enumeration.hasMoreElements()) {
                                String key = (String) enumeration.nextElement();
                                String value = RegexProperties.getProperty(key);
                                Pattern ptregex = Pattern.compile(value);
                                Pattern[] ptterns = new Pattern[]{ptregex};

                                //Map<String, Pattern[]> regexMap = new HashMap<>();
                                regexMap.put(key, ptterns);
                            }

                            RegexNameFinder finder = new RegexNameFinder(regexMap);
                            Span[] resultRegex = finder.find(tokensRegex);

                            // Avaliação dos resultados do modelo REGEX
                            log.info("");
                            log.info("    - Detecting Regular Expression...");

                            String getRegexType = "N/A";
                            for (int i = 0; i < resultRegex.length; i++) {
                                getRegexType = resultRegex[i].getType();
                                
                                final String RegexResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s", 
                                            resultRegex[i].getStart(),
                                            resultRegex[i].getEnd(),
                                            getRegexType, 
                                            tokensRegex[resultRegex[i].getStart()]);
                                    log.info(RegexResults);
                                
                                DictionariesFound.add(getRegexType);

                                //Validar o NIF
                                if (!Validador.NIF(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NIF")) {
                                    continue;
                                }

                                //Validar o NISS
                                if (!Validador.NISS(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NISS")) {
                                    continue;
                                }
                                // default regex probability is 99% always
                                probabilityListRegex.add(new Probability(tokensRegex[resultRegex[i].getStart()], 0.99));

                                // Buil list of results    
                                boolean flag = false;

                                for (ResultadoSuspeito rs : listaResultadoSuspeito) {
                                    if (rs.getEntidade().equals(tokensRegex[resultRegex[i].getStart()])) {
                                        flag = true;
                                        if (rs.getProbabilidade() < 0.99) {
                                            rs.setTipoEntidade(getRegexType);
                                            rs.setNomeFicheiro(ficheiro);
                                            rs.setProbabilidade(0.99);
                                            rs.setModelo(modelo);
                                        }
                                    }
                                }

                                if (!flag) {
                                    listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokensRegex[resultRegex[i].getStart()], 0.99, modelo, getRegexType));
                                }

                            }

                            finder.clearAdaptiveData();
                            averageProbability = calculateAverage(probabilityListRegex);

                            break; // End of NERRegex
                        
                        case "NERPattern": // Start NERPattern... a free query (costumize) in text file
                            log.info("");
                            log.info(" >> Loading Patterns...");
                            log.debug("    - Content:\n" + handlerString + "\n"); // permitir o ver o conteúdo do documento
                            log.debug("    - Content size: " + handlerString.length());

                            String[] patternList = fileDiscoveryProperties.getProperty("pattern").split(",");
                            String ficheiroAnalisar = caminho + nomeFicheiro;
                            log.debug("    - Ficheiro a analisar: " + ficheiroAnalisar);

                            for (String filePattern : patternList) {
                                File pattern = new File(filePattern);
                                String[] auxNomeAtributo = filePattern.split("\\\\");

                                log.info("");
                                log.info("    - Evaluating Pattern: " + auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""));
                                Scanner scanPattern = new Scanner(pattern);
                                
                                ArrayList<String> consulta = new ArrayList<String>();

                                while (scanPattern.hasNextLine()) {
                                    //log.info("Confirmação de entrada no while");
                                    String auxPattern = scanPattern.nextLine();
                                    consulta.add(auxPattern);
                                }

                                log.debug("");
//                                log.debug("    - Dados a consultar: " + Consulta);
                                for (String s : consulta) {
                                    Pattern patternCompile = Pattern.compile(s, Pattern.CASE_INSENSITIVE);  // case-insensitive matching
                                    Matcher matcher = patternCompile.matcher(handlerString);
                                    //log.debug("    - Dados a consultar: " + s);

                                    while (matcher.find()) { // find the next match
                                        
                                        final String PatternResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s", 
                                            matcher.start(),
                                            matcher.end(),
                                            auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""), 
                                            matcher.group());
                                            log.info(PatternResults);
     
//                                            log.info("         [" + matcher.start() + ".." + matcher.end() + "]"
//                                                + "  \t" + matcher.group()); // Padrão encontrado

                                        // Buil list of results    
                                        boolean flag = false;
                                        for (ResultadoSuspeito rs : listaResultadoSuspeito) {
                                            if (rs.getEntidade().equals(matcher.group())) {
                                                flag = true;
                                                if (rs.getProbabilidade() < 0.98) {
                                                    rs.setTipoEntidade(auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""));
                                                    rs.setNomeFicheiro(ficheiroAnalisar);
                                                    rs.setProbabilidade(0.98);
                                                    rs.setModelo(modelo);
                                                }
                                            }
                                        }

                                        if (!flag) {
                                            listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiroAnalisar, matcher.group(), 0.98, modelo, auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", "")));
                                        }

                                    }

                                }

                            }

                            break; // End of NERPattern
                        // End of Switch NERModel: Dictionary, MaxEntropy, Regex and Pattern
                        }

                }

                // Compare results and print the best result
                log.info("");
                log.info("----------------------------------------------------");
                log.info(" -> Resultados suspeitos do ficheiro: " + caminho + nomeFicheiro);
                log.info("    (ler 4 colunas: Tipo da NER, Probabilidade, Modelo, Entidade)\n");

                ArrayList<String> tiposEncontrados = new ArrayList<String>();
                int auxNrResultados = 0;
                for (ResultadoSuspeito rs : listaResultadoSuspeito) {
                    
                    final String FinalResults = String.format("    - %-15s %-9s %-13s %s", 
                                            rs.getTipoEntidade(),
                                            rs.getProbabilidade(), 
                                            rs.getModelo(),
                                            rs.getEntidade());
                                    log.info(FinalResults);
                    
                    //log.info(rs.toString()); // Check ResultadoSuspeito Class
                    if (!tiposEncontrados.contains(rs.getTipoEntidade())) {
                        tiposEncontrados.add(rs.getTipoEntidade());
                    }
                    auxNrResultados++;
                }
                log.info("");
                log.info("    Encontrados " + auxNrResultados + " tokens de " + handlerString.length() + " verificados");

                result.setDictionariesFound(tiposEncontrados);
                fileMatches.add(result);

            } catch (NullPointerException npe) {
                npe.printStackTrace(System.out);
                log.info("NameFinder Model can't be applied to " + ficheiro + ". Ignoring...");
            }

        }

        return fileMatches;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private String[] sentenceDetector(String handlerString) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isToken(String palavra, ArrayList<String> dicionario) {
        for (String d : dicionario) {
            if (palavra.equals(d)) {
                return true;
            }
        }
        return false;
    }

}
