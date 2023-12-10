import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EmissorLicensa {
    private KeyPair parChaves;
    private SecretKey chaveSimetrica;
    private String informacoesLicensa;

    /**
     * construtor que inicializa as variáveis para fazer a emissão da licença
     * @param parChaves
     * @param informacoesLicensa
     */
    public EmissorLicensa(KeyPair parChaves, String informacoesLicensa) {
        this.parChaves = parChaves;
        this.informacoesLicensa = adicionarDataValidacao(informacoesLicensa, dataExpiracao());

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try {
            gerarChaveSimetrica();
            adicionaRegistro(this.informacoesLicensa);
            salvarNaPastaZip("licenca.zip");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * adiciona um registo que contém as informações da licença num ficheiro no diretório "Apps Distribuídas".
     * @return
     */
    public static void adicionaRegistro(String conteudo) throws IOException {
        File diretorio = new File("Apps Distribuídas");
        if (!diretorio.exists()) {diretorio.mkdir();}

        File[] arquivos = diretorio.listFiles();
        int numeroDeArquivos = (arquivos != null) ? arquivos.length : 0;
        File novoArquivo = new File(diretorio, "App " + (numeroDeArquivos + 1));

        try (FileWriter writer = new FileWriter(novoArquivo)) {writer.write(conteudo);}
    }

    /**
     * solicita ao utilizador a quantidade de dias que a licença deve durar
     * @return
     */
    private Date dataExpiracao(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite a quantidade de dias que durará a licensa: ");
        int quantidadeDias = scanner.nextInt();

        Date dataAtual = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dataAtual);
        calendar.add(Calendar.DAY_OF_YEAR, quantidadeDias);

        return calendar.getTime();
    }

    /**
     * adiciona as informações de data atual e de validade à string JSON que representa a licença
     * @return
     */
    public static String adicionarDataValidacao(String jsonConcatenado, Date dataValidacao) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dataAtualFormatada = sdf.format(new Date());
        String jsonComDataAtual = String.format("%s,\n\"Data atual\":\"%s\",\n", jsonConcatenado, dataAtualFormatada);
        String dataValidacaoFormatada = sdf.format(dataValidacao);
        String jsonComData = String.format("%s\"Data expiracao\":\"%s\"}", jsonComDataAtual, dataValidacaoFormatada);
        return jsonComData;
    }

    /**
     * gera uma chave simétrica AES com tamanho de 256 bits
     */
    private void gerarChaveSimetrica() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        chaveSimetrica = keyGen.generateKey();
    }

    /**
     * cifra as informações da licença utilizando a chave simétrica AES
     * @return
     */
    private byte[] cifraDados() throws Exception {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, chaveSimetrica);
        return cipher.doFinal(informacoesLicensa.getBytes());
    }

    /**
     * assina as informações cifradas da licença utilizando a chave privada do par de chaves
     * @return
     */
    private byte[] assinaLicensa() throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(parChaves.getPrivate());
        signature.update(cifraDados());
        return signature.sign();
    }

    /**
     * cria um ficheiro zip e adiciona os diferentes componentes da licença (assinatura, licença cifrada, chave de validação e chave simétrica) ao zip
     */
    private void salvarNaPastaZip(String zipFileName) {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            adicionarAoZip("Assinatura", assinaLicensa(), zos);
            adicionarAoZip("Licenca", cifraDados(), zos);
            adicionarAoZip("ChaveValidacao", parChaves.getPublic().getEncoded(), zos);
            adicionarAoZip("ChaveSimetrica", this.chaveSimetrica.getEncoded(), zos);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * adiciona uma entrada ao ficheiro zip com um nome específico e os dados associados.
     */
    private void adicionarAoZip(String entryName, byte[] data, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }
}
