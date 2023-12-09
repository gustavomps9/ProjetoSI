import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class GestorDeLicensa {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        byte[] data = loadContent("PedidoLicensa/pedidoLicenca");
        byte[] signature = loadSignature("PedidoLicensa/assinatura");
        PublicKey publicKey = loadPublicKey("PedidoLicensa/certificado");

        if (verifySignature(data, signature, publicKey)) {
            System.out.println("Assinatura válida.");

            byte[] encryptedSymmetricKey = loadContent("PedidoLicensa/chaveSimetrica");
            PrivateKey privateKey = loadPrivateKey("PedidoLicensa/chaveAssimetrica");
            SecretKey symmetricKey = decryptSymmetricKey(encryptedSymmetricKey, privateKey);
            byte[] decryptedData = decifrarDados(symmetricKey, data);

            System.out.println("Texto decifrado: " + new String(decryptedData));
        }
        else {System.out.println("Assinatura inválida.");}
    }

    private static byte[] loadContent(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        return Files.readAllBytes(path);
    }

    private static byte[] loadSignature(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        return Files.readAllBytes(path);
    }

    private static PublicKey loadPublicKey(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        byte[] keyBytes = Files.readAllBytes(path);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    private static PrivateKey loadPrivateKey(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        byte[] keyBytes = Files.readAllBytes(path);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private static SecretKey decryptSymmetricKey(byte[] encryptedSymmetricKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedSymmetricKey);
        return new SecretKeySpec(decryptedKeyBytes, "AES");
    }

    private static byte[] decifrarDados(SecretKey chaveSimetrica, byte[] dadosCifrados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
        return cipher.doFinal(dadosCifrados);
    }
}
