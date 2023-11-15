import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

public class ControleExecucao {
    public Aplicacao app;

    public ControleExecucao(String nome, String versao) {
        this.app = new Aplicacao(nome, versao);
    }

    /**
     * É invocado no início da execução da aplicação ou sempre que necessário.
     * Valida a correta execução da aplicação.
     * @return
     */
    public boolean isRegistered(){
        Boolean estaValidado;

        /*
        se a aplicação não estiver validada return false;
        se a aplicação estiver validada return true;
        */

        return true;
    }

    /**
     * Apresenta uma interface que indica que a aplicação não se encontra registada e possibilita a iniciação do processo de registo de uma nova licença
     * @return
     */
    public boolean startRegistration() throws Exception {
        Utilizador utilizador = new Utilizador();
        Sistema sistema = new Sistema();

        String dadosDocumento = utilizador.toString() + sistema.toString() + this.app.toString();

        // assinatura com chave do cartão de cidadão do documento
        String assinatura = assinaturaCartaoCidadao(dadosDocumento);

        // cifra do documento
        String dadosCifrados = cifraDocumento(dadosDocumento + assinatura);

        // criação do ficheiro "pedido de registo" codificado para Base 64
        String dadosCodificados = Base64.getEncoder().encodeToString(dadosCifrados.getBytes(StandardCharsets.UTF_8));

        // armazenamento no ficheiro
        try (FileOutputStream fos = new FileOutputStream("PedidoDeRegisto")) {
            fos.write(dadosCodificados.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {e.printStackTrace();}

        // deve mostrar no ecrã

        return true;
    }

    /**
     * Apresenta os dados da licença atual
     */
    public void showLicenseInfo(){}

    private String assinaturaCartaoCidadao(String dadosDocumento) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(generateKeyPair().getPrivate());
        signature.update(dadosDocumento.getBytes(StandardCharsets.UTF_8));
        byte[] assinatura = signature.sign();
        return Base64.getEncoder().encodeToString(assinatura);
    }

    private String cifraDocumento(String dados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, generateSessionKey());
        byte[] dadosCifrados = cipher.doFinal(dados.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(dadosCifrados);
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Você pode ajustar o tamanho da chave conforme necessário
        return keyPairGenerator.generateKeyPair();
    }

    private SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Você pode ajustar o tamanho da chave conforme necessário
        return keyGenerator.generateKey();
    }
}
