/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author mfernandes
 */
public class ArquivoLocalReal implements IArquivoProxy {

    private final String nomeArquivo;

    public ArquivoLocalReal(String nome) {
        this.nomeArquivo = nome;
    }

    @Override
    public String lerConteudo() throws Exception {
        return getConteudoDeArquivo(nomeArquivo);
    }

    @Override
    public boolean gravaConteudo(String conteudo) throws Exception {

        File arquivo = new java.io.File(nomeArquivo);

        arquivo.createNewFile();

        arquivo.setWritable(true);

        FileWriter fw = new FileWriter(arquivo);

        fw.append(conteudo);

        fw.flush();

        fw.close();

        return true;

    }

    public String getConteudoDeArquivo(String nome)
            throws FileNotFoundException, IOException {
        FileReader arquivo = new FileReader(nome);
        BufferedReader lerArq = new BufferedReader(arquivo);
        String texto = "";
        String linha = lerArq.readLine();

        while (linha != null) {
            texto += linha;
            linha = lerArq.readLine();
        }
        arquivo.close();

        return texto;
    }

}
