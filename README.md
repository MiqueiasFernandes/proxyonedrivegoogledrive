# proxyonedrivegoogledrive
Implementação do design pattern Proxy utilizando a API do googleDrive e do Onedrive (nao oficial)

Este software recebe um nome de arquivo, exemplo: arquivodeteste (sem espaço e extensão)
e uma linha contendo um conteudo, exemplo: 
linha de teste do arquivo

então salva o arquivo arquivodeteste no googleDrive, para isso, ele tenta um login no navegador chrome, 
caso o usuario já esteja logado com sua conta do google ele continua, caso contrario abre uma aba para o usuario efetuar login

apos o login ele salva o arquivo arquivodeteste e assim que terminar e apresentar o feedback ele lê o conteudo de o
arquivo arquivodeteste baixando o arquivo do googledrive.

concluido a parte do googleDrive ele passa o escopo para o googleDrive
<ol>
<li>ele pede ao usuario para ir a janela que ele abriu (provavelmente no navegador principal) pode demorar uns 2 minutos p abrir</li>
<li>o usuario faz login com sua conta do windows live e clica em aceitar</li>
<li>o usuario copia o link <b>completo</b> que aparece na janela apos a mesma terminar de procesar a solicitação do item de cima,
o codigo que o usuario irá copiar da janela deve ser semelhante a este:<br>
https://login.live.com/oauth20_desktop.srf?<b>code=M44bfa5c7-a34b-0a7f-83f6-2b04280a98fc</b>&lc=1046<br></li>
<li>o usuario cola este codigo na linha de comando do software e clica em enter</li>
</ol>
com sucesso nos passos anteriores o software autentica-se e salva o arquivo arquivodeteste no onedrive da conta logada
depois do feedback do passo anterior o arquivo é baixado e lido

para finalizar ele salva e lê o arquivo em armazenamento local.


contatomiqueiasfernandes@hotmail.com<br>
www.miqueiasfernandes.com.br



