import javax.crypto.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValidaLicensa {
    public ValidaLicensa() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public boolean validaConteudo(String jsonString, String nomeApp, String versaoApp) {
        String nomeAppNoJson = jsonString.substring(jsonString.indexOf("\"app\":\"") + 7, jsonString.indexOf("\",\"versao\""));
        String versaoAppNoJson = jsonString.substring(jsonString.indexOf("\"versao\":\"") + 10, jsonString.indexOf("\"}", jsonString.indexOf("\"versao\":\"")));

        if (!nomeAppNoJson.equals(nomeApp) || !versaoAppNoJson.equals(versaoApp)) {
            System.out.println("Nome ou versão da aplicação não correspondem.");
            return false;
        }

        String dataExpiracaoString = jsonString.substring(jsonString.indexOf("\"Data expiracao\":\"") + 18, jsonString.indexOf("\"}", jsonString.indexOf("\"Data expiracao\":\"")));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            Date dataExpiracao = sdf.parse(dataExpiracaoString);
            Date dataAtual = new Date();
            return dataExpiracao.after(dataAtual);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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
