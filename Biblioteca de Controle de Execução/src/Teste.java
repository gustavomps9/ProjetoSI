import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;

public class Teste {
    public static void main(String[] args) {
        ControleExecucao controleExecucao = new ControleExecucao("", "");
        controleExecucao.startRegistration();
    }
}
