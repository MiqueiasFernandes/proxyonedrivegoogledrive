/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import com.google.api.services.samples.drive.cmdline.GoogleDriveAPI;
import java.io.File;

/**
 *
 * @author mfernandes
 */
public class ArquivoGoogleDriveReal implements IArquivoProxy {

    private final GoogleDriveAPI googleDriveAPI;
    private final String nomeArquivo;

    public ArquivoGoogleDriveReal(GoogleDriveAPI googleDriveAPI, String nomeArquivo) {
        this.googleDriveAPI = googleDriveAPI;
        this.nomeArquivo = nomeArquivo;
    }

    @Override
    public String lerConteudo() throws Exception {
        File file = googleDriveAPI.downloadFile("./", nomeArquivo, "title");
        ArquivoLocalProxy localProxy = new ArquivoLocalProxy(file.getAbsolutePath());
        return localProxy.lerConteudo();
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws Exception {

        ArquivoLocalProxy localProxy = new ArquivoLocalProxy(nomeArquivo);
        localProxy.gravaConteudo(conteudo);

        return googleDriveAPI.uploadFile(
                new File(nomeArquivo), "text/plain", "title") != null;

    }

}
