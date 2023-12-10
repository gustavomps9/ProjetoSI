public class Teste {
    public static void main(String[] args) {
        ControleExecucao controleExecucao = new ControleExecucao("TrackTune", "1.0");
        if (controleExecucao.isRegistered()){
            controleExecucao.showLicenseInfo();
        }else{
            controleExecucao.startRegistration();
        }
    }
}
