import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

public class EmitePedido {
    public String dados;
    public EmitePedido(String dadosPedido) {
        this.dados = dadosPedido;

        try {
            KeyPair keyPair = geracaoParChave();
            SecretKey secretKey = geracaoChaveSimetrica();
            byte[] dadosCifrados = cifragemDados(secretKey, dadosPedido.getBytes());
            byte[] assinatura = assinaDados(dadosCifrados, keyPair.getPrivate());
            KeyPair keyPair1 = geracaoParChave();
            byte[] chaveCifrada = cifragemAssimetrica(secretKey.getEncoded(), keyPair1.getPublic());
            emiteFicheiros(dadosCifrados, assinatura, keyPair.getPublic().getEncoded(), chaveCifrada, keyPair1.getPrivate().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void emiteFicheiros(byte[] dadosCifrados, byte[] assinatura, byte[] certificado, byte[] chaveCifrada, byte[] chavePrivada) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("pedidoLicenca")) {fos.write(dadosCifrados);}
        try (FileOutputStream fos = new FileOutputStream("assinatura")) {fos.write(assinatura);}
        try (FileOutputStream fos = new FileOutputStream("certificado")) {fos.write(certificado);}
        try (FileOutputStream fos = new FileOutputStream("chaveSimetrica")) {fos.write(chaveCifrada);}
        try (FileOutputStream fos = new FileOutputStream("chaveAssimetrica")) {fos.write(chavePrivada);}

    }

    private KeyPair geracaoParChave() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private SecretKey geracaoChaveSimetrica() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private byte[] cifragemDados(SecretKey secretKey, byte[] bytes) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(bytes);
    }

    private byte[] assinaDados(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    private byte[] cifragemAssimetrica(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }
}

/*
private KeyStore carregaProvider() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        Provider provider = null;
        for (Provider prov : Security.getProviders()) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;break;}}

        KeyStore ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
        ks.load(null, null);

        return ks;
    }

    public byte[] assinaturaDados(){
        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign((PrivateKey) carregaProvider().getKey("CITIZEN SIGNATURE CERTIFICATE", null));
            signature.update(this.dadosPedido.getBytes());
            return signature.sign();
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | SignatureException | InvalidKeyException | CertificateException | IOException e) {throw new RuntimeException(e);}
    }

    public X509Certificate obterCertificado() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        return (X509Certificate) carregaProvider().getCertificate("CITIZEN SIGNATURE CERTIFICATE");
    }
 */
