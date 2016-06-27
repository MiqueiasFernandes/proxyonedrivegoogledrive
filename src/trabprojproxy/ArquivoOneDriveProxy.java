/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import trabprojproxy.OneDrive.OneDriveAPI;
import trabprojproxy.OneDrive.model.Credenciais;

public class ArquivoOneDriveProxy implements IArquivoProxy {

    private final String nomeArquivo;
    private final OneDriveAPI oneDriveAPI;
    private final ArquivoOneDriveReal arquivoOneDriveReal;
    private static final String CLIENT_ID = "59da230c-3b53-41d3-98be-86e6a5ad93f9";
    private static final String CLIENT_SECRET = "c1it7euXKAWdy6haZHHyxto";

    public ArquivoOneDriveProxy(String nomeArquivo) throws Exception {
        this.nomeArquivo = nomeArquivo;
        oneDriveAPI = new OneDriveAPI(
                new Credenciais(CLIENT_ID, CLIENT_SECRET));
        arquivoOneDriveReal = new ArquivoOneDriveReal(nomeArquivo, oneDriveAPI);
    }

    @Override
    public String lerConteudo() throws Exception {
        return arquivoOneDriveReal.lerConteudo();
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws Exception {
        return arquivoOneDriveReal.gravaConteudo(conteudo);
    }

}
