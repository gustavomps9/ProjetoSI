public class Aplicacao {
    private final String nome;
    private final String versao;

    public Aplicacao(String nome, String versao) {
        this.nome = nome;
        this.versao = versao;
    }

    @Override
    public String toString() {
        return "{Nome:'" + nome + '\'' + ", Versão:'" + versao + "'}";
    }
}
