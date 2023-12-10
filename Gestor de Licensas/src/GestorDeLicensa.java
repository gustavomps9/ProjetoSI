import org.bouncycastle.asn1.x509.ObjectDigestInfo;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class GestorDeLicensa {
    public String dadosPedido;
    public GestorDeLicensa(KeyPair parChaves) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] conteudo = carregaConteudo("PedidoLicensa/pedidoLicenca");
        byte[] signature = carregaAssinatura("PedidoLicensa/assinatura");
        PublicKey publicKey = carregaCertificado("PedidoLicensa/certificado");

        if (processaPedido(conteudo, signature, publicKey)){
            EmissorLicensa emissorLicensa = new EmissorLicensa(parChaves, this.dadosPedido);
        }
    }

    public boolean processaPedido(byte[] conteudo, byte[] signature, PublicKey publicKey){
        if (verificaAssinatura(conteudo, signature, publicKey)) {
            byte[] encryptedSymmetricKey = carregaConteudo("PedidoLicensa/chaveSimetrica");
            PrivateKey privateKey = carregaChaveDecifra("chaveAssimetrica");
            SecretKey symmetricKey = decifraChaveSimetrica(encryptedSymmetricKey, privateKey);
            byte[] decryptedData = decifrarDadosSimetricamente(symmetricKey, conteudo);
            this.dadosPedido = new String(decryptedData);
            return true;
        }
        else {return false;}
    }

    private static byte[] carregaConteudo(String fileName){
        Path path = Paths.get(fileName);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] carregaAssinatura(String fileName){
        Path path = Paths.get(fileName);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private static PublicKey carregaCertificado(String fileName){
        Path path = Paths.get(fileName);
        try {
            byte[] keyBytes = Files.readAllBytes(path);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private static boolean verificaAssinatura(byte[] data, byte[] signature, PublicKey publicKey){
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey carregaChaveDecifra(String fileName){
        Path path = Paths.get(fileName);
        try {
            byte[] keyBytes = Files.readAllBytes(path);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    private static SecretKey decifraChaveSimetrica(byte[] encryptedSymmetricKey, PrivateKey privateKey){
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedKeyBytes = cipher.doFinal(encryptedSymmetricKey);
            return new SecretKeySpec(decryptedKeyBytes, "AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    public byte[] decifrarDadosSimetricamente(SecretKey chaveSimetrica, byte[] dadosCifrados){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
            return cipher.doFinal(dadosCifrados);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }
}
