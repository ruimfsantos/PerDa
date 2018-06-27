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
package PerDa;

import static java.lang.Double.parseDouble;
import static org.apache.log4j.Logger.getLogger;
import static PerDa.utils.AppProperties.loadProperties;

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

import PerDa.file.metadata.FileMatchMetaData;
import PerDa.utils.CommonUtils;

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

    @SuppressWarnings("unchecked")
    public List<FileMatchMetaData> discover(final Properties dataDiscoveryProperties)
            throws AnonymizerException, IOException, SAXException, TikaException, NullPointerException, Exception, java.lang.IllegalArgumentException {

        /**
         * Task of discover function. Inicia o processo de descoberta dos dados
         * não estruturados. Chama discoverAgainstSingleModel. Imprime os
         * resultados no ficheiro .log e .csv
         */
        // Start count running time
        final long startTime = System.currentTimeMillis();

        log.info("");
        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(" -> Unstruture Data discovery in process \n");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("   - Probability threshold: < " + probabilityThreshold + " >");

        List<FileMatchMetaData> finalList = new ArrayList<>();

        fileMatches = discoverAgainstSingleModel(dataDiscoveryProperties, probabilityThreshold);

        finalList = ListUtils.union(finalList, fileMatches);

        log.info("");
        log.info(CommonUtils.fixedLengthString('=', +80));;
        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(" -> Quadro resumo dos resultados suspeitos:");
        log.info("    (ler 4 colunas: Pasta, Nome Ficheiro, Risco, [ Tipo de dados ])\n");
        
        int auxTamAuxLogs = 0;
        
         for (final FileMatchMetaData data : finalList) {
             auxTamAuxLogs += data.getRepository().split(",").length;
         }

        String[][] auxLogs = new String[auxTamAuxLogs][5];
        int aux = 0;

        // Print the suspect results
        for (final FileMatchMetaData data : finalList) {
            
            String[] auxRep= data.getRepository().split(",");
            
            for(int sRep= 0 ; sRep < auxRep.length; sRep++){
                // Agregação dos resultados para o ficheiro log/cmd
                log.info("    - "
                        + data.getDirectory() + ", "
                        + data.getFileName() + ", "
                        + data.getGrauRisco() + ", "
                        //   + probability + ", "
                        //   + data.getModel() + ", "
                        + "[ " + data.getDictionariesFound() + " ]");

                // Agregação dos resultados para impressão do csv
                auxLogs[aux][0] = auxRep[sRep];
                String[] auxFilename = data.getFileName().split("\\.");

                // TODO: Criar parametro de entrada para a aplicação
                boolean edocMode = false;

                if (edocMode) {
                    auxLogs[aux][1] = getDocProcessType(data.getDirectory());
                } else {
                    auxLogs[aux][1] = auxFilename.length > 1 ? auxFilename[auxFilename.length - 1] : auxFilename[0];
                }

                auxLogs[aux][2] = data.getDictionariesFound();
                auxLogs[aux][4] = "" + data.getGrauRisco();
                aux++;
            }
        }

        // Last information. End Program. Print the execution time and date.
        final long endTime = System.currentTimeMillis();

        final NumberFormat formatter = new DecimalFormat("#0.00");
        log.info("");
        log.info(CommonUtils.fixedLengthString('-', +70));
        log.info(" -> Unstruture data scan completed");
        log.info("");
        log.info("    Execution time: " + formatter.format((endTime - startTime) / 1000d) + " seconds");

        DateFormat dateFormat = new SimpleDateFormat("E, d MMM HH:mm:ss '('zZ')' y"); // semana, dia mês hora fuzo ano
        Date date = new Date();
        log.info("    Finished at   : " + dateFormat.format(date));
        log.info(CommonUtils.fixedLengthString('=', +80));
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
                int nrFicheiros = 1;
                for (int m = i + 1; m < finalList.size(); m++) {
                    if (auxLogs[i][0].equals(auxLogs[m][0]) && auxLogs[i][1].equals(auxLogs[m][1])) {
                        nrFicheiros++;
                        String[] auxI = auxLogs[i][2].split(", ");
                        String[] auxM = auxLogs[m][2].split(", ");

                        ArrayList<String> auxFinal = new ArrayList<String>();
                        for (int n = 0; n < auxI.length; n++) {
                            auxFinal.add(auxI[n]);
                        }

                        for (int n = 0; n < auxM.length; n++) {
                            int flag = 0;
                            for (int p = 0; p < auxFinal.size(); p++) {
                                if (auxFinal.get(p).equals(auxM[n])) {
                                    flag = 1;
                                }
                            }
                            if (flag == 0) {
                                auxFinal.add(auxM[n]);
                            }
                        }

                        auxLogs[i][2] = "";
                        for (String s : auxFinal) {
                            auxLogs[i][2] += s + ", ";
                        }

                        auxLogs[i][2] = auxLogs[i][2].substring(0, auxLogs[i][2].length() - 2);

                        if (Integer.parseInt(auxLogs[m][4]) > Integer.parseInt(auxLogs[i][4])) {
                            auxLogs[i][4] = auxLogs[m][4];
                        }

                        auxLogsContagem[m] = 0;

                    }
                }

                ef.escreverCSV(auxLogs[i][0], auxLogs[i][1], nrFicheiros, auxLogs[i][2], Integer.parseInt(auxLogs[i][4]));
            }
        }

        return Collections.unmodifiableList(fileMatches);
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

        /**
         * Possible values for field NERModel. Read the cases type bellow...
         * NEREntropy - Uses only NER MaxEntropy OpenNLP trained models
         * NERDictionary - Uses only Dictionary XML OPenNLP implementation
         * NERRegex - Uses only Regex OpenNLP models NERPattern - Uses a free
         * query (costumize) in text file
         */
        final String[] NERModel = fileDiscoveryProperties.getProperty("NERmodel").split(",");

        dictionaryPathList = dictionaryPath.split(",");

        // Let's iterate over directories
        Metadata metadata;

        // ArrayList<String> DictionariesFound = null;
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
                // read content file...
                // TODO: Tentar melhorar para forçar a leitura a utf-8!!!!
                final InputStream stream = new FileInputStream(ficheiro);
                //final InputStreamReader stream = new InputStreamReader(new FileInputStream(ficheiro), "UTF-8");

                if (stream != null) {
                    parser.parse(stream, handler, metadata);
                    handlerString = handler.toString();
                }

            } catch (IOException e) {
                log.info("");
                log.info(CommonUtils.fixedLengthString('=', +70));
                log.info(" -> Unable to read: " + ficheiro + ". Ignoring...");
                log.info(CommonUtils.fixedLengthString('-', +70));
                
            } catch (Throwable npe) {
                log.info("");
                log.info(CommonUtils.fixedLengthString('=', +70));
                log.info(" -> File error or not supported: " + ficheiro + ". Ignoring...");
                log.info(CommonUtils.fixedLengthString('-', +70));
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
                log.info(CommonUtils.fixedLengthString('=', +70));
                log.info(" -> Ficheiro a Analisar: " + caminho + nomeFicheiro);
                log.info(CommonUtils.fixedLengthString('-', +70));

                String[] repositorioList = null;
                final String repositorio = fileDiscoveryProperties.getProperty("directories");
                repositorioList = repositorio.split(",");

                final FileMatchMetaData result = new FileMatchMetaData(caminho, nomeFicheiro, repositorio);

                ArrayList<ResultadoSuspeito> listaResultadoSuspeito = new ArrayList<ResultadoSuspeito>();

                InputStream modelInToken = new FileInputStream(fileDiscoveryProperties.getProperty("tokens"));
                TokenizerModel modelToken = new TokenizerModel(modelInToken);
                Tokenizer tokenizer = new TokenizerME(modelToken);

                log.debug("    - Content: \n" + handlerString + "\n"); // permitir o ver o conteúdo do documento
                log.debug("    - Content size: " + handlerString.length());

                for (String modelo : NERModel) {

                    switch (modelo) {

                        case "NEREntropy": // Start NEREntropy OpenNLP implementation
                            
                            log.info("");
                            log.info(" >> Maximun Entropy tokenizer process starting ...");

                            final String models = fileDiscoveryProperties.getProperty("models");
                            modelList = models.split(",");
                            
                            log.debug("");
                            log.debug("    - MaxEnt Model list: " + Arrays.toString(modelList));

                            for (String model : modelList) {
                                log.debug("");
                                log.debug("    - Processing model: " + model);
                                final Model modelEntropy = createModel(fileDiscoveryProperties, model);
                                final String tokens[] = modelEntropy.getTokenizer().tokenize(handler.toString());
                                final Span nameSpans[] = modelEntropy.getNameFinder().find(tokens);
                                final double[] spanProbs = modelEntropy.getNameFinder().probs(nameSpans);

                                //display names
                                probabilityList = new ArrayList<>();

                                for (int i = 0; i < nameSpans.length; i++) {

                                    final String MaxEntResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s",
                                            nameSpans[i].getStart(),
                                            nameSpans[i].getEnd() - 1,
                                            model,
                                            //decimalFormat.format(spanProbs[i]),
                                            tokens[nameSpans[i].getStart()]);
                                    log.info(MaxEntResults);

                                    probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], round(spanProbs[i], 2)));

                                    listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokens[nameSpans[i].getStart()], round(spanProbs[i], 2), modelo, modelEntropy.getName(), nameSpans[i].getStart(), nameSpans[i].getEnd() - 1));

                                    modelEntropy.getNameFinder().clearAdaptiveData();
                                    averageProbability = calculateAverage(probabilityList);
                                }

                            }

                            break; // End of NEREntropy

                        case "NERDictionary": // Start Dictionary XML

                            log.info("");
                            log.info(" >> Loading Dictionaries...");

                            findersDict = new ArrayList<>();
                            File nodeDict;
                            // add all dictionaries
                            for (final String dictPath : dictionaryPathList) {
                                nodeDict = new File(dictPath);
                                final InputStream Dictstream = new FileInputStream(dictPath);

                                Dictionary rawdict = new Dictionary(Dictstream);
                                findersDict.add(new DictionaryNameFinder(rawdict, nodeDict.getName().replaceAll("\\.\\w+", "")));
                                log.debug("    - Add Dictionaries: " + nodeDict.getName().replaceAll("\\.\\w+", ""));
                            }

                            probabilityListDict = new ArrayList<>();

                            final String tokensDict[] = tokenizer.tokenize(handler.toString());

                            // Applying Dictionary model
                            for (TokenNameFinder dictionaryNER : findersDict) {

                                final Span DictSpansOnly[] = dictionaryNER.find(tokensDict);

                                for (int i = 0; i < DictSpansOnly.length; i++) {

                                    final String DictionaryResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s",
                                            DictSpansOnly[i].getStart(),
                                            DictSpansOnly[i].getEnd() - 1,
                                            DictSpansOnly[i].getType(),
                                            tokensDict[DictSpansOnly[i].getStart()]);
                                    log.info(DictionaryResults);

                                    // Para dicionário está a ser assumido uma probabilidade de 0,85.
                                    // Embora reconheça o nome, o nome requer contextualização... logo assumo 85% de certeza
                                    // Tentar pensar em alterar a probabilidade para uma forma dinâmica!!!!
                                    probabilityListDict.add(new Probability(tokensDict[DictSpansOnly[i].getStart()], 0.85));

                                    listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokensDict[DictSpansOnly[i].getStart()], 0.85, modelo, DictSpansOnly[i].getType(), DictSpansOnly[i].getStart(), DictSpansOnly[i].getEnd() - 1));

                                }

                                dictionaryNER.clearAdaptiveData();
                                averageProbability = calculateAverage(probabilityListDict);

                            }

                            break; // End of NERDictionary

                        case "NERRegex": // Aplicação do modelo REGEX
                            
                            log.info("");
                            log.info(" >> Applying REGEX models...");

                            final Properties RegexProperties = loadProperties("regex.properties");
                            probabilityListRegex = new ArrayList<>();

                            final String tokensRegex[] = tokenizer.tokenize(handler.toString());

                            Enumeration<?> enumeration = RegexProperties.propertyNames();
                            Map<String, Pattern[]> regexMap = new HashMap<>();

                            while (enumeration.hasMoreElements()) {
                                String key = (String) enumeration.nextElement();
                                String value = RegexProperties.getProperty(key);
                                Pattern ptregex = Pattern.compile(value);
                                Pattern[] ptterns = new Pattern[]{ptregex};

                                regexMap.put(key, ptterns);
                            }

                            RegexNameFinder finder = new RegexNameFinder(regexMap);
                            Span[] resultRegex = finder.find(tokensRegex);

                            // Avaliação dos resultados do modelo REGEX
                            log.debug("");
                            log.debug("    - Detecting Regular Expression...");

                            String getRegexType = "N/A";
                            for (int i = 0; i < resultRegex.length; i++) {
                                getRegexType = resultRegex[i].getType();

                                //Validar o NIF
                                if (!Validador.NIF(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NIF")) {
                                    continue;
                                }

                                //Validar o NISS
                                if (!Validador.NISS(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NISS")) {
                                    continue;
                                }

                                final String RegexResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s",
                                        resultRegex[i].getStart(),
                                        resultRegex[i].getEnd() - 1,
                                        getRegexType,
                                        tokensRegex[resultRegex[i].getStart()]);
                                log.info(RegexResults);

                                // default regex probability is 99% always
                                probabilityListRegex.add(new Probability(tokensRegex[resultRegex[i].getStart()], 0.99));

                                listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiro, tokensRegex[resultRegex[i].getStart()], 0.99, modelo, getRegexType, resultRegex[i].getStart(), resultRegex[i].getEnd() - 1));

                            }

                            finder.clearAdaptiveData();
                            averageProbability = calculateAverage(probabilityListRegex);

                            break; // End of NERRegex

                        case "NERPattern": // Start NERPattern... a free query in text file
                            
                            log.info("");
                            log.info(" >> Loading Patterns...");

                            String[] patternList = fileDiscoveryProperties.getProperty("pattern").split(",");
                            String ficheiroAnalisar = caminho + nomeFicheiro;
                            log.debug("    - Verificação do ficheiro a analisar: " + ficheiroAnalisar);

                            for (String filePattern : patternList) {
                                File pattern = new File(filePattern);
                                String[] auxNomeAtributo = filePattern.split("/");

                                log.debug("    - Evaluating Pattern: " + auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""));
                                Scanner scanPattern = new Scanner(pattern);

                                ArrayList<String> consulta = new ArrayList<String>();

                                while (scanPattern.hasNextLine()) {
                                    //log.info("Confirmação de entrada no while");
                                    String auxPattern = scanPattern.nextLine();
                                    consulta.add(auxPattern);
                                }

                                //log.debug("    - Dados a consultar: " + consulta);
                                for (String s : consulta) {
                                    Pattern patternCompile = Pattern.compile(s, Pattern.CASE_INSENSITIVE);  // case-insensitive matching
                                    Matcher matcher = patternCompile.matcher(handlerString);
                                    //log.debug("    - Dados a consultar: " + s);

                                    while (matcher.find()) { // find the next match

                                        final String PatternResults = String.format("         %1$5s..%2$-9s %3$-15s %4$s",
                                                matcher.start(),
                                                matcher.end() - 1,
                                                auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""),
                                                matcher.group());
                                        log.info(PatternResults);

                                        listaResultadoSuspeito.add(new ResultadoSuspeito(ficheiroAnalisar, matcher.group(), 0.98, modelo, auxNomeAtributo[auxNomeAtributo.length - 1].replaceAll("\\.\\w+", ""), matcher.start(), matcher.end() - 1));

                                    }

                                }

                            }

                            break; // End of NERPattern
                        // End of Switch NERModel: Dictionary, MaxEntropy, Regex and Pattern
                        }

                }

                //Editar Lista de Resultados Suspeitos para juntar tokens consecutivos
                int k = 0;
                for (int i = 0; i < listaResultadoSuspeito.size() - 1; i++) {
                    if (listaResultadoSuspeito.get(i).getModelo().equals("NERDictionary") || listaResultadoSuspeito.get(i).getModelo().equals("NEREntropy")) {
                        k = 1;
                        while (listaResultadoSuspeito.size() > (i + 1) && listaResultadoSuspeito.get(i + 1).getTipoEntidade().equals(listaResultadoSuspeito.get(i).getTipoEntidade())
                                && listaResultadoSuspeito.get(i + 1).getModelo().equals(listaResultadoSuspeito.get(i).getModelo())
                                && listaResultadoSuspeito.get(i + 1).getPosFinal() == listaResultadoSuspeito.get(i).getPosFinal() + k) {
                            listaResultadoSuspeito.get(i).setEntidade(listaResultadoSuspeito.get(i).getEntidade() + " " + listaResultadoSuspeito.get(i + 1).getEntidade());
                            listaResultadoSuspeito.remove(i + 1);
                            k++;
                        }
                    }

                }

                //Editar Lista de Resultados Suspeitos para remover os resultados repetidos
                for (int i = 0; i < listaResultadoSuspeito.size(); i++) {
                    for (int j = i + 1; j < listaResultadoSuspeito.size(); j++) {
                        if (listaResultadoSuspeito.get(i).getEntidade().equalsIgnoreCase(listaResultadoSuspeito.get(j).getEntidade())) {
                            if ((listaResultadoSuspeito.get(j).getProbabilidade() > listaResultadoSuspeito.get(i).getProbabilidade())
                                    || (listaResultadoSuspeito.get(i).getTipoEntidade().equals("Telefone") && listaResultadoSuspeito.get(j).getTipoEntidade().equals("NIF"))) {
                                listaResultadoSuspeito.get(i).setClassificacaoRisco(listaResultadoSuspeito.get(j).getClassificacaoRisco());
                                listaResultadoSuspeito.get(i).setModelo(listaResultadoSuspeito.get(j).getModelo());
                                listaResultadoSuspeito.get(i).setPosFinal(listaResultadoSuspeito.get(j).getPosFinal());
                                listaResultadoSuspeito.get(i).setPosInicial(listaResultadoSuspeito.get(j).getPosInicial());
                                listaResultadoSuspeito.get(i).setProbabilidade(listaResultadoSuspeito.get(j).getProbabilidade());
                                listaResultadoSuspeito.get(i).setTipoEntidade(listaResultadoSuspeito.get(j).getTipoEntidade());
                            }
                            listaResultadoSuspeito.remove(j);
                            j = j - 1;
                        }
                    }
                }

                //Refinar os nomes repetidos para nomes mais completos
                for (int i = 0; i < listaResultadoSuspeito.size(); i++) {
                    if (listaResultadoSuspeito.get(i).getModelo().equals("NEREntropy") || listaResultadoSuspeito.get(i).getModelo().equals("NERDictionary")) {
                        for (int j = i + 1; j < listaResultadoSuspeito.size(); j++) {
                            if (listaResultadoSuspeito.get(j).getModelo().equals("NEREntropy") || listaResultadoSuspeito.get(j).getModelo().equals("NERDictionary")) {
                                if (listaResultadoSuspeito.get(i).getPosInicial() == listaResultadoSuspeito.get(j).getPosInicial()) {
                                    if (listaResultadoSuspeito.get(i).getEntidade().length() < listaResultadoSuspeito.get(j).getEntidade().length()) {
                                        listaResultadoSuspeito.get(i).setEntidade(listaResultadoSuspeito.get(j).getEntidade());
                                        listaResultadoSuspeito.get(i).setClassificacaoRisco(listaResultadoSuspeito.get(j).getClassificacaoRisco());
                                        listaResultadoSuspeito.get(i).setModelo(listaResultadoSuspeito.get(j).getModelo());
                                        listaResultadoSuspeito.get(i).setPosFinal(listaResultadoSuspeito.get(j).getPosFinal());
                                        listaResultadoSuspeito.get(i).setPosInicial(listaResultadoSuspeito.get(j).getPosInicial());
                                        listaResultadoSuspeito.get(i).setProbabilidade(listaResultadoSuspeito.get(j).getProbabilidade());
                                        listaResultadoSuspeito.get(i).setTipoEntidade(listaResultadoSuspeito.get(j).getTipoEntidade());
                                    }
                                    listaResultadoSuspeito.remove(j);
                                    j = j - 1;
                                }

                            }
                        }
                    }
                }

                // Compare results and print the best result
                log.info("");
                log.info(CommonUtils.fixedLengthString('-', +70));
                log.info(" -> Resultados suspeitos do ficheiro: " + caminho + nomeFicheiro);
                log.info("    (ler 7 colunas: Tipo da Entidade, Risco, Probabilidade, Modelo, Pos. Inicial, Pos. Final, Entidade)\n");

                ArrayList<String> tiposEncontrados = new ArrayList<String>();
                int auxNrResultados = 0;
                for (ResultadoSuspeito rs : listaResultadoSuspeito) {

                    log.info(rs.toString()); // Check ResultadoSuspeito Class
                    if (!tiposEncontrados.contains(rs.getTipoEntidade())) {
                        tiposEncontrados.add(rs.getTipoEntidade());
                    }
                    auxNrResultados++;
                }
                log.info("");
                log.info("    Encontrados " + auxNrResultados + " tokens de " + handlerString.length() + " verificados");

                // Grau de Risco de acordo com o tipo de entidades descobertas
                int auxMaxRisco = 0;
                for (int i = 0; i < tiposEncontrados.size(); i++) {
                    if (DataDefender.getClassificacaoRisco(tiposEncontrados.get(i)) > auxMaxRisco) {
                        auxMaxRisco = DataDefender.getClassificacaoRisco(tiposEncontrados.get(i));
                    }
                }

                if (tiposEncontrados.size() > 1) {
                    auxMaxRisco = Math.min(auxMaxRisco + 1, 5);
                }

                result.setDictionariesFound(tiposEncontrados);
                result.setGrauRisco(auxMaxRisco);
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

    private String getDocProcessType(String directory) {
        return "batata"; //To change body of generated methods, choose Tools | Templates.
        // TODO: Falta ligar à base de dados...
    }

}
