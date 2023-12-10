import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Teste {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Tamanho da chave (pode ser ajustado)
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        GestorDeLicensa gestorDeLicensa = new GestorDeLicensa(keyPair);
    }
}
