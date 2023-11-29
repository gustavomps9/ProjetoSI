import java.util.Scanner;

public class Utilizador {
    private String nome;
    private String email;
    private int numIdentificacaoCivil;

    public Utilizador() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Nome: ");
        this.nome = scanner.next();

        System.out.println("Email: ");
        this.email = scanner.next();

        System.out.println("Número de identificação civil: ");
        this.numIdentificacaoCivil = scanner.nextInt();
    }

    @Override
    public String toString() {
        return "{Nome:'" + nome + '\'' + ", Email:'" + email + '\'' +
                ", Número de Identificação Civil:'" + numIdentificacaoCivil + "'}";
    }
}
