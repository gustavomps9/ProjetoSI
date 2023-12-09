import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class ValidaLicensa {
    public ValidaLicensa() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private boolean validaConteudo(){
        //lógica a implementar
        return false;
    }

    public PublicKey carregarChaveValidacao(String nomeFicheiro) throws Exception {
        byte[] chavePublicaBytes = carregarDadosLicensa(nomeFicheiro);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec chaveSpec = new X509EncodedKeySpec(chavePublicaBytes);
        return keyFactory.generatePublic(chaveSpec);
    }

    public byte[] carregarDadosLicensa(String nomeFicheiro) throws IOException {
        FileInputStream fis = new FileInputStream(nomeFicheiro);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return data;
    }

    public boolean validarAssinaturaLicensa(PublicKey chaveValidacao, byte[] assinatura, byte[] dadosCifrados) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initVerify(chaveValidacao);
        signature.update(dadosCifrados);
        return signature.verify(assinatura);
    }

    public String decifrarDadosLicensa(byte[] chaveSimetricaBytes, byte[] dadosCifrados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        SecretKey chaveSimetrica = new javax.crypto.spec.SecretKeySpec(chaveSimetricaBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
        byte[] dadosDecifrados = cipher.doFinal(dadosCifrados);
        return new String(dadosDecifrados);
    }
}
