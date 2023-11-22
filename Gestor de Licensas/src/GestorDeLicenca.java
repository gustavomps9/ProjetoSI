import com.BibliotecaDeControleDeExecucao.ControleExecucao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;

public class GestorDeLicenca {
    public static void main(String[] args) {
        // Gera um par de chaves para o autor/distribuidor
        KeyPair keyPair = generateKeyPair();

        // Criacao do pedido de registo e sucessiva emissão da licença
        ControleExecucao controleExecucao = new ControleExecucao("MinhaApp", "1.0");
        controleExecucao.startRegistration();

        // Guarda o par de chaves do autor/distribuidor num ficheiro
        saveKeyPair(keyPair, "chavesAutor");

        // Validação da licença
        if (validateLicense(controleExecucao, keyPair)) {
            System.out.println("Licença válida.");
        } else {
            System.out.println("Licença inválida.");
        }
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static void saveKeyPair(KeyPair keyPair, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(keyPair);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean validateLicense(ControleExecucao controleExecucao, KeyPair keyPair) {
        // informações do sistema/utilizador
        String sistemaInfo = obterInformacoesDoSistema();
        String utilizadorInfo = obterInformacoesDoUtilizador();

        // Chamada do método da biblioteca para validar a licença com informações do sistema/usuário
        return controleExecucao.app.validateLicense(keyPair, sistemaInfo, utilizadorInfo);
    }

    private static String obterInformacoesDoSistema() {
        // Lógica para obter informações do sistema

        return "InformacoesDoSistema";
    }

    private static String obterInformacoesDoUtilizador() {
        // Lógica para obter informações do usuário

        return "InformacoesDoUsuario";
    }
}
