/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
  * @author ruimfsantos
 */
public class DatabaseResults {
    private String tabela;
    private String coluna;
    private double probabilidade;
    ArrayList<String> campos;
    
    final DecimalFormat decimalFormat = new DecimalFormat("#.##");
       
    public DatabaseResults() {
    }

    public DatabaseResults(String tabela, String coluna) {
        this.tabela = tabela;
        this.coluna = coluna;
    }

    public DatabaseResults(String tabela, String coluna, double probabilidade, ArrayList<String> campos) {
        this.tabela = tabela;
        this.coluna = coluna;
        this.probabilidade = probabilidade;
        this.campos = campos;

    }

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    public String getColuna() {
        return coluna;
    }

    public void setColuna(String coluna) {
        this.coluna = coluna;
    }

    public double getProbabilidade() {
        return probabilidade;
    }

    public void setProbabilidade(double probabilidade) {
        this.probabilidade = probabilidade;
    }

    public ArrayList<String> getCampos() {
        return campos;
    }

    public void setCampos(ArrayList<String> campos) {
        this.campos = campos;
    }

    @Override
    public String toString() {
        String aux = "";
       aux+= "    - " + tabela + "\t\t" + coluna + "\t\t" + probabilidade + "\t\t[ ";
       for(String c : this.campos){
           aux+=c + "; ";
       }
       
       aux=aux.substring(0, aux.length()-2);
       aux+=" ]";
       return aux;
    }
    
    public String camposToString(){
        String aux = "";
       for(String c : this.campos){
           aux+=c + ", ";
       }
       
       aux=aux.substring(0, aux.length()-2);
       return aux;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DatabaseResults other = (DatabaseResults) obj;
        if (!Objects.equals(this.tabela, other.tabela)) {
            return false;
        }
        if (!Objects.equals(this.coluna, other.coluna)) {
            return false;
        }
        return true;
    }
    
    
          
}
