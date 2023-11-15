import java.util.Scanner;

public class Utilizador {
    private String nome;
    private String email;
    private int numIdentificacaoCivil;
    private String certificadoChavePublica;

    public Utilizador() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Nome: ");
        this.nome = scanner.nextLine();

        System.out.println("Email: ");
        this.email = scanner.nextLine();

        System.out.println("Número de identificação civil: ");
        this.numIdentificacaoCivil = scanner.nextInt();

        System.out.println("Certificado de chave pública do cartão de cidadão: ");
        this.certificadoChavePublica = scanner.nextLine();
    }

    public String getNome() {return nome;}

    public String getEmail() {return email;}

    public int getNumIdentificacaoCivil() {return numIdentificacaoCivil;}

    public String getCertificadoChavePublica() {return certificadoChavePublica;}

    @Override
    public String toString() {
        return "Nome:'" + nome + '\'' + ", Email:'" + email + '\'' +
                ", Número de Identificação Civil:'" + numIdentificacaoCivil + '\'' +
                ", Certificado da chave publica do cartão de cidadão:'" + certificadoChavePublica;
    }
}
