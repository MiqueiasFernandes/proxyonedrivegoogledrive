/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import com.google.api.services.samples.drive.cmdline.GoogleDriveAPI;
import java.io.IOException;

public class ArquivoGoogleDriveProxy implements IArquivoProxy {

    private final GoogleDriveAPI googleDriveAPI;
    private final String nomeArquivo;
    private final ArquivoGoogleDriveReal arquivoGoogleDriveReal;

    public ArquivoGoogleDriveProxy(String nomeArquivo) throws IOException, Exception {
        this.nomeArquivo = nomeArquivo;
        this.googleDriveAPI = new GoogleDriveAPI();
        arquivoGoogleDriveReal = new ArquivoGoogleDriveReal(googleDriveAPI, this.nomeArquivo);
    }

    @Override
    public String lerConteudo() throws IOException, Exception {
        return arquivoGoogleDriveReal.lerConteudo();
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws IOException, Exception {
        return arquivoGoogleDriveReal.gravaConteudo(conteudo);
    }

}
