import javax.crypto.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class GestorDeLIcensa {
    public File pedidoDeLicensa;

    public GestorDeLIcensa(File pedidoDeLicensa) throws NoSuchPaddingException, IllegalBlockSizeException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, SignatureException, BadPaddingException, InvalidKeyException {
        System.out.println("Selecione o Pedido de Licensa:");

        this.pedidoDeLicensa = showFileChooser();

        if (pedidoDeLicensa == null) {System.out.println("Não há ficheiro a processar");}
        else{
            if (processaPedido()){
                System.out.println("Pedido validado corretamente");
                emitirLicensa();
            } else{System.out.println("Pedido validado incorretamente");}
        }

    }

    private static File showFileChooser() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos de Texto", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {return fileChooser.getSelectedFile();}

        return null;
    }

    /**
     * Método que processa o pedido de registo e retorna true se for bem validado ou false se não
     */
    private boolean processaPedido(){
        //validaAssinatura();
        return true;
    }

    private boolean validaAssinatura(byte dadosValidar) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException {
        Provider[] provs = Security.getProviders();
        Provider provider = null;
        for (Provider prov : provs) {if (prov.getName().equals("SunPKCS11-CartaoCidadao")) {provider = prov;}}

        KeyStore ks = KeyStore.getInstance("PKCS11", provider);
        ks.load(null, null);

        Certificate certificate = ks.getCertificate("CITIZEN SIGNATURE CERTIFICATE");

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());
        signature.update(dadosValidar);
        //return signature.verify(dadosValidar);
        return true;
    }

    /**
     * Método que após o pedido ser processado, emite a licensa
     */
    private void emitirLicensa(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Quantos dias durará a licensa ?");
        int duracao = sc.nextInt();

    }
}
