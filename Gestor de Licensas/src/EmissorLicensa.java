import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;
import java.security.Signature;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EmissorLicensa {
    private KeyPair parChaves;
    private SecretKey chaveSimetrica;
    private String informacoesLicensa;

    public EmissorLicensa(KeyPair parChaves, String informacoesLicensa) {
        this.parChaves = parChaves;
        this.informacoesLicensa = informacoesLicensa;

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try {
            gerarChaveSimetrica();
            salvarNaPastaZip("licenca.zip");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void gerarChaveSimetrica() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        chaveSimetrica = keyGen.generateKey();
    }

    private byte[] cifraDados() throws Exception {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, chaveSimetrica);
        return cipher.doFinal(informacoesLicensa.getBytes());
    }

    private byte[] assinaLicensa() throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(parChaves.getPrivate());
        signature.update(cifraDados());
        return signature.sign();
    }

    private static void salvarNoFicheiro(String nomeFicheiro, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(nomeFicheiro)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarNaPastaZip(String zipFileName) {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            adicionarAoZip("Assinatura", assinaLicensa(), zos);
            adicionarAoZip("Licenca", cifraDados(), zos);
            adicionarAoZip("ChaveValidacao", parChaves.getPublic().getEncoded(), zos);
            adicionarAoZip("ChaveSimetrica", this.chaveSimetrica.getEncoded(), zos);
        } catch (Exception e) {e.printStackTrace();}
    }

    private void adicionarAoZip(String entryName, byte[] data, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }
}
