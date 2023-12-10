import com.google.gson.Gson;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class DadosLicensa {
    public String nome;
    public String email;
    public int numIdentificacaoCivil;
    public String nomeApp;
    public String versao;
    public List<String> macAddresses;
    public List<String> volumeSerialNumbers;
    public int numCpus;
    public String tipoCpus;

    /**
     * instancia os dados do pedido de licensa
     * @param nome
     * @param email
     * @param numIdentificacaoCivil
     * @param nomeApp
     * @param versao
     */
    public DadosLicensa(String nome, String email, int numIdentificacaoCivil, String nomeApp, String versao) {
        this.nome = nome;
        this.email = email;
        this.numIdentificacaoCivil = numIdentificacaoCivil;
        this.nomeApp = nomeApp;
        this.versao = versao;
        this.macAddresses = getMacAddresses();
        this.volumeSerialNumbers = getVolumeSerialNumbers();
        this.numCpus = Runtime.getRuntime().availableProcessors();
        this.tipoCpus = System.getProperty("os.arch");
    }

    /**
     * busca os mac addresses da máquina do utilizador
     * @return
     */
    private List<String> getMacAddresses() {
        List<String> macAddresses = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macAddress = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));}
                    macAddresses.add(macAddress.toString());
                }
            }
        } catch (SocketException e) {
            System.out.println("Erro: " + e);
        }
        return macAddresses;
    }

    /**
     * busca os serial numbers da máquina do utilizador
     * @return
     */
    private List<String> getVolumeSerialNumbers() {
        List<String> volumeSerialNumbers = new ArrayList<>();

        File[] roots = File.listRoots();
        for (File root : roots) {
            String volumeSerialNumber = getVolumeSerialNumber(root);
            volumeSerialNumbers.add(volumeSerialNumber);
        }
        return volumeSerialNumbers;
    }

    private String getVolumeSerialNumber(File root) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "vol", root.toString());
                Process process = processBuilder.start();

                try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {return scanner.hasNext() ? scanner.next().trim() : "N/A";}
            } else {return "N/A";}
        } catch (Exception e) {
            System.out.println("Erro: " + e);
            return "N/A";
        }
    }

    /**
     * formata em json o dados para o pedido de licensa
     * @return
     */
    public String toJson() {
        String utilizador = String.format("\"Utilizador\":{\"nome\":\"%s\",\"email\":\"%s\",\"numIdentificacaoCivil\":%d}", this.nome, this.email, this.numIdentificacaoCivil);

        String app = String.format("\"App\":{\"app\":\"%s\",\"versao\":\"%s\"}", this.nomeApp, this.versao);

        String sistema = String.format("\"Sistema\":{\"macAddresses\":[%s],\"volumeSerialNumbers\":[%s],\"numCpus\":%d,\"tipoCpus\":\"%s\"}",
                this.macAddresses.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                this.volumeSerialNumbers.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                this.numCpus, tipoCpus);

        return String.format("{%s,\n%s,\n%s}", utilizador, app, sistema);
    }
}
