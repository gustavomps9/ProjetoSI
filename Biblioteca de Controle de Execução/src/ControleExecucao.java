import java.security.PublicKey;
import java.util.Scanner;

public class ControleExecucao {
    public String infoLicensa;
    public String nomeApp;
    public String versao;

    /**
     * construtor que inicializa as variáveis para identificar a aplicação
     * @param nomeApp
     * @param versao
     */
    public ControleExecucao(String nomeApp, String versao) {
        this.nomeApp = nomeApp;
        this.versao = versao;
    }

    /**
     * verifica a existência da licensa e seus ficheiros e valida a licensa
     * @return
     */
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

    /**
     * inicia o processo de criação dos ficheiros para o pedido de licensa
     * @return
     */
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
        return true;
    }

    /**
     * prime na linha de comandos as informações da licensa
     */
    public void showLicenseInfo(){
        System.out.println(this.infoLicensa);
    }
}
