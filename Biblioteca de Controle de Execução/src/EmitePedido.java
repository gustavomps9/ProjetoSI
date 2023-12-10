import javax.crypto.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            System.out.println("Erro: " + e);
        }
    }

    private void emiteFicheiros(byte[] dadosCifrados, byte[] assinatura, byte[] certificado, byte[] chaveCifrada, byte[] chavePrivada){
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream("PedidoLicensa.zip"))) {
            adicionarArquivoAoZip(zipOut, "pedidoLicenca", dadosCifrados);
            adicionarArquivoAoZip(zipOut, "assinatura", assinatura);
            adicionarArquivoAoZip(zipOut, "certificado", certificado);
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

    private byte[] assinaDados(byte[] data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
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
