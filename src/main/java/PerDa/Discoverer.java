/**
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
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

import java.util.List;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import PerDa.database.metadata.MatchMetaData;
import PerDa.utils.RequirementUtils;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import static org.apache.log4j.Logger.getLogger;


/**
 * Holds common logic for Discoverers.
 * @author Akira Matsuo
 * @author ruimfsantos
 */
public abstract class Discoverer { //implements IDiscoverer {

    protected List<MatchMetaData> matches;

    private static final Logger log = getLogger(Discoverer.class);

    public void createRequirement(final String fileName) throws AnonymizerException {
        if (matches == null || matches.isEmpty()) {
            throw new AnonymizerException("No matches to create requirement from!");
        }
        RequirementUtils.write(RequirementUtils.create(matches), fileName);
    }

    public double calculateAverage(final List <Probability> values) {
        Double sum = 0.0;
        if(!values.isEmpty()) {
            for (final Probability value : values) {
                sum += value.getProbabilityValue();
            }
            return sum / values.size();
        }
        return sum;
    }

    /**
     * Creates model POJO based on OpenNLP model
     *
     * @param dataDiscoveryProperties
     * @param modelName
     * @return Model
     */
    public  Model createModel(final Properties dataDiscoveryProperties, final String modelName) {
        InputStream modelInToken = null;
        InputStream modelIn = null;
//        InputStream modelInSentence = null;
        
        TokenizerModel modelToken;
        Tokenizer tokenizer = null;
      
        TokenNameFinderModel model = null;
        NameFinderME nameFinder = null;
                        
//        SentenceModel modelSentence = null;
//        SentenceDetectorME sentenceDetector = null;

        try {
            log.debug("      Model name: " + modelName);

            log.debug("      ProprietyFile: " + dataDiscoveryProperties.toString());
            modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("tokens"));

            log.debug("      BinFile: " + dataDiscoveryProperties.getProperty(modelName));
            modelIn = new FileInputStream(dataDiscoveryProperties.getProperty(modelName));

            modelToken = new TokenizerModel(modelInToken);
            tokenizer = new TokenizerME(modelToken);
          
            model = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(model);
           
            //modelSentence = new SentenceDetector(modelInSentence);
//            sentenceDetector = new SentenceDetectorME(modelSentence);
            
            modelInToken.close();
            modelIn.close();
//            modelInSentence.close();
            
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());
            try {
                if (modelInToken != null) {
                    modelInToken.close();
                }
                if (modelIn != null) {
                    modelIn.close();
                }
//                if (modelInSentence != null) {
//                    modelInSentence.close();
//                }
            } catch (IOException ioe) {
                log.error(ioe.toString());
            }
            
        } catch (IOException ex) {
            log.error(ex.toString());
        }

        return new Model(tokenizer, nameFinder, /*modelSentence,*/ modelName);
    }
}
