import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.File;
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

        String dadosDocumento = utilizador.toString() + sistema.toString() + this.app.toString();

        byte[] assinatura = assinaturaCartaoCidadao(dadosDocumento);

        byte[] dadosCifrados = cifraDocumento(assinatura);

        String dadosCodificados = Base64.getEncoder().encodeToString(dadosCifrados);

        return descarregaPedido(dadosCodificados);
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

        //automatizar obtenção do provider...

        KeyStore ks = KeyStore.getInstance("PKCS11", provs[13]);
        ks.load(null, null);

        PrivateKey privateKey = (PrivateKey) ks.getKey("CITIZEN SIGNATURE CERTIFICATE", null);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(dadosDocumento.getBytes());
        return signature.sign();
    }

    private byte[] cifraDocumento(byte[] dados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, generateSessionKey());
        return cipher.doFinal(dados);
    }

    private SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private Boolean descarregaPedido(String dadosCodificados){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Como");

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile())) {
                fos.write(dadosCodificados.getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(null, "Download concluído com sucesso!");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro durante o download: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("Download cancelado pelo usuário.");
            return false;
        }
    }

}
