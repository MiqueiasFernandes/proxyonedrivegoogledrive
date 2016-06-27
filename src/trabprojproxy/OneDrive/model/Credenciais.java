/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy.OneDrive.model;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import trabprojproxy.OneDrive.OSBrowserChoose;

/**
 *
 * @author mfernandes
 */
public final class Credenciais {

    private String clientId;
    private String clientSecret;
    private String authorizationCode;
    private OAuth20Token oAuth20Token;

    public Credenciais(String clientId, String clientSecret) throws IOException, URISyntaxException, Exception {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        authorizationCode = getAuthorizationCode();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthorizationCode() throws IOException, URISyntaxException, Exception {

        if (authorizationCode == null) {
            ///gerar auteroziation code 
            String url = "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId + "&scope=onedrive.readwrite+wl.offline_access+wl.photos+wl.skydrive+wl.basic+wl.skydrive_update&response_type=code&redirect_uri=https://login.live.com/oauth20_desktop.srf";

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {

                Process p = Runtime.getRuntime().exec(OSBrowserChoose.getBrowserCommand() + " " + url);
            }

            System.out.println("Vá ao Browser que foi aberto; Após aceitar, copie o URL do navegador e cole abaixo:");

///https://login.live.com/oauth20_desktop.srf?code=M2a68dff6-73e4-8227-d667-b69feff76663&lc=1046
            String past = new Scanner(System.in).nextLine();
            try {
                authorizationCode = past.split("=")[1];

                if (authorizationCode.contains("&")) {
                    authorizationCode = authorizationCode.split("&")[0];
                }
                System.out.println("AUTHORIZATION_CODE -> " + authorizationCode);
            } catch (Exception ex) {
                System.err.println("erro: " + ex);
                codigoIncorretoAlert();
            }
        }
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public OAuth20Token getoAuth20Token() {
        return oAuth20Token;
    }

    public void setoAuth20Token(OAuth20Token oAuth20Token) {
        this.oAuth20Token = oAuth20Token;
    }

    public void codigoIncorretoAlert() {
        System.err.println("Você digitou um codigo incorreto!");
        System.err.println("um codigo correto é do tipo: ");
        System.err.println("https://login.live.com/oauth20_desktop.srf?code=Mf50b47a2-d511-5d5d-8e45-4c04d3fabcb5&lc=1046");
        System.err.println("tente novamente, aguarde até o navegador carregar a sua requisição....");
    }

    @Override
    public String toString() {
        String credenciais = "clientId : " + clientId
                + "\nclientSecret : " + clientSecret
                + "\nauthorizationCode : " + authorizationCode
                + "\noAuth20Token : " + oAuth20Token + "\n";
        return credenciais;
    }
}
