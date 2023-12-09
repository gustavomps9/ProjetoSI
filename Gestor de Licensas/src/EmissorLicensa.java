import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;
import java.security.Signature;

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
            salvarNoFicheiro("Assinatura", assinaLicensa());
            salvarNoFicheiro("Licensa", cifraDados());
            salvarNoFicheiro("ChaveValidacao", parChaves.getPublic().getEncoded());
            salvarNoFicheiro("ChaveSimetrica", this.chaveSimetrica.getEncoded());
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
}
