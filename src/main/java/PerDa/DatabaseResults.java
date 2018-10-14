/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerDa;

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
    private String tipoColuna;
    private int grauRisco;
    
    final DecimalFormat decimalFormat = new DecimalFormat("#.##");
       
    public DatabaseResults() {
    }

    public DatabaseResults(String tabela, String coluna) {
        this.tabela = tabela;
        this.coluna = coluna;
    }

    public DatabaseResults(String tabela, String coluna, double probabilidade, ArrayList<String> campos, String tipoColuna) {
        this.tabela = tabela;
        this.coluna = coluna;
        this.probabilidade = probabilidade;
        this.campos = campos;
        this.tipoColuna = tipoColuna;
        
    }
    
    public String getTipoColuna() {
        return tipoColuna;
    }

    public void setTipoColuna(String tipoColuna) {
        this.tipoColuna = tipoColuna;
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

    public int getGrauRisco() {
        return grauRisco;
    }

    public void setGrauRisco(int grauRisco) {
        this.grauRisco = DataDefender.getClassificacaoRisco(tipoColuna);
    }

    @Override
    // Para imprimir no dataLogs
    public String toString() {
       String aux = "";
       //aux += "[";
       for(String c : this.campos){
           aux += c + ", ";
       }
       
       aux = aux.substring(0, aux.length()-2);
       //aux += "]";
       return aux;
    }
    
    // Para escrever no ficheiro CSV
    public String camposToString(){
        String aux = "";
        Boolean jaEncontrei;
       for(int i=0; i< this.campos.size(); i++){
           jaEncontrei = false;
           for(int j= i-1; j>=0; j--){
               if(this.campos.get(j).equals(this.campos.get(i)))
                   jaEncontrei = true;
           }
           if(!jaEncontrei)
            aux+=this.campos.get(i) + ", ";
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
