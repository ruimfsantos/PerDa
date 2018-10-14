/**
 * Copyright 2014-2016, Armenak Grigoryan, and individual contributors as indicated
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

import PerDa.database.IDBFactory;
import static PerDa.utils.AppProperties.loadProperties;
import PerDa.utils.ApplicationLock;
import PerDa.utils.CommonUtils;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Entry point to Data Defender.
 *
 * This class will parse and analyze the parameters and execute appropriate
 * service.
 *
 * @author Redglue
 * @author ruimfsantos
 */
public class DataDefender {

    private static final Logger log = getLogger(DataDefender.class);

    @SuppressWarnings("unchecked")
    public static void main(final String[] args)
            throws ParseException, DataDefenderException, AnonymizerException,
            IOException, SAXException, TikaException, java.text.ParseException,
            Exception, java.lang.NullPointerException, java.lang.IllegalArgumentException {

        // Start run time execution
        
        // Ensure we are not trying to run second instance of the same program
        final ApplicationLock al = new ApplicationLock("PerDa2Disco");

        if (al.isAppActive()) {
            log.error("Another instance of this program is already active");
            System.exit(1);
        }

        log.info(CommonUtils.fixedLengthString('=', +80));
        log.info(" -> Command-line arguments: " + Arrays.toString(args));
        log.info(CommonUtils.fixedLengthString('-', +70));
        final Options options = createOptions();
        final CommandLine line = getCommandLine(options, args);

        @SuppressWarnings("unchecked")
        List<String> unparsedArgs = line.getArgList();

        if (line.hasOption("help") || args.length == 0 || unparsedArgs.size() < 1) {
            help(options);
            return;
        }
        if (line.hasOption("debug")) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        } else {
            LogManager.getRootLogger().setLevel(Level.INFO);
        }

        final String cmd = unparsedArgs.get(0); // get & remove command arg
        unparsedArgs = unparsedArgs.subList(1, unparsedArgs.size());

        // Verificar quais são as pastas partilhadas do computador
        List errors = new ArrayList();

        preparaFicheiros();

        if ("file-discovery".equals(cmd)) {
            fileDiscovery(errors, cmd, line);
        }

        if ("database-discovery".equals(cmd)) {
            databaseDiscovery(line, cmd, unparsedArgs, options);
        }

    }

    public static boolean databaseDiscovery(final CommandLine line, final String cmd, List<String> unparsedArgs, final Options options) throws DataDefenderException, AnonymizerException, java.text.ParseException, IOException {
        List errors;
        errors = PropertyCheck.checkDtabaseProperties();
        if (errors.size() > 0) {
            displayErrors(errors);
            return true;
        }
        final Properties props = loadProperties(line.getOptionValue('P', "db.properties"));
        try (final IDBFactory dbFactory = IDBFactory.get(props)) {
            switch (cmd) {
                case "database-discovery":
                    if (line.hasOption('c')) {
                        errors = PropertyCheck.check(cmd, 'c');
                        if (errors.size() > 0) {
                            displayErrors(errors);
                            return true;
                        }
                        final String columnPropertyFile = line.getOptionValue('C', "columndiscovery.properties");
                        final Properties columnProperties = loadProperties(columnPropertyFile);
                        final ColumnDiscoverer discoverer = new ColumnDiscoverer();
                        discoverer.discover(dbFactory, columnProperties, getTableNames(unparsedArgs, columnProperties));
                        if (line.hasOption('r')) {
                            discoverer.createRequirement(line.getOptionValue('R', "Sample-Requirement.xml"));
                        }
                    } else if (line.hasOption('d')) {
                        errors = PropertyCheck.check(cmd, 'd');
                        if (errors.size() > 0) {
                            displayErrors(errors);
                            return true;
                        }
                        final String datadiscoveryPropertyFile = line.getOptionValue('D', "datadiscovery.properties");
                        final Properties dataDiscoveryProperties = loadProperties(datadiscoveryPropertyFile);
                        final DatabaseDiscoverer discoverer = new DatabaseDiscoverer();
                        discoverer.discover(dbFactory, dataDiscoveryProperties, getTableNames(unparsedArgs, dataDiscoveryProperties));
                        if (line.hasOption('r')) {
                            discoverer.createRequirement(line.getOptionValue('R', "Sample-Requirement.xml"));
                        }
                    }
                    break;
                default:
                    help(options);
                    break;
            }
        }

        return false;
    }

    private static void preparaFicheiros() {
        // Remover os ficheiros para gerar página WEB
        File fileResumo = new File("PerDaInterface/public_html/Resumo.csv");
        if (fileResumo.exists()) {
            fileResumo.delete();
        }

        File fileResultado = new File("PerDaInterface/public_html/Resultado.csv");
        if (fileResultado.exists()) {
            fileResultado.delete();
        }

        File fileExtensao = new File("PerDaInterface/public_html/Extensao.csv");
        if (fileExtensao.exists()) {
            fileExtensao.delete();
        }

        File fileGovernacao = new File("PerDaInterface/public_html/Governacao.csv");
        if (fileGovernacao.exists()) {
                fileGovernacao.delete();
        }
    }
    
        /**
         * Parses command line arguments
         *
         * @param options
         * @param args
         * @return CommandLine
         */
    private static CommandLine getCommandLine(final Options options, final String[] args) {
        final CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args, false);
        } catch (ParseException e) {
            help(options);
        }
        return line;
    }

    /**
     * Creates options for the command line
     *
     * @return Options
     */
    private static Options createOptions() {
        final Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption("c", "columns", false, "discover candidate column names for anonymization based on provided patterns");
        options.addOption("C", "column-properties", true, "define column property file");
        options.addOption("d", "data", false, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("D", "data-properties", true, "discover candidate column for anonymization based on semantic algorithms");
        options.addOption("P", "database properties", true, "define database property file");
        options.addOption("F", "file discovery properties", true, "define file discovery property file");
        options.addOption("debug", false, "enable debug output");
        return options;
    }

    /**
     * Displays command-line help options
     *
     * @param Options
     */
    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PerDa2Disco database-discovery|file-discovery [options] [table1 [table2 [...]]]", options);
    }

    /**
     * Returns the list of unparsed arguments as a list of table names by
     * transforming the strings to lower case.
     *
     * This guarantees table names to be in lower case, so functions comparing
     * can use contains() with a lower case name.
     *
     * If tables names are not supplied via command line, then will search the
     * property file for space separated list of table names.
     *
     * @param tableNames
     * @param props application property file
     * @return The list of table names
     */
    public static Set<String> getTableNames(List<String> tableNames, final Properties props) {
        //List<String> tableNamesTmp = new ArrayList();

        if (tableNames.isEmpty()) {
            final String tableStr = props.getProperty("tables");
            if (tableStr == null) {
                return Collections.emptySet();
            }
            tableNames = Arrays.asList(tableStr.split(" "));
            log.info("Adding tables from property file.");
        }
        final Set<String> tables = tableNames.stream().map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
        log.info("Tables: " + Arrays.toString(tables.toArray()));
        return tables;
    }

    private static void displayErrors(final List<String> errors) {
        for (final String err : errors) {
            log.info(err);
        }
    }

    public static int getClassificacaoRisco(String tipoEntidade) {
        int risco = 0;
        switch (tipoEntidade) {
            case "Nome":
            case "Name":
            case "Localidade":
            case "Location":
            case "DesignacaoCP":
            case "EstadoCivil":
            case "Habilitacao":
            case "Profissao":
            case "Genero":
            case "Data":
            case "CPostal":
            case "Telemovel":
            case "Telefone":
            case "Organizacao":
            case "Organization":
            case "EMAIL":
                risco = 1;
                break;

            case "CCidadao":
            case "NIF":
            case "NISS":
            case "CConducao":
            case "IBAN":
            case "NIB":
            case "CCredito":
            case "PCI-VISA":
            case "PCI-Master":
            case "PCI-AmEx":
            case "PCI-Diners":
            case "PCI-Discover":
            case "PCI-JCB":
            case "PCI-Maestro":
            case "PCI-Payment":
            case "Passaporte":
                risco = 3;
                break;

            case "Crime":
            case "Filiacao":
            case "Religiao":
            case "Saude":
                risco = 5;
                break;

            default:
                risco = 0;
                break;
        }

        return risco;
    }

    public static void fileDiscovery(List errors, String cmd, CommandLine line) throws DataDefenderException, IOException, SAXException, TikaException, NullPointerException, Exception {
        errors = PropertyCheck.check(cmd, ' ');
        if (errors.size() > 0) {
            displayErrors(errors);
            return;
        }

        ArrayList<String> diretorios = new ArrayList<String>();
        diretorios.add("\\\\127.0.0.1\\d$\\");

        InterfaceRede iur = new InterfaceRede();
        iur.obterSharedFolders();

        // Gerar o ficheiro de propriedades automaticamente
        //iur.gerarFileProperties(diretorios); //Tem de estar oculta porque não estou a gerar o ficheiro automaticamente
        // Fazer a descoberta dos ficheiros existentes na pasta directório
        final String fileDiscoveryPropertyFile = line.getOptionValue('F', "filediscovery.properties");
        final Properties fileDiscoveryProperties = loadProperties(fileDiscoveryPropertyFile);
        final FileDiscoverer discoverer = new FileDiscoverer();
        discoverer.descobertaFicheiros(fileDiscoveryProperties);
        Scanner scan = new Scanner(System.in); //scanner for input

        // Depois da identificação dos ficheiros existentes questionar se pretende analisá-los
        char a = 'a';
        do {
            log.info("");
            log.info(CommonUtils.fixedLengthString('=', +80));
            log.info(" -> Pretende analisar os ficheiros supracitados? (y/n)\n    ");
            a = scan.next().charAt(0);
        } while (a != 'Y' && a != 'y' && a != 'N' && a != 'n');

        if (a == 'Y' || a == 'y') {
            discoverer.discover(fileDiscoveryProperties);
        }

        return;

    }

    public static void webFileDiscovery(Properties fileDiscoveryProperties) throws DataDefenderException, IOException, SAXException, TikaException, NullPointerException, Exception {

        preparaFicheiros();

//            ArrayList<String> diretorios = new ArrayList<String>();
//            diretorios.add("\\\\127.0.0.1\\d$\\");
//            InterfaceRede iur = new InterfaceRede();
//            iur.obterSharedFolders();
        // Gerar o ficheiro de propriedades automaticamente
        //iur.gerarFileProperties(diretorios); //Tem de estar oculta porque não estou a gerar o ficheiro automaticamente
        // Fazer a descoberta dos ficheiros existentes na pasta directório
//            final String fileDiscoveryPropertyFile = line.getOptionValue('F', "filediscovery.properties");
//            final Properties fileDiscoveryProperties = loadProperties(fileDiscoveryPropertyFile);
        final FileDiscoverer discoverer = new FileDiscoverer();
        discoverer.descobertaFicheiros(fileDiscoveryProperties);
//            Scanner scan = new Scanner(System.in); //scanner for input

        // Depois da identificação dos ficheiros existentes questionar se pretende analisá-los
//            char a = 'a';
//            do {
//                log.info("");
//                log.info(CommonUtils.fixedLengthString('=', +80));
//                log.info(" -> Pretende analisar os ficheiros supracitados? (y/n)\n    ");
//                a = scan.next().charAt(0);
//            } while (a != 'Y' && a != 'y' && a != 'N' && a != 'n');
//            if (a == 'Y' || a == 'y') {
        discoverer.discover(fileDiscoveryProperties);
//            }

        return;

    }
}
