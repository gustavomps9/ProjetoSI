public class Aplicacao {
    private String nome;
    private String versao;

    public Aplicacao(String nome, String versao) {
        this.nome = nome;
        this.versao = versao;
    }

    @Override
    public String toString() {return "Nome:'" + nome + '\'' + ", Versão:'" + versao + '\'';}
}
