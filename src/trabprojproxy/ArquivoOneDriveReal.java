/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import java.io.File;
import trabprojproxy.OneDrive.OneDriveAPI;

/**
 *
 * @author mfernandes
 */
public class ArquivoOneDriveReal implements IArquivoProxy {

    private final String nomeArquivo;
    private final OneDriveAPI oneDriveAPI;
    private final ArquivoLocalProxy arquivoLocalProxy;

    public ArquivoOneDriveReal(String nomeArquivo, OneDriveAPI oneDriveAPI) {
        this.nomeArquivo = nomeArquivo;
        this.oneDriveAPI = oneDriveAPI;
        arquivoLocalProxy = new ArquivoLocalProxy(nomeArquivo);
    }

    @Override
    public String lerConteudo() throws Exception {
        return oneDriveAPI.leConteudoDoArquivo(nomeArquivo);
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws Exception {
        arquivoLocalProxy.gravaConteudo(conteudo);
        return oneDriveAPI.gravaArquivo(new File(nomeArquivo));
    }

}
