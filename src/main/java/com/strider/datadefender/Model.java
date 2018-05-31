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

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;

public class Model {
    final private Tokenizer tokenizer;
    final private NameFinderME nameFinder;
    final private String name;

    public Model(final Tokenizer tokenizer, final NameFinderME nameFinder, final String name) {
        this.name       = name;
        this.tokenizer  = tokenizer;
        this.nameFinder = nameFinder;
    }

    Model(Tokenizer tokenizer, NameFinderME nameFinder, SentenceDetectorME sentenceDetector, String modelName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Model(Tokenizer tokenizer, NameFinderME nameFinder, SentenceModel modelSentence, String modelName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Tokenizer getTokenizer() {
        return this.tokenizer;
    }

    public NameFinderME getNameFinder() {
        return this.nameFinder;
    }

    public String getName() {
        return this.name;
    }

}
