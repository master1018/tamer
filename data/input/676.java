public class SystemEnvironment {
    private String hostname;
    private String hostId;
    private String osName;
    private String osVersion;
    private String osArchitecture;
    private String systemModel;
    private String systemManufacturer;
    private String cpuManufacturer;
    private String serialNumber;
    private static SystemEnvironment sysEnv = null;
    public static synchronized SystemEnvironment getSystemEnvironment() {
        if (sysEnv == null) {
            String os = System.getProperty("os.name");
            if (os.equals("SunOS")) {
                sysEnv = new SolarisSystemEnvironment();
            } else if (os.equals("Linux")) {
                sysEnv = new LinuxSystemEnvironment();
            } else if (os.startsWith("Windows")) {
                sysEnv = new WindowsSystemEnvironment();
            } else {
                sysEnv = new SystemEnvironment();
            }
        }
        return sysEnv;
    }
    SystemEnvironment() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            this.hostname = "Unknown host";
        }
        this.hostId = "";
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.osArchitecture = System.getProperty("os.arch");
        this.systemModel = "";
        this.systemManufacturer = "";
        this.cpuManufacturer = "";
        this.serialNumber = "";
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public void setOsName(String osName) {
        this.osName = osName;
    }
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }
    public void setSystemModel(String systemModel) {
        this.systemModel = systemModel;
    }
    public void setSystemManufacturer(String systemManufacturer) {
        this.systemManufacturer = systemManufacturer;
    }
    public void setCpuManufacturer(String cpuManufacturer) {
        this.cpuManufacturer = cpuManufacturer;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public void setHostId(String hostId) {
        if (hostId == null || hostId.equals("null")) {
            hostId = "";
        }
        if (hostId.length() > 16) {
            hostId = hostId.substring(0,16);
        }
        this.hostId = hostId;
    }
    public String getHostname() {
        return hostname;
    }
    public String getOsName() {
        return osName;
    }
    public String getOsVersion() {
        return osVersion;
    }
    public String getOsArchitecture() {
        return osArchitecture;
    }
    public String getSystemModel() {
        return systemModel;
    }
    public String getSystemManufacturer() {
        return systemManufacturer;
    }
    public String getSerialNumber() {
        return serialNumber;
    }
    public String getHostId() {
        return hostId;
    }
    public String getCpuManufacturer() {
        return cpuManufacturer;
    }
    protected String getCommandOutput(String... command) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            p = pb.start();
            p.waitFor();
            if (p.exitValue() == 0) {
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(line);
                    }
                }
            }
            return sb.toString();
        } catch (InterruptedException ie) {
            if (p != null) {
                p.destroy();
            }
            return "";
        } catch (Exception e) {
            return "";
        } finally {
            if (p != null) {
                try {
                    p.getErrorStream().close();
                } catch (IOException e) {
                }
                try {
                    p.getInputStream().close();
                } catch (IOException e) {
                }
                try {
                    p.getOutputStream().close();
                } catch (IOException e) {
                }
                p = null;
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }
    protected String getFileContent(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
