import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ControleExecucao {
    private final Aplicacao aplicacao;
    private final Sistema sistema;
    private  Utilizador utilizador;
    private File licensa;

    public ControleExecucao(String nomeDaApp, String versao) {
        this.aplicacao = new Aplicacao(nomeDaApp, versao);
        this.sistema = new Sistema();
    }

    public boolean isRegistered() {
        String userDirectory = System.getProperty("user.dir");
        String fullPath = userDirectory + File.separator + "licensa";
        File folder = new File(fullPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (validaLicensa(file)) {
                        this.licensa = file;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean validaLicensa(File file) {
        //código a implementar sincronizado com a emissão da licensa no gestor
        return true;
    }

    public boolean startRegistration() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        this.utilizador = new Utilizador();
        String dados = aplicacao.toString() + "\n" + utilizador.toString() + "\n" + sistema.toString();

        Provider provider = null;
        for (Provider prov : Security.getProviders()) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;break;}}

        KeyStore ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
        ks.load(null, null);

        byte[] dadosAssinados = assinaturaDados(ks, dados);
        X509Certificate certificate = (X509Certificate) ks.getCertificate("CITIZEN SIGNATURE CERTIFICATE");

        try {
            zipDataAndCertificate(dadosAssinados, certificate.getPublicKey());

            SecretKey secretKey = generateSymmetricKey();
            cipherZipFolder("PedidoLicensa.zip", "PedidoLicensaCifrado.zip", secretKey);
            cipherSymmetricKey(secretKey, certificate.getPublicKey(), "chave_simetrica_cifrada");

        } catch (Exception e) {throw new RuntimeException(e);}

        return true;
    }

    private byte[] assinaturaDados(KeyStore ks, String dados){
        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign((PrivateKey) ks.getKey("CITIZEN SIGNATURE CERTIFICATE", null));
            signature.update(dados.getBytes());
            return signature.sign();
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void zipDataAndCertificate(byte[] dadosAssinados, PublicKey cert) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream("PedidoLicensa.zip"))) {
            ZipEntry dataEntry = new ZipEntry("InfoLicensa.txt");
            zipStream.putNextEntry(dataEntry);
            zipStream.write(dadosAssinados);
            zipStream.closeEntry();

            ZipEntry certEntry = new ZipEntry("certificado.cer");
            zipStream.putNextEntry(certEntry);
            zipStream.write(cert.getEncoded());
            zipStream.closeEntry();
        }
    }

    private static SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    private static void cipherZipFolder(String inputFileName, String outputFileName, SecretKey secretKey) throws Exception {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(inputFileName));
             ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFileName))) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Crie um objeto Cipher para criptografia simétrica
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Crie a entrada criptografada
            zipOutputStream.putNextEntry(new ZipEntry("PedidoLicensaCifrado.zip"));

            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                // Atualize os dados criptografados
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                zipOutputStream.write(encryptedBytes);
            }

            // Finalize a operação de criptografia
            byte[] finalBytes = cipher.doFinal();
            zipOutputStream.write(finalBytes);

            zipOutputStream.closeEntry();
        }

        File zipFile = new File(inputFileName);
        if (zipFile.exists()) {zipFile.delete();}
    }

    private static void cipherSymmetricKey(SecretKey secretKey, PublicKey publicKey, String outputFileName) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = cipher.doFinal(secretKey.getEncoded());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFileName))) {
            oos.writeObject(encryptedKey);
        }
    }
}
