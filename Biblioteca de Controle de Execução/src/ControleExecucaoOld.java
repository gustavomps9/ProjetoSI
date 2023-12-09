import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ControleExecucaoOld {
    private Aplicacao aplicacao;
    private Sistema sistema;
    private File licensa;

    public ControleExecucaoOld(String nomeDaApp, String versao) {
        this.aplicacao = new Aplicacao(nomeDaApp, versao);
        this.sistema = new Sistema();
        isRegistered();
    }

    public boolean isRegistered() {
        String userDirectory = System.getProperty("user.dir");
        String fullPath = userDirectory + File.separator + "licensa";
        File folder = new File(fullPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (validacaoLicensa(file)) {
                        this.licensa = file;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean startRegistration() throws Exception {
        Utilizador utilizador = new Utilizador();
        String dados = aplicacao.toString() + "\n" + utilizador.toString() + "\n" + sistema.toString();

        Provider provider = null;
        for (Provider prov : Security.getProviders()) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;break;}}

        KeyStore ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
        ks.load(null, null);

        SecretKey chaveSimetrica = geraracaoChaveSimetrica();
        KeyPair parChaves = geraracaoParChaves();

        byte[] dadosAssinados = assinaturaDados(ks, dados);
        X509Certificate certificate = (X509Certificate) ks.getCertificate("CITIZEN SIGNATURE CERTIFICATE");

        byte[] dadosCifrados = cifragemDados(dadosAssinados, chaveSimetrica);
        System.out.println("Dados assinados: " + Arrays.toString(dadosCifrados));
        byte[] chaveSimetricaCifrada = cifragemChaveSimetrica(chaveSimetrica, parChaves.getPublic());

        String zipFilePath = "PedidoLicensa.zip";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            adicaoArquivoAoZip(zipOutputStream, "InfoLicensa", dadosCifrados);
            adicaoArquivoAoZip(zipOutputStream, "Certificado", certificate.getPublicKey().getEncoded());
            adicaoArquivoAoZip(zipOutputStream, "chaveSimetricaCifrada", chaveSimetricaCifrada);
        }
        return true;
    }

    public void showLicenseInfo(){
        //código a implementar para mostrar as informações da licensa
    }

    private boolean validacaoLicensa(File file) {
        //código a implementar sincronizado com a emissão da licensa no gestor
        return true;
    }

    private SecretKey geraracaoChaveSimetrica() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private KeyPair geraracaoParChaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
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

    private byte[] cifragemDados(byte[] dados, SecretKey chaveSimetrica) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chaveSimetrica);
            return cipher.doFinal(dados);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {throw new RuntimeException(e);}
    }

    private byte[] cifragemChaveSimetrica(SecretKey chaveSimetrica, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(chaveSimetrica.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private void adicaoArquivoAoZip(ZipOutputStream zipOutputStream, String nomeArquivo, byte[] conteudo) throws Exception {
        ZipEntry zipEntry = new ZipEntry(nomeArquivo);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(conteudo);
        zipOutputStream.closeEntry();
    }
}
