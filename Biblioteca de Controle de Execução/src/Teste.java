import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

public class Teste {
    public static void main(String[] args) throws Exception {
        ControleExecucao controleExecucao = new ControleExecucao("Bible YouVersion", "1.0");
        controleExecucao.startRegistration();
    }
}
