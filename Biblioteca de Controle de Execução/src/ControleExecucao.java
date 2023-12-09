import java.io.IOException;
import java.security.PublicKey;
import java.util.Scanner;

public class ControleExecucao {
    public String infoLicensa;
    public String nomeApp;
    public String versao;

    public ControleExecucao(String nomeApp, String versao) {
        this.nomeApp = nomeApp;
        this.versao = versao;
    }

    public boolean isRegistered() {
        ValidaLicensa validaLicensa = new ValidaLicensa();
        try {
            byte[] dadosCifrados = validaLicensa.carregarDadosLicensa("licenca/Licenca");
            PublicKey chaveValidacao = validaLicensa.carregarChaveValidacao("licenca/ChaveValidacao");
            byte[] assinatura = validaLicensa.carregarDadosLicensa("licenca/Assinatura");
            byte[] chaveSimetricaBytes = validaLicensa.carregarDadosLicensa("licenca/ChaveSimetrica");

            if (validaLicensa.validarAssinaturaLicensa(chaveValidacao, assinatura, dadosCifrados)) {
                this.infoLicensa = validaLicensa.decifrarDadosLicensa(chaveSimetricaBytes, dadosCifrados);
                return true;
            } else {return false;}
        } catch (Exception e) {
            System.out.println("Erro: " + e);
            return false;
        }
    }

    public boolean startRegistration(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Nome: ");
        String nome = scanner.next();
        System.out.println("Email: ");
        String email = scanner.next();
        System.out.println("Número de Identificação Civil: ");
        int numIdCivil = scanner.nextInt();

        DadosLicensa dadosLicensa = new DadosLicensa(nome, email, numIdCivil, this.nomeApp, this.versao);
        EmitePedido emitePedido = new EmitePedido(dadosLicensa.toJson());
        //Resto da lógica
        return true;
    }

    public void showLicenseInfo(){
        System.out.println(this.infoLicensa);
    }
}
