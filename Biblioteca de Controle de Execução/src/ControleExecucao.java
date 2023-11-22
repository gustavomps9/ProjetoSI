import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class ControleExecucao {
    public Aplicacao app;

    public ControleExecucao(String nome, String versao) throws Exception {
        this.app = new Aplicacao(nome, versao);
        isRegistered();
    }

    private boolean isRegistered() throws Exception {
        Scanner scanner = new Scanner(System.in);

        if (procuraLicenca()) {
            do {
                System.out.println("Deseja ver as informações da licença existente? [S/n]");
                String resposta = scanner.next();

                if ("S".equalsIgnoreCase(resposta)) {
                    app.showLicenseInfo();  // Chama o método showLicenseInfo da classe Aplicacao
                    return true;
                } else if ("n".equalsIgnoreCase(resposta)) {
                    return true;
                }

                System.out.println("Resposta inválida. Por favor, digite 'S' para sim ou 'n' para não.");

            } while (true);
        } else {
            do {
                System.out.println("Deseja pedir um novo registo? [S/n]");
                String resposta = scanner.next();

                if ("S".equalsIgnoreCase(resposta)) {
                    startRegistration();
                    return false;
                } else if ("n".equalsIgnoreCase(resposta)) {
                    return false;
                }

                System.out.println("Resposta inválida. Por favor, digite 'S' para sim ou 'n' para não.");

            } while (true);
        }
    }

    // Método para iniciar o registro conforme as instruções do enunciado
    public boolean startRegistration() throws Exception {
        // Gera informações necessárias para a licença
        String identificadorDoSistema = app.getIdentificadorDoSistema();
        String identificadorDoUtilizador = app.getIdentificadorDoUtilizador();
        Date dataDeValidade = app.getDataDeValidade();

        // Construção dos dados da licença
        String dadosLicencaString = "Identificador do Sistema: " + identificadorDoSistema +
                ", Identificador do Utilizador: " + identificadorDoUtilizador +
                ", Data de Expiracao: " + new SimpleDateFormat("yyyy-MM-dd").format(dataDeValidade);

        byte[] dadosDaLicenca = dadosLicencaString.getBytes(StandardCharsets.UTF_8);

        // Criação de chave simétrica para cifrar os dados da licença
        SecretKey chaveSimetrica = app.generateSymmetricKey();

        // Cifra os dados da licença com a chave simétrica
        byte[] dadosCifrados = app.cifraDocumento(dadosDaLicenca, chaveSimetrica);

        // Codifica os dados cifrados em Base64
        String dadosCodificados = Base64.getEncoder().encodeToString(dadosCifrados);

        // Armazenamento dos dados cifrados no arquivo "PedidoDeRegisto"
        try (FileOutputStream fos = new FileOutputStream("PedidoDeRegisto")) {
            fos.write(dadosCodificados.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Boolean procuraLicenca() {
        // Lógica para procurar a licença. Este método precisa ser implementado.
        // Retorne true se a licença existir, false caso contrário.
        return false;
    }


    private byte[] assinaturaCartaoCidadao(String dadosDocumento) throws Exception {
        Provider[] provs = Security.getProviders();

        KeyStore ks = KeyStore.getInstance("PKCS11", provs[13]);
        ks.load(null, null);

        PrivateKey privateKey = (PrivateKey) ks.getKey("CITIZEN SIGNATURE CERTIFICATE", null);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(dadosDocumento.getBytes());
        return signature.sign();
    }

    private byte[] cifraDocumento(byte[] dados) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, generateSessionKey());
        return cipher.doFinal(dados);
    }

    private byte[] decifraDocumento(byte[] dados SecretKey chaveDocumento) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, chaveDocumento);
        return cipher.doFinal(dados);
    }

    private SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
