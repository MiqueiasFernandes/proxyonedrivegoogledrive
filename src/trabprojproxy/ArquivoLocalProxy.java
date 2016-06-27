/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import java.io.IOException;

public class ArquivoLocalProxy implements IArquivoProxy {

    private final String nomeArquivo;
    private final ArquivoLocalReal arquivoLocalReal;

    public ArquivoLocalProxy(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        arquivoLocalReal = new ArquivoLocalReal(nomeArquivo);
    }

    @Override
    public String lerConteudo() throws IOException, Exception {
        return arquivoLocalReal.lerConteudo();
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws IOException, Exception {
        return arquivoLocalReal.gravaConteudo(conteudo);
    }

}
