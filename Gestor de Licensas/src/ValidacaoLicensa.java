import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class ValidacaoLicensa {

    public static void main(String[] args) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            PublicKey chaveValidacao = carregarChaveValidacao("ChaveValidacao");
            byte[] assinatura = carregarDados("Assinatura");
            byte[] dadosCifrados = carregarDados("Licensa");
            byte[] chaveSimetricaBytes = carregarDados("ChaveSimetrica");

            if (validarAssinatura(chaveValidacao, assinatura, dadosCifrados)) {
                String dadosDecifrados = decifrarDados(chaveSimetricaBytes, dadosCifrados);
                System.out.println("Conteúdo válido:\n" + dadosDecifrados);
            } else {
                System.out.println("Assinatura inválida. A licença não é válida.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PublicKey carregarChaveValidacao(String nomeFicheiro) throws Exception {
        byte[] chavePublicaBytes = carregarDados(nomeFicheiro);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec chaveSpec = new X509EncodedKeySpec(chavePublicaBytes);

        return keyFactory.generatePublic(chaveSpec);
    }

    private static byte[] carregarDados(String nomeFicheiro) throws IOException {
        FileInputStream fis = new FileInputStream(nomeFicheiro);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return data;
    }

    private static boolean validarAssinatura(PublicKey chaveValidacao, byte[] assinatura, byte[] dadosCifrados) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initVerify(chaveValidacao);
        signature.update(dadosCifrados);
        return signature.verify(assinatura);
    }

    private static String decifrarDados(byte[] chaveSimetricaBytes, byte[] dadosCifrados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        SecretKey chaveSimetrica = new javax.crypto.spec.SecretKeySpec(chaveSimetricaBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
        byte[] dadosDecifrados = cipher.doFinal(dadosCifrados);
        return new String(dadosDecifrados);
    }
}
