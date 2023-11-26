import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.security.*;

public class ControleExecucao {
    public Aplicacao app;
    public File file;

    public ControleExecucao(String nomeDaApp, String versao) throws Exception {
        this.app = new Aplicacao(nomeDaApp, versao);
        isRegistered();
    }

    private boolean isRegistered() throws Exception {
        Scanner scanner1 = new Scanner(System.in);
        Scanner scanner2 = new Scanner(System.in);

        if(procuraLicensa()){
            do{
                System.out.println("Deseja ver as informações da licensa existente ? [S/n]");

                if (Objects.equals(scanner1.next(), "S")) {
                    showLicenseInfo();
                    return true;
                } else if (Objects.equals(scanner1.next(), "n")) {return true;}

            } while(!Objects.equals(scanner1.next(), "S") || !Objects.equals(scanner1.next(), "n"));
        }else{
            do{
                System.out.println("Deseja pedir um novo registro ? [S/n]");

                if (Objects.equals(scanner2.next(), "S")) {
                    startRegistration();
                } else if (Objects.equals(scanner2.next(), "n")) {return false;}

            } while(!Objects.equals(scanner2.next(), "S") || !Objects.equals(scanner2.next(), "n"));
        }
        return false;
    }

    public boolean startRegistration() throws Exception {
        Sistema sistema = new Sistema();
        Utilizador utilizador = new Utilizador();

        String conteudo = utilizador.toString() + sistema.toString() + this.app.toString();

        String dadosDocumento = Base64.getEncoder().encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));

        byte[] assinatura = assinaturaCartaoCidadao(dadosDocumento);

        byte[] dadosCifrados = cifraDocumento(dadosDocumento);

        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("pedido_de_registro"))) {
            outputStream.writeObject(dadosCifrados);
            outputStream.writeObject(assinatura);
            return descarregaPedido(outputStream);
        }
    }

    public void showLicenseInfo() {
        System.out.println(this.file.toString());
    }

    private Boolean procuraLicensa() {
        File diretorioAtual = new File("../Aplicação/licensa");

        //desassociar busca a aplicação em causa...

        if (diretorioAtual.exists() && diretorioAtual.isDirectory()) {
            File[] arquivos = diretorioAtual.listFiles();

            if (arquivos != null && arquivos.length > 0) {
                for (File arquivo : arquivos) {
                    if (arquivo.isFile()) {
                        this.file = arquivo;
                        return true;
                    }
                }
            }else {return false;}
        } else{return false;}

        return false;
    }

    private byte[] assinaturaCartaoCidadao(String dadosDocumento) throws Exception {
        Provider[] provs = Security.getProviders();
        Provider provider = null;

        for (Provider prov : provs) {
            if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {
                provider = prov;
            }
        }

        KeyStore ks = KeyStore.getInstance("PKCS11", provider);
        ks.load(null, null);

        PrivateKey privateKey = (PrivateKey) ks.getKey("CITIZEN SIGNATURE CERTIFICATE", null);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(dadosDocumento.getBytes());
        return signature.sign();
    }

    private byte[] cifraDocumento(String dados) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey());
        return cipher.doFinal(dados.getBytes());
    }

    private Boolean descarregaPedido(ObjectOutputStream dadosCodificados){
        return true;
    }
}
