import javax.crypto.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class ValidaLicensa {
    public ValidaLicensa() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private boolean validaConteudo(){
        //lógica a implementar
        return false;
    }

    public PublicKey carregarChaveValidacao(String nomeFicheiro) {
        try {
            byte[] chavePublicaBytes = carregarDadosLicensa(nomeFicheiro);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec chaveSpec = new X509EncodedKeySpec(chavePublicaBytes);
            return keyFactory.generatePublic(chaveSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    public byte[] carregarDadosLicensa(String nomeFicheiro){
        try {
            FileInputStream fis = new FileInputStream(nomeFicheiro);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return data;
        } catch (IOException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    public boolean validarAssinaturaLicensa(PublicKey chaveValidacao, byte[] assinatura, byte[] dadosCifrados){
        try {
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initVerify(chaveValidacao);
            signature.update(dadosCifrados);
            return signature.verify(assinatura);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    public String decifrarDadosLicensa(byte[] chaveSimetricaBytes, byte[] dadosCifrados){
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            SecretKey chaveSimetrica = new javax.crypto.spec.SecretKeySpec(chaveSimetricaBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
            byte[] dadosDecifrados = cipher.doFinal(dadosCifrados);
            return new String(dadosDecifrados);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }
}
