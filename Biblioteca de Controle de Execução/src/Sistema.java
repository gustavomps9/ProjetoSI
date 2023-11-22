import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringJoiner;

public class Sistema {
    private int numCpus;
    private String tipoCpus;
    private List<String> macAddresses;
    private List<String> volumeSerialNumbers;

    public Sistema() {
        this.numCpus = Runtime.getRuntime().availableProcessors();
        this.tipoCpus = System.getProperty("os.arch");
        this.macAddresses = getMacAddresses();
        this.volumeSerialNumbers = getVolumeSerialNumbers();
    }

    private List<String> getMacAddresses() {
        List<String> macAddresses = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macAddress = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    macAddresses.add(macAddress.toString());
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAddresses;
    }

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

                try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
                    return scanner.hasNext() ? scanner.next().trim() : "N/A";
                }
            } else {
                return "N/A";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Número de CPUs: ").append(numCpus).append("\n");
        result.append("Tipo de CPUs: ").append(tipoCpus).append("\n");
        result.append("Endereços MAC:\n");
        for (String macAddress : macAddresses) {
            result.append("  ").append(macAddress).append("\n");
        }
        result.append("Números de Série dos Volumes:\n");
        for (String volumeSerialNumber : volumeSerialNumbers) {
            result.append("  ").append(volumeSerialNumber).append("\n");
        }
        return result.toString();
    }
}
