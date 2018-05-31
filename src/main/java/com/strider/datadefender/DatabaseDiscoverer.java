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

import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.database.sqlbuilder.ISQLBuilder;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.report.ReportUtil;
import com.strider.datadefender.specialcase.SpecialCase;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.Score;
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
import static com.strider.datadefender.utils.AppProperties.loadProperties;

import java.io.IOException;
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
public class DatabaseDiscoverer extends Discoverer {

    private static final Logger log = getLogger(DatabaseDiscoverer.class);

    private static String[] modelList;

    @SuppressWarnings("unchecked")
    public List<MatchMetaData> discover(final IDBFactory factory, final Properties dataDiscoveryProperties, final Set<String> tables)
            throws AnonymizerException, ParseException, IOException, DataDefenderException {
        
        log.info("============================================================");
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

        for(String NERModel : NERmodels){
            log.info("------------------------------------------------------------");
            log.info(" -> Processing NER Model Type: " + NERModel);
            log.info("");
            if(NERModel.equals("NEREntropy")){
                for(String model: modelList){
                    log.info("");
                    log.info("         Processing model: " + model);
                    final Model modelEntropy = createModel(dataDiscoveryProperties, model);
                    matches = discoverAgainstSingleModelNEREntropy(factory, dataDiscoveryProperties, tables, modelEntropy, probabilityThreshold, NERModel);
                    
                }
            }else{
                matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, probabilityThreshold, NERModel);
            }
            finalList = ListUtils.union(finalList, matches);
        }
        
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        log.info("");
        log.info("------------------------------------------------------------");
        log.info(" -> List of suspects:");
        log.info("    (Ler 6 colunas: Tabela, Coluna, Probabilidade, Modelo NER, [Tipo de Dados], Nr Linhas)");
        log.info("");
        
        final Score score = new Score();
        int highRiskColumns = 0;
        int rowCount=0;
        for(final MatchMetaData data: finalList) {
            // empty arraylist 
            listSampleData.clear();

            // Row count
            if (calculate_score.equals("yes")) {
                log.debug("Skipping table rowcount...");
                rowCount = ReportUtil.rowCount(factory, data.getTableName());
            }
            
            // Getting 5 sample values
            // final List<String> sampleDataList = ReportUtil.sampleData(factory, data.getTableName(), data.getColumnName());

//            // Output
//            log.debug("Table.Column                : " + data.toString());
//            log.info( CommonUtils.fixedLengthString('=', data.toString().length() + 30));
//            
//            // Prob
//            log.debug("Probability                 : " + decimalFormat.format(data.getAverageProbability()));
//
//            // Model
//            log.debug("Model                       : " + data.getModel());
//
//            // Dictionaries
//            log.debug("Data Type                   : " + data.getDictionariesFound());
            
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
            //log.info(CommonUtils.fixedLengthString('-', 11));
            
            final List<Probability> probabilityList = data.getProbabilityList();
            Collections.sort(probabilityList,Comparator.comparingDouble(Probability::getProbabilityValue).reversed());
            
            // Score calculation is evaluated with calculate_score parameter
            if (calculate_score.equals("yes")) {
                if (score.columnScore(rowCount).equals("High")) {
                    highRiskColumns++;
                }
            }
            
            log.info("    - " + data.getTableName() +
                     ", " + data.getColumnName() +
                     ", " + decimalFormat.format(data.getAverageProbability()) +
                     ", " + data.getModel() +
                     ", [" + data.getDictionariesFound() + "]" +
                     ", " + rowCount);
            
            boolean encontreiTabela=false, encontreiCampo=false;
            for(DatabaseResults dbr : results){
                if(dbr.equals(new DatabaseResults(data.getTableName(), data.getColumnName()))){
                    encontreiTabela = true;
                    encontreiCampo=false;
                    for(String s : dbr.getCampos()){
                        if(s.equals(data.getDictionariesFound())){
                            encontreiCampo = true;
                        }
                    }
                    if(!encontreiCampo){
                        ArrayList<String> auxCampos = dbr.getCampos();
                        auxCampos.add(data.getDictionariesFound());
                        dbr.setCampos(auxCampos);
                    } 
                }
            }
            
            if(!encontreiTabela){
                ArrayList<String> auxCampos = new ArrayList<String>();
                auxCampos.add(data.getDictionariesFound());
                results.add(new DatabaseResults(data.getTableName(), data.getColumnName(), data.getAverageProbability(),auxCampos));
            }
            
            // Restringir a impressão apenas a 5 entidades com probabilidade mais elevada.
            
            int y=0;
            if (data.getProbabilityList().size() >= 5) {
                y = 5;
            } else {
                y = data.getProbabilityList().size();
            }

            for (int i=0; i<y; i++) {
                final Probability p = data.getProbabilityList().get(i);
                log.info("         " + p.getSentence() + ": " + decimalFormat.format(p.getProbabilityValue()));
                listSampleData.add(p.getSentence());
            }
            
//            log.info(String.format("Summary: %s / %s / %s / %s / %s / %s", "Schema", "Table", "Column", "Probability", "NER Model", "[Data Type]"));
//            final String result = String.format("         %s / %s / %s / %s / %s / [%s]", 
//                                                data.getSchemaName(),
//                                                data.getTableName(),
//                                                data.getColumnName(),
//                                                decimalFormat.format(data.getAverageProbability()),
//                                                data.getModel(),
//                                                data.getDictionariesFound());
//            log.info(result);
//            log.info("\n");
        
        }
        
        
        EscreverFicheiro ef = new EscreverFicheiro();
        ef.cabecalhoCSVDatabase();
        
        log.info("");
        log.info("============================================================");
        log.info("============================================================");
        log.info(" -> Quadro resumo dos resultados suspeitos:");
        log.info("    (ler 4 colunas: Tabela, Coluna, Probabilidade, [ Tipo de dados ])\n");
        
        for(DatabaseResults dbr : results){
            ef.escreverCSV(dbr.getTabela(), dbr.getColuna(), ReportUtil.rowCount(factory, dbr.getTabela()), dbr.camposToString());
            log.info(dbr.toString());
        }
        
        // Only applicable when parameter table_rowcount=yes otherwise score calculation should not be done
        if (calculate_score.equals("yes")) {
            log.info("");
            log.info("    - Overall score: " + score.dataStoreScore());
            log.info("");
            
            if (finalList != null && finalList.size() > 0) {
                log.info("============================================================");
                final int threshold_count    = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_count"));
                if (finalList.size() > threshold_count) {
                    log.info("Number of PI [" + finalList.size() + "] columns is higher than defined threashold [" + threshold_count + "]");
                } else {
                    log.info("Number of PI [" + finalList.size() + "] columns is lower or equal than defined threashold [" + threshold_count + "]");
                }
                final int threshold_highrisk = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_highrisk"));
                if (highRiskColumns > threshold_highrisk) {
                    log.info("Number of High risk PI [" + highRiskColumns + "] columns is higher than defined threashold [" + threshold_highrisk + "]\n");
                } else {
                    log.info("Number of High risk PI [" + highRiskColumns + "] columns is lower or equal than defined threashold [" + threshold_highrisk + "]\n");
                }
            }
            
        }
        else {
            log.info("Overall score: N/A");
        }
 
        return matches;
    }
    
    private List<TokenNameFinder> getDictionariesFileForSearch(String[] dictionaryPathList, File nodeDict)
            throws IOException {
        
        List<TokenNameFinder> findersDict = new ArrayList<>();
        
        // Add all dictionaries
        for (final String dictPath: dictionaryPathList) {
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
        
        for(final MatchMetaData data: map) {
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
                "SELECT " + columnName +
                " FROM " + table +
                " WHERE " + columnName  + " IS NOT NULL ORDER BY RAND()", limit);  // Procura aleatória 
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
                    if (specialCase) {
                        try {
                            for (int i=0; i<specialCaseFunctions.length; i++) {
                                if (sentence != null && !sentence.equals("")) {
                                    specialCaseData = (MatchMetaData)callExtention(specialCaseFunctions[i], data, sentence);
                                }
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
                            log.error(e.toString());
                        }
                    }

                    if (sentence != null && !sentence.isEmpty()) {
                        String processingValue = "";
                        if (data.getColumnType().equals("DATE") ||
                                data.getColumnType().equals("TIMESTAMP") ||
                                data.getColumnType().equals("DATETIME")) {
                            
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
                                 
                                    probabilityListRegex.add(new Probability(tokensRegex[resultRegex[i].getStart()], 0.99));

                                //averageProbability = calculateAverage(probabilityListRegex);
                                //data.setAverageProbability(0.99);
                                //data.setAverageProbability(averageProbabilityRegex);
                                }
                                
                                finder.clearAdaptiveData();
                                data.setProbabilityList(probabilityListRegex);
                                averageProbability = calculateAverage(probabilityListRegex);
                                data.setDictionariesFound(getRegexType);
                                data.setAverageProbability(averageProbability);
                                
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
                                    for( int i = 0; i < DictSpansOnly.length; i++) {

                                        data.setDictionariesFound(DictSpansOnly[i].getType());
                                        log.debug("Dictionary type is: " + DictSpansOnly[i].getType());
                                            
                                        // dictionary finding always represent 99% of being correct.
                                        log.debug("Dictionary text is: " + tokensDict[DictSpansOnly[i].getStart()]);
                                        
                                        probabilityListDict.add(new Probability(tokensDict[DictSpansOnly[i].getStart()], 0.99));
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
                                    String[] auxNomeEntidade = filePattern.split("\\\\");
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
                                            log.info("         [" + matcher.start() + ".." + matcher.end() + "]"
                                                    + "  \t" + matcher.group()); // Padrão encontrado
                                            
                                            probabilityListPattern.add(new Probability(matcher.group(), 0.99));
                                            
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
        
        for(final MatchMetaData data: map) {
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
                "SELECT " + columnName +
                " FROM " + table +
                " WHERE " + columnName  + " IS NOT NULL ORDER BY RAND() ", limit);
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
                    if (specialCase) {
                        try {
                            for (int i=0; i<specialCaseFunctions.length; i++) {
                                if (sentence != null && !sentence.equals("")) {
                                    specialCaseData = (MatchMetaData)callExtention(specialCaseFunctions[i], data, sentence);
                                }
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
                            log.error(e.toString());
                        }
                    }

                    if (sentence != null && !sentence.isEmpty()) {
                        String processingValue = "";
                        if (data.getColumnType().equals("DATE") ||
                                data.getColumnType().equals("TIMESTAMP") ||
                                data.getColumnType().equals("DATETIME")) {
                            
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
                            for( int i = 0; i<nameSpans.length; i++) {
                                log.debug("Span            : " + nameSpans[i].toString());
                                log.debug("Covered text is : " + tokens[nameSpans[i].getStart()]);
                                log.debug("Probability is  : " + spanProbs[i]);
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

    /**
     * Calls a function defined as an extention
     * @param function
     * @param data
     * @param text
     * @return
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private Object callExtention(final String function, MatchMetaData data, String text)
            throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        if (function == null || function.equals("")) {
            log.warn("Function " + function + " is not defined");
            return null;
        }

        Object value = null;

        try {
            final String className = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Method method = Class.forName(className).getMethod(methodName, new Class[]{MatchMetaData.class, String.class});

            final SpecialCase instance = (SpecialCase) Class.forName(className).newInstance();

            final Map<String, Object> paramValues = new HashMap<>(2);
            paramValues.put("metadata", data);
            paramValues.put("text", text);

            value = method.invoke(instance, data, text);

        } catch (AnonymizerException | InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return value;
    }
         
}
