import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ControleExecucao {
    private Aplicacao aplicacao;
    private Sistema sistema;
    private  Utilizador utilizador;
    private File licensa;

    public ControleExecucao(String nomeDaApp, String versao) {
        this.aplicacao = new Aplicacao(nomeDaApp, versao);
        this.sistema = new Sistema();
    }

    public ControleExecucao() {
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

    public boolean startRegistration() throws Exception {
        this.utilizador = new Utilizador();
        String dados = aplicacao.toString() + "\n" + utilizador.toString() + "\n" + sistema.toString();

        SecretKey chaveSimetrica = gerarChaveSimetrica();
        KeyPair keyPairDestinatario = gerarParChaves();

        Provider provider = null;
        for (Provider prov : Security.getProviders()) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;break;}}

        KeyStore ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
        ks.load(null, null);

        byte[] dadosAssinados = assinaturaDados(ks, dados);
        X509Certificate certificate = (X509Certificate) ks.getCertificate("CITIZEN SIGNATURE CERTIFICATE");

        byte[] dadosCifrados = cifrarDados(dadosAssinados, chaveSimetrica);
        byte[] chaveSimetricaCifrada = cifrarChaveSimetrica(chaveSimetrica, keyPairDestinatario.getPublic());

        String zipFilePath = "PedidoLicensa.zip";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            adicionarArquivoAoZip(zipOutputStream, "InfoLicensa", dadosCifrados);
            adicionarArquivoAoZip(zipOutputStream, "Certificado", certificate.getPublicKey().getEncoded());
            adicionarArquivoAoZip(zipOutputStream, "chaveSimetricaCifrada", chaveSimetricaCifrada);
        }

        return true;
    }

    private void adicionarArquivoAoZip(ZipOutputStream zipOutputStream, String nomeArquivo, byte[] conteudo) throws Exception {
        ZipEntry zipEntry = new ZipEntry(nomeArquivo);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(conteudo);
        zipOutputStream.closeEntry();
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

    private SecretKey gerarChaveSimetrica() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private KeyPair gerarParChaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Tamanho da chave, ajuste conforme necessário
        return keyPairGenerator.generateKeyPair();
    }

    private byte[] cifrarDados(byte[] dados, SecretKey chaveSimetrica) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chaveSimetrica);
            return cipher.doFinal(dados);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {throw new RuntimeException(e);}
    }

    private byte[] cifrarChaveSimetrica(SecretKey chaveSimetrica, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(chaveSimetrica.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
