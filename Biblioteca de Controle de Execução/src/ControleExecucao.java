import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

public class ControleExecucao {
    public Aplicacao app;

    public ControleExecucao(String nome, String versao) throws Exception {
        this.app = new Aplicacao(nome, versao);
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
                    break;
                } else if (Objects.equals(scanner1.next(), "n")) {
                    break;
                }
            } while(!Objects.equals(scanner1.next(), "S") || !Objects.equals(scanner1.next(), "n"));
        }else{
            do{
                System.out.println("Deseja pedir um novo registro ? [S/n]");

                if (Objects.equals(scanner2.next(), "S")) {
                    startRegistration();
                } else if (Objects.equals(scanner2.next(), "n")) {
                    return false;
                }
            } while(!Objects.equals(scanner2.next(), "S") || !Objects.equals(scanner2.next(), "n"));
        }
        return false;
    }

    public boolean startRegistration() throws Exception {
        Utilizador utilizador = new Utilizador();
        Sistema sistema = new Sistema();
        String dadosDocumento = utilizador.toString() + sistema.toString() + this.app.toString();

        byte[] assinatura = assinaturaCartaoCidadao(dadosDocumento);

        byte[] dadosCifrados = cifraDocumento(assinatura);

        String dadosCodificados = Base64.getEncoder().encodeToString(dadosCifrados);

        try (FileOutputStream fos = new FileOutputStream("PedidoDeRegisto")) {
            fos.write(dadosCodificados.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void showLicenseInfo() {
    }

    private Boolean procuraLicensa() {
        return true;
    }

    private byte[] assinaturaCartaoCidadao(String dadosDocumento) throws Exception {
        Provider[] provs = Security.getProviders();

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
}
