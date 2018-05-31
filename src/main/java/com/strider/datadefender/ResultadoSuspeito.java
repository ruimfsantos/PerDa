/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender;

/**
 * @author ruimfsantos
 */
public class ResultadoSuspeito {

    private String nomeFicheiro;
    private String entidade;
    private double probabilidade;
    private String modelo;
    private String tipoEntidade;

    public ResultadoSuspeito(String nomeFicheiro, String entidade, double probabilidade, String modelo, String tipoEntidade) {
        this.nomeFicheiro = nomeFicheiro;
        this.entidade = entidade;
        this.probabilidade = probabilidade;
        this.modelo = modelo;
        this.tipoEntidade = tipoEntidade;
    }

    public String getTipoEntidade() {
        return tipoEntidade;
    }

    public void setTipoEntidade(String tipoEntidade) {
        this.tipoEntidade = tipoEntidade;
    }

    public String getNomeFicheiro() {
        return nomeFicheiro;
    }

    public void setNomeFicheiro(String nomeFicheiro) {
        this.nomeFicheiro = nomeFicheiro;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public double getProbabilidade() {
        return probabilidade;
    }

    public void setProbabilidade(double probabilidade) {
        this.probabilidade = probabilidade;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    @Override
    public String toString() {
        return "    - " + tipoEntidade + "\t\t" + probabilidade + "\t\t" + modelo + "\t\t" + entidade;
        
    }

}
