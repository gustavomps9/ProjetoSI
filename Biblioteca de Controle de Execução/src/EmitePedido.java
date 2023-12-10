import javax.crypto.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EmitePedido {
    public String dados;

    public EmitePedido(String dadosPedido) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        this.dados = dadosPedido;

        try {
            SecretKey secretKey = geracaoChaveSimetrica();
            X509Certificate certificate = (X509Certificate) getKeyStore().getCertificate("CITIZEN SIGNATURE CERTIFICATE");
            byte[] dadosCifrados = cifragemDados(secretKey, dadosPedido.getBytes());
            byte[] assinatura = assinaDados(dadosCifrados, getKeyStore());
            KeyPair keyPair = geracaoParChave();
            byte[] chaveCifrada = cifragemAssimetrica(secretKey.getEncoded(), keyPair.getPublic());
            emiteFicheiros(dadosCifrados, assinatura, certificate, chaveCifrada, keyPair.getPrivate().getEncoded());
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
    }

    private KeyStore getKeyStore(){
        Provider provider = null;
        for (Provider prov : Security.getProviders()) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;break;}}

        try {
            KeyStore ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
            ks.load(null, null);
            return ks;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void emiteFicheiros(byte[] dadosCifrados, byte[] assinatura, X509Certificate certificado, byte[] chaveCifrada, byte[] chavePrivada){
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream("PedidoLicensa.zip"))) {
            adicionarArquivoAoZip(zipOut, "pedidoLicenca", dadosCifrados);
            adicionarArquivoAoZip(zipOut, "assinatura", assinatura);
            adicionarArquivoAoZip(zipOut, "certificado", certificado.getPublicKey().getEncoded());
            adicionarArquivoAoZip(zipOut, "chaveSimetrica", chaveCifrada);
            try (FileOutputStream fos = new FileOutputStream("chaveAssimetrica")) {fos.write(chavePrivada);}
        } catch (IOException e) {
            System.out.println("Erro: " + e);
        }
    }

    private void adicionarArquivoAoZip(ZipOutputStream zipOut, String nomeArquivo, byte[] dados){
        try {
            ZipEntry entrada = new ZipEntry(nomeArquivo);
            zipOut.putNextEntry(entrada);
            zipOut.write(dados);
            zipOut.closeEntry();
        } catch (IOException e) {
            System.out.println("Erro: " + e);
        }
    }

    private KeyPair geracaoParChave(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private SecretKey geracaoChaveSimetrica(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private byte[] cifragemDados(SecretKey secretKey, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private byte[] assinaDados(byte[] data, KeyStore ks) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign((PrivateKey) ks.getKey("CITIZEN SIGNATURE CERTIFICATE", null));
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException | KeyStoreException e) {throw new RuntimeException(e);}
    }

    private byte[] cifragemAssimetrica(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }
}
