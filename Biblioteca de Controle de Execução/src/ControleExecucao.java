import java.security.PublicKey;

public class ControleExecucao {
    private Aplicacao aplicacao;
    private Sistema sistema;
    public String infoLicensa;
    public String dadosPedido;

    public ControleExecucao(String nomeDaApp, String versao) {
        this.aplicacao = new Aplicacao(nomeDaApp, versao);
        this.sistema = new Sistema();
    }

    public boolean isRegistered() throws Exception {
        ValidaLicensa validaLicensa = new ValidaLicensa();

        byte[] dadosCifrados = validaLicensa.carregarDadosLicensa("licenca/Licenca");
        PublicKey chaveValidacao = validaLicensa.carregarChaveValidacao("licenca/ChaveValidacao");
        byte[] assinatura = validaLicensa.carregarDadosLicensa("licenca/Assinatura");
        byte[] chaveSimetricaBytes = validaLicensa.carregarDadosLicensa("licenca/ChaveSimetrica");

        if (validaLicensa.validarAssinaturaLicensa(chaveValidacao, assinatura, dadosCifrados)) {
            this.infoLicensa = validaLicensa.decifrarDadosLicensa(chaveSimetricaBytes, dadosCifrados);
            return true;
        } else {return false;}
    }

    private boolean startRegistration(){
        Utilizador utilizador = new Utilizador();
        
        return true;
    }

    public void showLicenseInfo(){
        System.out.println(this.infoLicensa);
    }
}
