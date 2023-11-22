import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class Licenca {

    private String nomeDaApp;
    private String versao;
    private byte[] dadosDaLicenca;
    private byte[] assinaturaDigital;

    public Licenca(String nomeDaApp, String versao, byte[] dadosDaLicenca) {
        this.nomeDaApp = nomeDaApp;
        this.versao = versao;
        this.dadosDaLicenca = dadosDaLicenca;
    }


    public String getIdentificadorDoSistema() {
        // Implementar a lógica para obter o identificador do sistema
        return "IdentificadorDoSistema";
    }

    public String getIdentificadorDoUtilizador() {
        // Implementar a lógica para obter o identificador do utilizador
        return "IdentificadorDoUtilizador";
    }

    public Date getDataDeValidade() {
        // Implementar a lógica para obter a data de validade da licença
        return new Date();
    }

    public void saveKeyPair(KeyPair keyPair, String alias) {
        // Armazenamento do par de chaves no KeyStore
        GestorDeLicenca.saveKeyPair(keyPair, alias, keystorePassword);
    }

    public KeyPair loadKeyPair(String alias) {
        // Carregamento do par de chaves do KeyStore
        return GestorDeLicenca.loadKeyPair(alias, keystorePassword);
    }

    public byte[] calculateHash(byte[] data) {
        // Cálculo do hash SHA-256
        return GestorDeLicenca.calculateHash(data);
    }

    public KeyPair generateKeyPair() {
        // Geração de um par de chaves RSA
        return GestorDeLicenca.generateKeyPair();
    }

    public SecretKey generateSymmetricKey() {
        // Geração de uma chave simétrica AES
        return GestorDeLicenca.generateSymmetricKey();
    }


    private String getUserIdentifier() {
        // Lógica para obter o identificador do utilizador (pode ser um número de série, nome, etc.)
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o identificador do utilizador:");
        return scanner.nextLine();
    }

    private SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
        // Implemente a lógica para gerar e retornar uma chave simétrica.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private Date obterDataExpiracao(byte[] dadosDecifrados) {
        try {
            String dadosComoString = new String(dadosDecifrados, "UTF-8");
            int indiceInicio = dadosComoString.indexOf("Data de Expiracao:") + "Data de Expiracao:".length();
            int indiceFim = dadosComoString.indexOf(",", indiceInicio);
            String dataExpiracaoStr = dadosComoString.substring(indiceInicio, indiceFim).trim();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dataExpiracaoStr);
        } catch (UnsupportedEncodingException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidTimeFrame(byte[] dadosDecifrados) {
        try {
            Date dataExpiracao = obterDataExpiracao(dadosDecifrados);

            // Verifique se a data de expiração está definida e se é posterior à data atual
            if (dataExpiracao != null) {
                Date dataAtual = new Date();
                return dataAtual.before(dataExpiracao);
            } else {
                // Se a data de expiração não puder ser obtida, considere a licença como inválida
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isRegistered(KeyPair keyPair) throws Exception {
        // Decifra os dados da licença com o auxílio da chave simétrica
        Cipher cipher = Cipher.getInstance("AES");
        // substituir pela chave simétrica real
        SecretKey chaveSimetrica = generateSymmetricKey();
        cipher.init(Cipher.DECRYPT_MODE, chaveSimetrica);
        byte[] dadosDecifrados = cipher.doFinal(dadosDaLicenca);

        // Verifica a assinatura digital dos dados decifrados com o auxílio da chave pública
        Signature assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initVerify(keyPair.getPublic());
        assinatura.update(dadosDecifrados);

        return assinatura.verify(assinaturaDigital) && isValidTimeFrame(dadosDecifrados);
    }

    public boolean startRegistration(KeyPair keyPair) throws Exception {
        String identificadorDoSistema = getIdentificadorDoSistema();
        String identificadorDoUtilizador = getIdentificadorDoUtilizador();
        Date dataDeValidade = getDataDeValidade();

        // Construção dos dados da licença
        String dadosLicencaString = "Identificador do Sistema: " + identificadorDoSistema +
                ", Identificador do Utilizador: " + identificadorDoUtilizador +
                ", Data de Expiracao: " + new SimpleDateFormat("yyyy-MM-dd").format(dataDeValidade);

        byte[] dadosDaLicenca = dadosLicencaString.getBytes("UTF-8");

        // Criação de chave simétrica para cifrar os dados da licença
        SecretKey chaveSimetrica = generateSymmetricKey();

        // Cifra os dados da licença com a chave simétrica
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, chaveSimetrica);
        byte[] dadosCifrados = cipher.doFinal(dadosDaLicenca);

        // Criação de uma assinatura digital dos dados cifrados com o auxilio da chave privada
        Signature assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initSign(keyPair.getPrivate());
        assinatura.update(dadosCifrados);
        assinaturaDigital = assinatura.sign();

        // Armazenamento dos dados cifrados e da assinatura digital
        dadosDaLicenca = dadosCifrados;

        PublicKey chavePublicaUtilizador = getPublicKeyFromIdentifier("IdentificadorDoUtilizador");

        // Autentica o utilizador com a chave pública registada na licença
        if (!authenticateUser(chavePublicaUtilizador)) {
            throw new Exception("Falha na autenticação do utilizador.");
        }

        return true;
    }

    public static KeyPair loadKeyPair(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (KeyPair) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private PublicKey getPublicKeyFromIdentifier(String identifier) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Implementar a lógica para obter a chave pública do utilizador a partir do identificador
        // isto é uma chave pública de exemplo. precisamos de substituir pela lógica real.

        // simulação de uma codificação da chave pública em formato Base64
        String chavePublicaBase64 = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAqTgY79YDflFk1u7KyGx0LfzTuQ6uUktOl2OCH3TZ1KLh5Jwmn98/HlnnqVZx1eMxMkN1v9Qz1aVV8bLdPbxbetnGQgjXkysJeTlFZY4r9/9rlCvs3/xlRctEmO5uRz8bDSZnEVBff+4JtDYKYEP90pDKOWbsUjkI2NMa9irY0EebiR0iGJvZIfTZV1VIm8bc34yMVCm3RU+YB5IiRizW1Q5c+opctU4MLDgItKcX1p3IlJTPg8p0bQfrC3g0GiGiOdEPfNrl5k/ia8Mf0KZIDCugUEtwqX7KKLEpYfbfw+oxXlhe65c4D+6NS5r71Ss8Sl/qnbjr+MksotomFucNe0F85/TUoW+jb6xYP2uBX1F5qilwDeSYu93bhKy/JvdvI8b5A0LqzzTn+f5Qr06D8xapPBy6uxLOJv64gYHj3RRIYeXlt7AXxZiZ8HTaDqNGdoCruK0okHsFsDSMd3e8ihovq/FcMvWVY7VV8CVpFYKJuq4XuNol1RbhCixXv20imF2XuopzqfJQ3EXOxwye2NN+8Vl+KqWz6bwTn5O+kE7XpKIlLC/kCJhoWnPtIV5WkOPWU0u4nJiRcZ3EO0rQblnBZak+U7iaFE5aL6hxmN/fc0UnEK6jraXJ3gHIp3Tfp8FzAUz0lx5el/vrT/7TyA8CAwEAAQ==";

        byte[] chavePublicaBytes = Base64.getDecoder().decode(chavePublicaBase64);
        X509EncodedKeySpec chaveSpec = new X509EncodedKeySpec(chavePublicaBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(chaveSpec);
    }

    private boolean authenticateUser(PublicKey chavePublicaUtilizador) throws Exception {
        // Implementar a lógica para autenticar o utilizador com a chave pública
        // Aqui, vamos simular uma autenticação bem-sucedida. Substitua isso pela lógica real.
        return true;
    }


    public void showLicenseInfo() {
        // Implementar a lógica para exibir as informações da licença
        System.out.println("Informações da Licença:");
        System.out.println("Nome da Aplicação: " + nomeDaApp);
        System.out.println("Versão: " + versao);
        System.out.println("Identificador do Sistema: " + getIdentificadorDoSistema());
        System.out.println("Identificador do Utilizador: " + getIdentificadorDoUtilizador());
        System.out.println("Data de Validade: " + getDataDeValidade());
    }

    public boolean validateLicense(KeyPair keyPair) throws Exception {
        // Decifra os dados da licença com o auxilio da chave simetrica
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec("chave-simetrica".getBytes(), "AES"));
        byte[] dadosDecifrados = cipher.doFinal(dadosDaLicenca);

        // Verifica a assinatura digital dos dados decifrados com o auxílio da chave pública
        Signature assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initVerify(keyPair.getPublic());
        assinatura.update(dadosDecifrados);

        return assinatura.verify(assinaturaDigital);
    }
}


    public static KeyPair generateKeyPair() {
        // Implemente a lógica para gerar e retornar um par de chaves.
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
    }

/*
    public static void main(String[] args) throws Exception {
        KeyPair keyPair = generateKeyPair();
        Licenca licenca = new Licenca("MinhaApp", "1.0", "DadosDaLicenca".getBytes());

        licenca.startRegistration(keyPair);

        // exibir informações da licença
        licenca.showLicenseInfo();

        // validaçao da licença
        if (licenca.validateLicense(keyPair)) {System.out.println("Licença válida.");
        } else {System.out.println("Licença inválida."); Quando não é válida aparece esta mensagem}
    }*/
