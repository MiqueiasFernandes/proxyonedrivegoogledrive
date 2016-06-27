/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import java.util.Scanner;

/**
 *
 * @author mfernandes
 */
public class TrabProjProxy {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String nome;
        String conteudo;
        Scanner scanner = new Scanner(System.in);

        System.out.println("DIGITE ALGUM NOME VÁLIDO PARA O ARQUIVO: (não use espaços nem extensão)");
        nome = scanner.nextLine();
        System.out.println("DIGITE ALGUM CONTEUDO PARA O ARQUIVO: (apenas uma linha)");
        conteudo = scanner.nextLine();
        nome += ".txt";
        System.out.println("O ARQUIVO " + nome + " SERÁ SALVO NO ONEDRIVE, GOOGLE DRIVE E LOCAL...");

        IArquivoProxy arquivo;

        try {

            ////Processo Google Drive
            arquivo = new ArquivoGoogleDriveProxy(nome);
            System.out.println("***********************SALVANDO ARQUIVO GOOGLE DRIVE***********************");
            arquivo.gravaConteudo(conteudo);
            System.out.println("***********************LENDO ARQUIVO GOOGLE DRIVE**************************");
            System.out.println("LIDO: " + arquivo.lerConteudo());
            System.out.println("***************************************************************************");

            ////Processo One Drive: Uma pagina será aberta no navegador para gerar o token
            arquivo = new ArquivoOneDriveProxy(nome);
            System.out.println("**************************SALVANDO ARQUIVO ONE DRIVE************************");
            arquivo.gravaConteudo(conteudo);
            System.out.println("**************************LENDO ARQUIVO ONE DRIVE***************************");
            System.out.println("LIDO: " + arquivo.lerConteudo());
            System.out.println("***************************************************************************");

            ////Processo Arquivo local: grava e lê um arquivo local
            arquivo = new ArquivoLocalProxy(nome);
            System.out.println("****************************SALVANDO ARQUIVO LOCAL**************************");
            arquivo.gravaConteudo(conteudo);
            System.out.println("****************************LENDO ARQUIVO LOCAL*****************************");
            System.out.println("LIDO: " + arquivo.lerConteudo());
            System.out.println("***************************************************************************");

        } catch (Exception ex) {
            System.err.println("erro: " + ex);
        }

    }

}
