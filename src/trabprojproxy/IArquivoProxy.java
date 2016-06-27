/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

/**
 *
 * @author mfernandes
 */
public interface IArquivoProxy {

    public abstract String lerConteudo() throws Exception;

    public abstract boolean gravaConteudo(String conteudo) throws Exception;

}
