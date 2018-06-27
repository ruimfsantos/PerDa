/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerDa;


/**
 * @author ruimfsantos
 */
public class ResultadoSuspeito {

    private String nomeFicheiro;
    private String entidade;
    private double probabilidade;
    private String modelo;
    private String tipoEntidade;
    private int posInicial;
    private int posFinal;
    private int classificacaoRisco;
    
    public ResultadoSuspeito(String nomeFicheiro, String entidade, double probabilidade, String modelo, String tipoEntidade, int posInicial, int posFinal) {
        this.nomeFicheiro = nomeFicheiro;
        this.entidade = entidade;
        this.probabilidade = probabilidade;
        this.modelo = modelo;
        this.tipoEntidade = tipoEntidade;
        this.posInicial = posInicial;
        this.posFinal = posFinal;
        this.classificacaoRisco = DataDefender.getClassificacaoRisco(tipoEntidade);
    }

    ResultadoSuspeito(String ficheiro, String token, double round, String modelo, String name, int posFinal) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getPosInicial() {
        return posInicial;
    }
    
    public void setPosInicial(int posInicial) {
        this.posInicial = posInicial;
    }
    
    public int getPosFinal() {
        return posFinal;
    }
    
    public void setPosFinal(int posFinal) {
        this.posFinal = posFinal;
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

    public int getClassificacaoRisco() {
        return classificacaoRisco;
    }

    public void setClassificacaoRisco(int classificacaoRisco) {
        this.classificacaoRisco = classificacaoRisco;
    }
    
    @Override
    public String toString() {
        
        String aux = String.format("    - %-17s %-7s %-9s %-18s %-9s %-9s %s", 
                                            this.getTipoEntidade(),
                                            this.getClassificacaoRisco(),
                                            this.getProbabilidade(), 
                                            this.getModelo(),
                                            this.getPosInicial(),
                                            this.getPosFinal(),
                                            this.getEntidade());
        return aux;
        
    }

}
