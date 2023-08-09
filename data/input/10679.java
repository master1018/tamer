class SolarisSystemEnvironment extends SystemEnvironment {
    private static final String ORACLE = "Oracle Corporation";
    SolarisSystemEnvironment() {
        setHostId(getCommandOutput("/usr/bin/hostid"));
        setSystemModel(getCommandOutput("/usr/bin/uname", "-i"));
        setSystemManufacturer(getSolarisSystemManufacturer());
        setCpuManufacturer(getSolarisCpuManufacturer());
        setSerialNumber(getSolarisSN());
    }
    private String getSolarisCpuManufacturer() {
        if ("sparc".equalsIgnoreCase(System.getProperty("os.arch"))) {
            return ORACLE;
        }
        return getSmbiosData("4", "Manufacturer: ");
    }
    private String getSolarisSystemManufacturer() {
        if ("sparc".equalsIgnoreCase(System.getProperty("os.arch"))) {
            return ORACLE;
        }
        return getSmbiosData("1", "Manufacturer: ");
    }
    private String getSolarisSN() {
        String tmp = getFileContent("/var/run/psn");
        if (tmp.length() > 0) {
            return tmp.trim();
        }
        String tmpSN = getSneepSN();
        if (tmpSN.length() > 0) {
            return tmpSN;
        }
        tmpSN = getSmbiosData("1", "Serial Number: ");
        if (tmpSN.length() > 0) {
            return tmpSN;
        }
        tmpSN = getSmbiosData("3", "Serial Number: ");
        if (tmpSN.length() > 0) {
            return tmpSN;
        }
        return "";
    }
    private String getSmbiosData(String type, String target) {
        String output = getCommandOutput("/usr/sbin/smbios", "-t", type);
        for (String s : output.split("\n")) {
            if (s.contains(target)) {
                int indx = s.indexOf(target) + target.length();
                if (indx < s.length()) {
                    String tmp = s.substring(indx).trim();
                    String lowerCaseStr = tmp.toLowerCase();
                    if (!lowerCaseStr.startsWith("not available")
                            && !lowerCaseStr.startsWith("to be filled by o.e.m")) {
                        return tmp;
                    }
                }
            }
        }
        return "";
    }
    private String getSneepSN() {
        String basedir = getCommandOutput("pkgparam","SUNWsneep","BASEDIR");
        File f = new File(basedir + "/bin/sneep");
        if (f.exists()) {
            String sneepSN = getCommandOutput(basedir + "/bin/sneep");
            if (sneepSN.equalsIgnoreCase("unknown")) {
                return "";
            } else {
                return sneepSN;
            }
        } else {
            return "";
        }
    }
}
