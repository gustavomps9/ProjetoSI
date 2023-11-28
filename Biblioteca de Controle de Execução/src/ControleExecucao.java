import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ControleExecucao {
    private final Aplicacao aplicacao;
    private final Sistema sistema;
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

    /*
        - o pedido de licensa é encriptado com uma chave pública e é enviado uma chave privada para o autor para desencriptar o pedido
        - a assinatura serve para comprovar a integridade e é assinado com uma chave privada e verificado com uma chave pública
        - as chaves simétricas são criptogradas com chaves assiméticas (híbridas)
    */

    /*
        Biblioteca
        - os dados são assinados com a chave privada do cartão de cidadão e codificados em base64 = ficheiro "dadosAssinados"
        - o certificado do cartão de cidadão que será utilizado para verificar a assinatura no lado do autor é colocado em outro ficheiro = ficheiro "certificado"
        - os ficheiros em causa são colocados numa pasta zipada, essa pasta é cifrada SIMÉTRICAMENTE e a sua chave é transmitido ao autor através
        do mecanismo de chaves híbridas = pasta "PedidoDeLicensa"
     */

    /*
        Gestor
        - o gestor recebe a pasta zipada, e decifra com a chave simétrica que foi transmistida com mecanismos de cifra híbrida
        - depois de unzipado e decifrado, vai usar o certificado para validar a assinatura do utilizador
        - após isso tudo validado, na emissão da licensa, o autor vai assinar a licensa e com mecanismos de cifras híbridas vai
        transmitir a chave pública ao utilizador para que a biblioteca faça a validação da licensa.
     */

    private boolean validaLicensa(File file) {
        //código a implementar sincronizado com a emissão da licensa
        return true;
    }

    public boolean startRegistration() {
        Utilizador utilizador = new Utilizador();
        String dados = utilizador.toString() + sistema.toString() + aplicacao.toString();

        try {
            Certificate cert = getKeyStore().getCertificate("CITIZEN SIGNATURE CERTIFICATE");
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("certificado"))) {
                objectOutputStream.writeObject(cert);
            } catch (IOException e) {throw new RuntimeException(e);}
        } catch (KeyStoreException e) {throw new RuntimeException(e);}


        try (FileOutputStream fos = new FileOutputStream("assinatura")) {
            fos.write(assinaturaDados(dados));
        } catch (IOException e) {throw new RuntimeException(e);}

        return true;
    }

    private byte[] assinaturaDados(String dados) {
        try{
            PrivateKey privateKey = (PrivateKey) getKeyStore().getKey("CITIZEN SIGNATURE CERTIFICATE", null);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(dados.getBytes());
            return signature.sign();
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyStore getKeyStore(){
        try {
            Provider[] provs = Security.getProviders();
            Provider provider = null;
            for (Provider prov : provs) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;}}
            KeyStore keyStore = KeyStore.getInstance("PKCS11", Objects.requireNonNull(provider));
            keyStore.load(null, null);
            return keyStore;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void descarregaPasta(ObjectOutputStream objectOutputStream, FileOutputStream fileOutputStream) throws IOException {
        // Cria um novo ZipOutputStream
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        // Adiciona o ObjectOutputStream ao ZipOutputStream
        ZipEntry zipEntry = new ZipEntry("objectOutputStream.dat");
        zipOutputStream.putNextEntry(zipEntry);

        // Escreve o ObjectOutputStream para o ZipOutputStream
        //objectOutputStream.writeTo(zipOutputStream);

        // Fecha o ZipOutputStream
        zipOutputStream.close();
    }
}
