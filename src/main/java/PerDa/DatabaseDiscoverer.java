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

import PerDa.database.DBConnection;
import static java.lang.Double.parseDouble;
import static java.util.regex.Pattern.compile;
import static org.apache.log4j.Logger.getLogger;
import org.apache.commons.collections.ListUtils;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.RegexNameFinder;
import opennlp.tools.namefind.TokenNameFinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import opennlp.tools.util.Span;

import org.apache.log4j.Logger;

import PerDa.database.IDBFactory;
import PerDa.database.metadata.IMetaData;
import PerDa.database.metadata.MatchMetaData;
import PerDa.database.sqlbuilder.ISQLBuilder;
import PerDa.functions.Utils;
import PerDa.report.ReportUtil;
import PerDa.specialcase.SpecialCase;
import PerDa.utils.CommonUtils;
import PerDa.utils.Score;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Enumeration;
import static PerDa.utils.AppProperties.loadProperties;
import java.awt.Desktop;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.commons.cli.CommandLine;

/**
 * @author Armenak Grigoryan
 * @author Redglue
 * @author ruimfsantos
 */
public class DatabaseDiscoverer extends Discoverer {

    private static final Logger log = getLogger(DatabaseDiscoverer.class);

    private static String[] modelList;

    @SuppressWarnings("unchecked")
    public List<MatchMetaData> discover(final IDBFactory factory, final Properties dataDiscoveryProperties, final Set<String> tables)
            throws AnonymizerException, ParseException, IOException, DataDefenderException {

        final long startTime = System.currentTimeMillis();

        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(" -> Struture Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        final String calculate_score = dataDiscoveryProperties.getProperty("score_calculation");
        final String NERModelsList = dataDiscoveryProperties.getProperty("NERmodel");

        ArrayList<String> listSampleData = new ArrayList<String>();

        log.info("    - Probability threshold [" + probabilityThreshold + "]");

        // Get list of models used in data discovery
        final String models = dataDiscoveryProperties.getProperty("models");
        modelList = models.split(",");
        log.info("    - Model list [" + Arrays.toString(modelList) + "]");

        String[] NERmodels = NERModelsList.split(",");

        List<MatchMetaData> finalList = new ArrayList<>();
        List<DatabaseResults> results = new ArrayList<>();

        for (String NERModel : NERmodels) {
            log.info(CommonUtils.fixedLengthString('-', +70));
            log.info(" -> Processing NER Model Type: " + NERModel);
            log.info("");
            if (NERModel.equals("NEREntropy")) {
                for (String model : modelList) {
                    log.info("    - Analysing model: " + model);
                    log.info("");
                    final Model modelEntropy = createModel(dataDiscoveryProperties, model);
                    matches = discoverAgainstSingleModelNEREntropy(factory, dataDiscoveryProperties, tables, modelEntropy, probabilityThreshold, NERModel);

                }

            } else {
                matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, probabilityThreshold, NERModel);
            }

            finalList = ListUtils.union(finalList, matches);
        }

        EscreverFicheiroResultado efResultado = new EscreverFicheiroResultado();
        efResultado.cabecalhoCSVResultadoDB();

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        log.info("");
        log.info(CommonUtils.fixedLengthString('-', +70));
        log.info(" -> List of suspects:");
        log.info("    (Ler 6 colunas: Tabela, Coluna, Probabilidade, Modelo NER, [Tipo de Dados], Risco, Nr Linhas)");
        log.info("");

        final Score score = new Score();
        int highRiskColumns = 0;
        int rowCount = 0;
        for (final MatchMetaData data : finalList) {
            // empty arraylist 
            listSampleData.clear();

            // Row count
            if (calculate_score.equals("yes")) {
                log.debug("Skipping table rowcount...");
                rowCount = ReportUtil.rowCount(factory, data.getTableName());
            }

            // Getting 5 sample values
            // final List<String> sampleDataList = ReportUtil.sampleData(factory, data.getTableName(), data.getColumnName());
            // Output
            log.debug("Table.Column                : " + data.toString());
            log.debug(CommonUtils.fixedLengthString('=', data.toString().length() + 30));

            // Prob
            log.debug("Probability                 : " + decimalFormat.format(data.getAverageProbability()));

            // Model
            log.debug("Model                       : " + data.getModel());

            // Dictionaries
            log.debug("Data Type                   : " + data.getDictionariesFound());

            if (calculate_score.equals("yes")) {

                // row count
                log.debug("Number of rows              : " + rowCount);

                // Score
                log.debug("Score                       : " + score.columnScore(rowCount));
            } else {

                log.debug("Number of rows              : N/A");
                log.debug("Score                       : N/A");
            }

            log.debug("Sample data:");
            log.debug(CommonUtils.fixedLengthString('-', 11));

            final List<Probability> probabilityList = data.getProbabilityList();
            Collections.sort(probabilityList, Comparator.comparingDouble(Probability::getProbabilityValue).reversed());

            // Score calculation is evaluated with calculate_score parameter
            if (calculate_score.equals("yes")) {
                if (score.columnScore(rowCount).equals("High")) {
                    highRiskColumns++;
                }
            }

            final String Results = String.format("    - %1$-40s %2$-30s %3$-9s %4$-17s %5$-13s %6$-7s %7$s",
                    data.getTableName(),
                    data.getColumnName(),
                    decimalFormat.format(data.getAverageProbability()),
                    data.getModel(),
                    data.getDictionariesFound(),
                    DataDefender.getClassificacaoRisco(data.getDictionariesFound()),
                    rowCount);
            log.info(Results);

            for(Probability p: probabilityList){
                efResultado.escreverCSVResultado(data.getTableName()+"."+data.getColumnName(), data.getDictionariesFound(), DataDefender.getClassificacaoRisco(data.getDictionariesFound()), data.getAverageProbability(), data.getModel(), rowCount, p.getSentence());
            }
            boolean encontreiTabela = false, encontreiCampo = false;

            for (DatabaseResults dbr : results) {
                if (dbr.equals(new DatabaseResults(data.getTableName(), data.getColumnName()))) {
                    encontreiTabela = true;
                    encontreiCampo = false;
                    for (String s : dbr.getCampos()) {
                        if (s.equals(data.getDictionariesFound())) {
                            encontreiCampo = true;
                        }
                    }
                    if (!encontreiCampo) {
                        ArrayList<String> auxCampos = dbr.getCampos();
                        auxCampos.add(data.getDictionariesFound());
                        dbr.setCampos(auxCampos);
                    }
                }
            }

            if (!encontreiTabela) {
                ArrayList<String> auxCampos = new ArrayList<String>();
                auxCampos.add(data.getDictionariesFound());
                results.add(new DatabaseResults(data.getTableName(), data.getColumnName(), data.getAverageProbability(), auxCampos, data.getColumnType()));
            }

            // Restringir a impressão apenas a 5 entidades com probabilidade mais elevada.
            int y = 0;
            if (data.getProbabilityList().size() >= 5) {
                y = 3;
            } else {
                y = data.getProbabilityList().size();
            }

            for (int i = 0; i < y; i++) {
                final Probability p = data.getProbabilityList().get(i);
                log.info("         " + p.getSentence() + ": " + decimalFormat.format(p.getProbabilityValue()));
                listSampleData.add(p.getSentence());
            }

        }

        EscreverFicheiroEAPY ef = new EscreverFicheiroEAPY();
        EscreverFicheiroGovernacao efGovernacao = new EscreverFicheiroGovernacao();
        EscreverFicheiroResumo efResumo = new EscreverFicheiroResumo();

        ef.cabecalhoCSVDatabase();
        efGovernacao.cabecalhoCSVGovernacaoDB();
        efResumo.cabecalhoCSVResumoDB();

        log.info("");
        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(" -> Quadro resumo dos resultados suspeitos:");
        log.info("    (ler 4 colunas: Tabela, Coluna, Probabilidade, [ Tipo de dados ])\n");
        
        
        List<String> auxTabelas = new ArrayList();
        List<String> auxTiposEncontrados = new ArrayList();
        
        for (int dbrAux = 0; dbrAux < results.size(); dbrAux++) {
            final String Resumo = String.format("    - %1$-40s %2$-30s %3$-10s %4$-15s %5$-7s %6$-10s %7$s",
                    results.get(dbrAux).getTabela(),
                    results.get(dbrAux).getColuna(),
                    decimalFormat.format(results.get(dbrAux).getProbabilidade()),
                    results.get(dbrAux).getTipoColuna(),
                    DataDefender.getClassificacaoRisco(results.get(dbrAux).toString()),
                    //results.get(dbrAux).getGrauRisco(),
                    ReportUtil.rowCount(factory, results.get(dbrAux).getTabela()),
                    results.get(dbrAux).toString());
            log.info(Resumo);

            efGovernacao.escreverCSVGovernacao(results.get(dbrAux).getColuna(), results.get(dbrAux).getTipoColuna(), DataDefender.getClassificacaoRisco(results.get(dbrAux).toString()), factory.getUrl(), results.get(dbrAux).getTabela(), results.get(dbrAux).toString());
            
            
            int auxTabelasId = 0;
            boolean encontreiTabela = false;
            for(String s:auxTabelas){
                if(s.equals(results.get(dbrAux).getTabela())){
                    String[] auxTipoTabela = auxTiposEncontrados.get(auxTabelasId).split(",");
                    int i;
                    for(i=0; i<auxTipoTabela.length; i++){
                        if(auxTipoTabela[i].equals(results.get(dbrAux).toString()))
                            break;
                    }
                    if(i==auxTipoTabela.length){
                        auxTiposEncontrados.add(auxTiposEncontrados.get(auxTabelasId) + "," + results.get(dbrAux).toString());
                        auxTiposEncontrados.remove(auxTabelasId);
                    }
                   encontreiTabela = true; 
                }
                auxTabelasId++;
            }
            
            if(!encontreiTabela){
                auxTabelas.add(results.get(dbrAux).getTabela());
                auxTiposEncontrados.add(results.get(dbrAux).toString());
            }
            
            
        }
        final int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
        for(int j= 0; j<auxTabelas.size(); j++){
            int risco=0;
            String[] auxTipoTabela = auxTiposEncontrados.get(j).split(",");
            
            for(int i=0; i<auxTipoTabela.length; i++){
                if(DataDefender.getClassificacaoRisco(auxTipoTabela[i])>risco)
                    risco = DataDefender.getClassificacaoRisco(auxTipoTabela[i]);
            }
            if(auxTipoTabela.length>1)
                risco = Math.min(risco + 1, 5);
            efResumo.escreverCSVResumoBD(factory.getUrl(), auxTabelas.get(j), auxTiposEncontrados.get(j), risco, Math.min(limit,ReportUtil.rowCount(factory, auxTabelas.get(j))), ReportUtil.rowCount(factory, auxTabelas.get(j)),  ReportUtil.columnCount(factory, auxTabelas.get(j)));
        }
        for (int dbrAux = 0; dbrAux < results.size(); dbrAux++) {
            int nrColunas = 1;
            for (int m = dbrAux + 1; m < results.size(); m++) {
                if (results.get(m).getTabela().equals(results.get(dbrAux).getTabela())) {
                    nrColunas++;

                    for (String s : results.get(m).getCampos()) {
                        int flag = 0;
                        for (int p = 0; p < results.get(dbrAux).getCampos().size(); p++) {
                            if (results.get(dbrAux).getCampos().get(p).equals(s)) {
                                flag = 1;
                            }
                        }

                        ArrayList<String> auxFinalCampos = results.get(dbrAux).getCampos();
                        auxFinalCampos.add(s);
                        if (flag == 0) {
                            results.get(dbrAux).setCampos(auxFinalCampos);
                        }
                    }

                    results.remove(m);
                    m = m - 1;
                }
            }

            int auxMaxRisco = 0;
            for (int i = 0; i < results.get(dbrAux).getCampos().size(); i++) {
                if (DataDefender.getClassificacaoRisco(results.get(dbrAux).getCampos().get(i)) > auxMaxRisco) {
                    auxMaxRisco = DataDefender.getClassificacaoRisco(results.get(dbrAux).getCampos().get(i));
                }
            }

            if (results.get(dbrAux).getCampos().size() > 1) {
                auxMaxRisco = Math.min(auxMaxRisco + 1, 5);
            }

            results.get(dbrAux).setGrauRisco(auxMaxRisco);

            ef.escreverCSV(factory.getUrl(), results.get(dbrAux).getTabela(), nrColunas, results.get(dbrAux).camposToString(), DataDefender.getClassificacaoRisco(results.get(dbrAux).toString()));
            
       

        }
        
        // Apresentar os resultados de uma forma gráfica em WEB
        File htmlFile = new File("PerDaInterface\\public_html\\index.html");
        Desktop.getDesktop().browse(htmlFile.toURI());

        // Only applicable when parameter table_rowcount=yes otherwise score calculation should not be done
        if (calculate_score.equals("yes")) {
            log.info("");
            log.info("    - Overall score: " + score.dataStoreScore());
            log.info("");

            if (finalList != null && finalList.size() > 0) {
                log.info(CommonUtils.fixedLengthString('=', +80));
                final int threshold_count = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_count"));

                if (finalList.size() > threshold_count) {
                    log.info("    Number of PI [" + finalList.size() + "] columns is higher than defined threashold [" + threshold_count + "]");
                } else {
                    log.info("    Number of PI [" + finalList.size() + "] columns is lower or equal than defined threashold [" + threshold_count + "]");
                }
                final int threshold_highrisk = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_highrisk"));
                if (highRiskColumns > threshold_highrisk) {
                    log.info("    Number of High risk PI [" + highRiskColumns + "] columns is higher than defined threashold [" + threshold_highrisk + "]\n");
                } else {
                    log.info("    Number of High risk PI [" + highRiskColumns + "] columns is lower or equal than defined threashold [" + threshold_highrisk + "]\n");
                }
            }

        } else {
            log.info("Overall score: N/A");
        }
        

        final long endTime = System.currentTimeMillis();

        final NumberFormat formatter = new DecimalFormat("#0.00");
        log.info(CommonUtils.fixedLengthString('-', +70));
        log.info(" -> Struture Data Scan completed");
        log.info("");
        log.info("    Execution time: " + formatter.format((endTime - startTime) / 1000d) + " seconds");

        DateFormat dateFormat = new SimpleDateFormat("E, d MMM HH:mm:ss '('zZ')' y"); // semana, dia mês hora fuzo ano
        Date date = new Date();
        log.info("    Finished at   : " + dateFormat.format(date));
        log.info(CommonUtils.fixedLengthString('=', +80));

        return matches;

    }

    private List<TokenNameFinder> getDictionariesFileForSearch(String[] dictionaryPathList, File nodeDict)
            throws IOException {

        List<TokenNameFinder> findersDict = new ArrayList<>();

        // Add all dictionaries
        for (final String dictPath : dictionaryPathList) {
            nodeDict = new File(dictPath);
            final InputStream Dictstream = new FileInputStream(dictPath);

            Dictionary rawdict = new Dictionary(Dictstream);
            findersDict.add(new DictionaryNameFinder(rawdict, nodeDict.getName().replaceAll("\\.\\w+", "")));
            String entidadeDict = nodeDict.getName().replaceAll("\\.\\w+", "");
            log.info("    - Dictionary considered: " + entidadeDict);
        }
        log.info("");
        return findersDict;
    }

    private List<MatchMetaData> discoverAgainstSingleModel(final IDBFactory factory, final Properties dataDiscoveryProperties,
            final Set<String> tables, final double probabilityThreshold, final String NERModel)
            throws AnonymizerException, ParseException, IOException, DataDefenderException {

        final IMetaData metaData = factory.fetchMetaData();
        final List<MatchMetaData> map = metaData.getMetaData();

        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();
        MatchMetaData specialCaseData = null;
        boolean specialCase = false;

        final String[] specialCaseFunctions = dataDiscoveryProperties.getProperty("extentions").split(",");

        //NERDictionary
        String dictionaryPath = null;
        File nodeDict = null;
        String[] dictionaryPathList = null;
        List<TokenNameFinder> findersDict = null;
        ArrayList<String> DictionariesFound = null;

        InputStream modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("tokens"));
        TokenizerModel modelToken = new TokenizerModel(modelInToken);
        Tokenizer tokenizer = new TokenizerME(modelToken);

        if (NERModel.equals("NERDictionary")) {
            dictionaryPath = dataDiscoveryProperties.getProperty("dictionary_path");
            dictionaryPathList = dictionaryPath.split(",");
            findersDict = getDictionariesFileForSearch(dictionaryPathList, nodeDict);
        }

        // possible values for this:
        // NEREntropy - Uses only NER MaxEntropy OpenNLP trained models
        // NERDictionary - Uses only Dictionary XML OPenNLP implementation
        // NERRegex - Uses only Regex OpenNLP models
        // NEREntropyDictionary - Uses MaxEntropy and compares to Dictionary - Returns only the ones present in the dictionary
        //final InputStream DictstreamDB = new FileInputStream(dictionaryPath);
        //Dictionary rawdictDB = new Dictionary(DictstreamDB);
        //DictionaryNameFinder dictionaryNERDB = new DictionaryNameFinder(rawdictDB, "NERDB");
        if (specialCaseFunctions != null && specialCaseFunctions.length > 0) {
            specialCase = true;
        }

        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        List<Probability> probabilityListRegex;
        List<Probability> probabilityListDict;
        List<Probability> probabilityListPattern;
        double averageProbability;

        // Initization
        String getRegexType = "N/A";

        for (final MatchMetaData data : map) {
            final String tableName = data.getTableName();
            final String columnName = data.getColumnName();

            // start with 0 as new column is analyzed
            probabilityListRegex = new ArrayList<>();
            probabilityListDict = new ArrayList<>();
            probabilityListPattern = new ArrayList<>();
            averageProbability = 0;
            log.debug("    - Analyzing...  [" + tableName + "] . [" + columnName + "] . [" + data.getColumnType() + "]");

            if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase(Locale.ENGLISH))) {
                log.debug("    - Continue ...");
                continue;
            }

            final String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");
            if (!CommonUtils.isEmptyString(tableNamePattern)) {
                final Pattern p = compile(tableNamePattern);
                if (!p.matcher(tableName).matches()) {
                    continue;
                }
            }

            final String table = sqlBuilder.prefixSchema(tableName);
            final int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
            final String query = sqlBuilder.buildSelectWithLimit(
                    "SELECT " + columnName
                    + " FROM " + table
                    + " WHERE " + columnName + " IS NOT NULL ORDER BY RAND()", limit);  // Procura aleatória 
            log.debug("Executing query against database: " + query);

            try (Statement stmt = factory.getConnection().createStatement();
                    ResultSet resultSet = stmt.executeQuery(query);) {

                while (resultSet.next()) {
                    if (data.getColumnType().equals("BLOB") || data.getColumnType().equals("GEOMETRY")) {
                        continue;
                    }

//                    if (model.getName().equals("location") &&
//                        data.getColumnType().contains("INT")) {
//                        continue;
//                    }
                    final String sentence = resultSet.getString(1);
//                    if (specialCase) {
//                        try {
//                            for (int i=0; i<specialCaseFunctions.length; i++) {
//                                if (sentence != null && !sentence.equals("")) {
//                                    specialCaseData = (MatchMetaData)callExtention(specialCaseFunctions[i], data, sentence);
//                                }
//                            }
//                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
//                            log.error(e.toString());
//                        }
//                    }

                    if (sentence != null && !sentence.isEmpty()) {
                        String processingValue = "";
                        if (data.getColumnType().equals("DATE")
                                || data.getColumnType().equals("TIMESTAMP")
                                || data.getColumnType().equals("DATETIME")) {

                            final DateFormat originalFormat = new SimpleDateFormat(sentence, Locale.ENGLISH);
                            final DateFormat targetFormat = new SimpleDateFormat("MMM d, yy", Locale.ENGLISH);
                            final java.util.Date date = originalFormat.parse(sentence);
                            processingValue = targetFormat.format(date);
                        } else {
                            processingValue = sentence;
                        }

                        switch (NERModel) {
                            case "NERRegex": // START LOOKUP WITH REGULAR EXPRESSIONS
                                final Properties RegexProperties = loadProperties("regex.properties");

                                log.debug("Tokenizing for column is starting ...");
                                log.debug("Content: " + processingValue);

                                final String tokensRegex[] = tokenizer.tokenize(processingValue);
                                final List<String> suspList = new ArrayList(RegexProperties.keySet());

                                log.debug("Applying REGEX model for sensitive data ...");

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
                                log.debug("Evaluating Regex results ...");
                                final String RegexSpam = Arrays.toString(Span.spansToStrings(resultRegex, tokensRegex));

                                //log.info("Found Regex: " + RegexSpam);
                                //String getRegexType = "N/A";
                                for (int i = 0; i < resultRegex.length; i++) {
                                    getRegexType = resultRegex[i].getType();
                                    log.debug("Found Type text       : " + getRegexType);
                                    log.debug("Found identifier text : " + tokensRegex[resultRegex[i].getStart()]);

                                    //Validar o NIF
                                    if (!Validador.NIF(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NIF")) {
                                        continue;
                                    }

                                    //Validar o NISS
                                    if (!Validador.NISS(tokensRegex[resultRegex[i].getStart()]) && getRegexType.equals("NISS")) {
                                        continue;
                                    }

                                    //Com Expressões Regulares a probabilidade é sempre de 99%
                                    probabilityListRegex.add(new Probability(tokensRegex[resultRegex[i].getStart()], 0.99));

                                    final String RegexResults = String.format("         %1$5s..%2$-9s %3$-15s %4$-30s %5$s",
                                            resultRegex[i].getStart(),
                                            resultRegex[i].getEnd() - 1,
                                            getRegexType,
                                            data.getColumnName(),
                                            tokensRegex[resultRegex[i].getStart()]);
                                    log.info(RegexResults);

                                    //averageProbability = calculateAverage(probabilityListRegex);
                                    //data.setAverageProbability(0.99);
                                    //data.setAverageProbability(averageProbabilityRegex);
                                    finder.clearAdaptiveData();
                                    data.setProbabilityList(probabilityListRegex);
                                    averageProbability = calculateAverage(probabilityListRegex);
                                    data.setDictionariesFound(getRegexType);
                                    data.setAverageProbability(averageProbability);
                                }

                                break; // END LOOKUP WITH REGULAR EXPRESSIONS

                            case "NERDictionary": // START LOOKUP WITH DICTIONARIES
                                data.setAverageProbability(0);

                                DictionariesFound = new ArrayList<String>();
                                log.debug("Loading Dictionaries. Please wait ...");

                                log.debug("Dictionary considered for analysis: " + dictionaryPath);
                                log.debug("Tokenizing for column is starting ...");

                                final String tokensDict[] = tokenizer.tokenize(processingValue);
                                log.debug("Applying Dictionary model to column ...");

                                for (TokenNameFinder dictionaryNERDB : findersDict) {
                                    final Span DictSpansOnly[] = dictionaryNERDB.find(tokensDict);
                                    for (int i = 0; i < DictSpansOnly.length; i++) {

                                        data.setDictionariesFound(DictSpansOnly[i].getType());
                                        log.debug("Dictionary type is: " + DictSpansOnly[i].getType());

                                        log.debug("Dictionary text is: " + tokensDict[DictSpansOnly[i].getStart()]);

                                        final String DictionaryResults = String.format("         %1$5s..%2$-9s %3$-15s %4$-30s %5$s",
                                                DictSpansOnly[i].getStart(),
                                                DictSpansOnly[i].getEnd() - 1,
                                                DictSpansOnly[i].getType(),
                                                data.getColumnName(),
                                                tokensDict[DictSpansOnly[i].getStart()]);
                                        log.info(DictionaryResults);

                                        // dictionary finding always represent 85% of being correct. Não considero os 99% devido a poder ser outra entidade...
                                        probabilityListDict.add(new Probability(tokensDict[DictSpansOnly[i].getStart()], 0.85));
                                    }

                                    dictionaryNERDB.clearAdaptiveData();
                                    data.setProbabilityList(probabilityListDict);
                                    averageProbability = calculateAverage(probabilityListDict);
                                    data.setAverageProbability(averageProbability);
                                }
                                break; // END OF DICTIONARY LOOKUP

                            case "NERPattern": // START LOOKUP WITH PATTERNS
//                                log.info("");
//                                log.info(" >> Loading Patterns...");

                                String[] patternList = dataDiscoveryProperties.getProperty("pattern").split(",");

                                for (String filePattern : patternList) {
                                    File pattern = new File(filePattern);
                                    String[] auxNomeEntidade = filePattern.split("/");
                                    String entidadePattern = auxNomeEntidade[auxNomeEntidade.length - 1].replaceAll("\\.\\w+", "");

//                                    log.info("");
//                                    log.info("    - Evaluating Pattern: " + entidadePattern);
                                    Scanner scanPattern = new Scanner(pattern);

                                    ArrayList<String> consulta = new ArrayList<String>();

                                    while (scanPattern.hasNextLine()) {
                                        //log.info("Confirmação de entrada no while");
                                        String auxPattern = scanPattern.nextLine();
                                        consulta.add(auxPattern);
                                    }

                                    //log.debug("    - Dados a consultar: " + Consulta);
                                    for (String s : consulta) {
                                        Pattern patternCompile = Pattern.compile(s, Pattern.CASE_INSENSITIVE);  // case-insensitive matching
                                        Matcher matcher = patternCompile.matcher(processingValue);
                                        //log.debug("    - Dados a consultar: " + s);

                                        while (matcher.find()) { // find the next match
//                                            log.info("         [" + matcher.start() + ".." + matcher.end() + "]"
//                                                    + "  \t" + entidadePattern + "  \t" + matcher.group()); // Padrão encontrado

                                            final String PatternResults = String.format("         %1$5s..%2$-9s %3$-15s %4$-30s %5$s",
                                                    matcher.start(),
                                                    matcher.end() - 1,
                                                    entidadePattern,
                                                    data.getColumnName(),
                                                    matcher.group());
                                            log.info(PatternResults);

                                            probabilityListPattern.add(new Probability(matcher.group(), 0.98));
                                            data.setDictionariesFound(entidadePattern);
                                            //entidadePattern.clearAdaptiveData();
                                            data.setProbabilityList(probabilityListPattern);
                                            averageProbability = calculateAverage(probabilityListPattern);
                                            data.setAverageProbability(averageProbability);
                                        }

                                    }

                                }
                                break;

                            // START LOOKUP WITH OPENNLP (MAXIMUM ENTROPY)
                        }
                    }
                }

            } catch (SQLException sqle) {
                log.error(sqle.toString());
            }

            if ((averageProbability >= probabilityThreshold)) {
                data.setModel(NERModel);
                matches.add(data);
            }
        }
        return matches;
    }

    private List<MatchMetaData> discoverAgainstSingleModelNEREntropy(final IDBFactory factory, final Properties dataDiscoveryProperties,
            final Set<String> tables, final Model model, final double probabilityThreshold, final String NERModel)
            throws AnonymizerException, ParseException, IOException, DataDefenderException {

        final IMetaData metaData = factory.fetchMetaData();
        final List<MatchMetaData> map = metaData.getMetaData();

        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();
        MatchMetaData specialCaseData = null;
        boolean specialCase = false;

        final String[] specialCaseFunctions = dataDiscoveryProperties.getProperty("extentions").split(",");

        //NERDictionary
        String dictionaryPath = null;
        File nodeDict = null;
        String[] dictionaryPathList = null;
        List<TokenNameFinder> findersDict = null;
        ArrayList<String> DictionariesFound = null;

        InputStream modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("tokens"));
        TokenizerModel modelToken = new TokenizerModel(modelInToken);
        Tokenizer tokenizer = new TokenizerME(modelToken);

        // possible values for this:
        // NEREntropy - Uses only NER MaxEntropy OpenNLP trained models
        // NERDictionary - Uses only Dictionary XML OPenNLP implementation
        // NERRegex - Uses only Regex OpenNLP models
        // NEREntropyDictionary - Uses MaxEntropy and compares to Dictionary - Returns only the ones present in the dictionary
        if (specialCaseFunctions != null && specialCaseFunctions.length > 0) {
            specialCase = true;
        }

        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        List<Probability> probabilityList;
        double averageProbability;

        // Initization
        String getRegexType = "N/A";

        // FindersDict = getDictionariesFileForSearch(dictionaryPathList, nodeDict);
        for (final MatchMetaData data : map) {
            final String tableName = data.getTableName();
            final String columnName = data.getColumnName();
            //log.debug(data.getColumnType());

            // start with 0 as new column is analyzed
            probabilityList = new ArrayList<>();
            averageProbability = 0;
            log.debug("    - Analyzing...  [" + tableName + "] . [" + columnName + "] . [" + data.getColumnType() + "]");

            if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase(Locale.ENGLISH))) {
                log.debug("    - Continue ...");
                continue;
            }

            final String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");
            if (!CommonUtils.isEmptyString(tableNamePattern)) {
                final Pattern p = compile(tableNamePattern);
                if (!p.matcher(tableName).matches()) {
                    continue;
                }
            }

            final String table = sqlBuilder.prefixSchema(tableName);
            final int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
            final String query = sqlBuilder.buildSelectWithLimit(
                    "SELECT " + columnName
                    + " FROM " + table
                    + " WHERE " + columnName + " IS NOT NULL ORDER BY RAND() ", limit);
            log.debug("Executing query against database: " + query);

            try (Statement stmt = factory.getConnection().createStatement();
                    ResultSet resultSet = stmt.executeQuery(query);) {

                while (resultSet.next()) {
                    if (data.getColumnType().equals("BLOB") || data.getColumnType().equals("GEOMETRY")) {
                        continue;
                    }

//                    if (model.getName().equals("location") &&
//                        data.getColumnType().contains("INT")) {
//                        continue;
//                    }
                    final String sentence = resultSet.getString(1);
//                    if (specialCase) {
//                        try {
//                            for (int i=0; i<specialCaseFunctions.length; i++) {
//                                if (sentence != null && !sentence.equals("")) {
//                                    specialCaseData = (MatchMetaData)callExtention(specialCaseFunctions[i], data, sentence);
//                                }
//                            }
//                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
//                            log.error(e.toString());
//                        }
//                    }

                    if (sentence != null && !sentence.isEmpty()) {
                        String processingValue = "";
                        if (data.getColumnType().equals("DATE")
                                || data.getColumnType().equals("TIMESTAMP")
                                || data.getColumnType().equals("DATETIME")) {

                            final DateFormat originalFormat = new SimpleDateFormat(sentence, Locale.ENGLISH);
                            final DateFormat targetFormat = new SimpleDateFormat("MMM d, yy", Locale.ENGLISH);
                            final java.util.Date date = originalFormat.parse(sentence);
                            processingValue = targetFormat.format(date);
                        } else {
                            processingValue = sentence;
                        }

                        // Convert sentence into tokens
                        final String tokens[] = model.getTokenizer().tokenize(processingValue);

                        // Find names
                        final Span nameSpans[] = model.getNameFinder().find(tokens);

                        // Find probabilities for names
                        final double[] spanProbs = model.getNameFinder().probs(nameSpans);

                        // Collect top X tokens with highest probability
                        // Display names
                        for (int i = 0; i < nameSpans.length; i++) {
                            log.debug("Span            : " + nameSpans[i].toString());
                            log.debug("Covered text is : " + tokens[nameSpans[i].getStart()]);
                            log.debug("Probability is  : " + spanProbs[i]);

                            final String MaxEntResults = String.format("         %1$5s..%2$-9s %3$-15s %4$-30s %5$s",
                                    nameSpans[i].getStart(),
                                    nameSpans[i].getEnd() - 1,
                                    model.getName(),
                                    data.getColumnName(),
                                    //decimalFormat.format(spanProbs[i]),
                                    tokens[nameSpans[i].getStart()]);
                            log.info(MaxEntResults);

                            probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], spanProbs[i]));

                        }

                        // From OpenNLP documentation:
                        // After every document clearAdaptiveData must be called to clear the adaptive data in the feature generators.
                        // Not calling clearAdaptiveData can lead to a sharp drop in the detection rate after a few documents.
                        data.setProbabilityList(probabilityList);
                        averageProbability = calculateAverage(probabilityList);
                        model.getNameFinder().clearAdaptiveData();
                        data.setDictionariesFound(model.getName());
                        data.setAverageProbability(averageProbability);
                    }
                }

            } catch (SQLException sqle) {
                log.error(sqle.toString());
            }

            if ((averageProbability >= probabilityThreshold)) {
                data.setModel(NERModel);
                matches.add(data);
            }
        }
        return matches;
    }

//    /**
//     * Calls a function defined as an extention
//     * @param function
//     * @param data
//     * @param text
//     * @return
//     * @throws SQLException
//     * @throws NoSuchMethodException
//     * @throws SecurityException
//     * @throws IllegalAccessException
//     * @throws IllegalArgumentException
//     * @throws InvocationTargetException
//     */
//    private Object callExtention(final String function, MatchMetaData data, String text)
//            throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        
//        if (function == null || function.equals("")) {
//            log.warn("Function " + function + " is not defined");
//            return null;
//        }
//
//        Object value = null;
//
//        try {
//            final String className = Utils.getClassName(function);
//            final String methodName = Utils.getMethodName(function);
//            final Method method = Class.forName(className).getMethod(methodName, new Class[]{MatchMetaData.class, String.class});
//
//            final SpecialCase instance = (SpecialCase) Class.forName(className).newInstance();
//
//            final Map<String, Object> paramValues = new HashMap<>(2);
//            paramValues.put("metadata", data);
//            paramValues.put("text", text);
//
//            value = method.invoke(instance, data, text);
//
//        } catch (AnonymizerException | InstantiationException | ClassNotFoundException ex) {
//            log.error(ex.toString());
//        }
//        
//        return value;
//    }
//         
}
