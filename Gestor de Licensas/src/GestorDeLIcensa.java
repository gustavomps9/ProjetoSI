import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GestorDeLIcensa {
    private String infoLicensa;

    public GestorDeLIcensa(PrivateKey chavePrivada1, PrivateKey chavePrivada2) throws Exception {
        if (processarPedido(chavePrivada1)){
            System.out.println("Pedido processado com sucesso");
            emitirLicensa(chavePrivada2);
        }else{System.out.println("Falha ao processar pedido");}
    }

    private void emitirLicensa(PrivateKey privateKey) throws Exception {
        registroDistribuicao();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Indica a data de validade da licensa: ");
        String data = scanner.nextLine();

        //EmissorLicensa emissorLicensa = new EmissorLicensa(infoLicensa, data, privateKey);
    }

    private void registroDistribuicao(){
        File pasta = new File("Apps Distribuídas");
        File[] arquivos = pasta.listFiles();

        String nomeDoArquivo = "App " + ((arquivos != null) ? arquivos.length + 1 : 1);

        File arquivo = new File("Apps Distribuídas" + File.separator + nomeDoArquivo);

        try {
            if (!arquivo.getParentFile().exists()) {arquivo.getParentFile().mkdirs();}
            arquivo.createNewFile();

            FileWriter escritor = new FileWriter(arquivo);
            escritor.write(this.infoLicensa);
            escritor.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private boolean processarPedido(PrivateKey privateKey) throws Exception {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream("PedidoLicensa.zip"))) {
            byte[] dadosCifrados = extrairArquivoDoZip(zipInputStream, "InfoLicensa");
            byte[] chaveSimetricaCifrada = extrairArquivoDoZip(zipInputStream, "chaveSimetricaCifrada");
            byte[] chaveSimetricaBytes = decifrarChaveSimetrica(chaveSimetricaCifrada, privateKey);
            Certificate certificate = extrairCertificadoDoZip(zipInputStream, "Certificado");

            SecretKey chaveSimetrica = new SecretKeySpec(chaveSimetricaBytes, "AES");
            byte[] dadosAssinados = decifrarDados(dadosCifrados, chaveSimetrica);

            if (validarAssinatura(dadosAssinados, certificate)) {
                this.infoLicensa = Arrays.toString(dadosAssinados);
                return true;
            } else {return false;}
        }
    }

    private byte[] extrairArquivoDoZip(ZipInputStream zipInputStream, String nomeArquivo) throws Exception {
        ZipEntry zipEntry;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals(nomeArquivo)) {
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {outputStream.write(buffer, 0, bytesRead);}
                zipInputStream.closeEntry();
                return outputStream.toByteArray();
            }
            zipInputStream.closeEntry();
        }
        throw new IllegalArgumentException("Arquivo não encontrado: " + nomeArquivo);
    }

    private static byte[] decifrarChaveSimetrica(byte[] chaveSimetricaCifrada, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(chaveSimetricaCifrada);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {throw new RuntimeException(e);}
    }

    private static byte[] decifrarDados(byte[] dadosCifrados, SecretKey chaveSimetrica) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
            return cipher.doFinal(dadosCifrados);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {throw new RuntimeException(e);}
    }

    private Certificate extrairCertificadoDoZip(ZipInputStream zipInputStream, String nomeArquivo) throws IOException, CertificateException {
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().equals(nomeArquivo)) {
                byte[] certificadoBytes = extrairConteudoDoZip(zipInputStream);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream certificadoStream = new ByteArrayInputStream(certificadoBytes);
                return cf.generateCertificate(certificadoStream);
            }
        }
        throw new IOException("Arquivo não encontrado: " + nomeArquivo);
    }

    private boolean validarAssinatura(byte[] dadosValidar, Certificate certificate) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(certificate.getPublicKey());
            signature.update(dadosValidar);
            return signature.verify(dadosValidar);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private byte[] extrairConteudoDoZip(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zipInputStream.read(buffer)) > 0) {byteArrayOutputStream.write(buffer, 0, len);}
        return byteArrayOutputStream.toByteArray();
    }
}
