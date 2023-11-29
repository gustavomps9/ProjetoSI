import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.zip.ZipEntry;
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

    public boolean isRegistred() {
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

    public boolean startRegistration() throws KeyStoreException {
        this.utilizador = new Utilizador();
        String dados = aplicacao.toString() + "\n" + utilizador.toString() + "\n" + sistema.toString();

        Provider provider = null;
        for (Provider prov : Security.getProviders()) {
            if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {
                provider = prov;
                break;
            }
        }

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
            ks.load(null, null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        X509Certificate certificate = (X509Certificate) ks.getCertificate("CITIZEN SIGNATURE CERTIFICATE");

        createZipFile(assinaDados(ks, dados), certificate.getPublicKey().getEncoded());

        return true;
    }

    private byte[] assinaDados(KeyStore ks, String dados){
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

    private static void createZipFile(byte[] infoLicensa, byte[] certificado) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("PedidoLicensa"))) {
            ZipEntry zipEntry1 = new ZipEntry("InfoLicensa");
            zos.putNextEntry(zipEntry1);
            zos.write(infoLicensa);

            ZipEntry zipEntry2 = new ZipEntry("Certificado");
            zos.putNextEntry(zipEntry2);
            zos.write(certificado);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
