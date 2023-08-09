public class Registry {
    private static final String STCLIENT_SOLARIS = "/usr/bin/stclient";
    private static final String STCLIENT_LINUX = "/opt/sun/servicetag/bin/stclient";
    private static final int ST_ERR_NOT_AUTH = 245;
    private static final int ST_ERR_REC_NOT_FOUND = 225;
    private static final String INSTANCE_URN_DESC = "Product instance URN=";
    private static boolean initialized = false;
    private static File stclient = null;
    private static String stclientPath = null;
    private static Registry registry = new Registry();
    private static String SVCTAG_STCLIENT_CMD = "servicetag.stclient.cmd";
    private static String SVCTAG_STHELPER_SUPPORTED = "servicetag.sthelper.supported";
    private Registry() {
    }
    private synchronized static String getSTclient() {
        if (!initialized) {
            String os = System.getProperty("os.name");
            if (os.equals("SunOS")) {
                stclient = new File(STCLIENT_SOLARIS);
            } else if (os.equals("Linux")) {
                stclient = new File(STCLIENT_LINUX);
            } else if (os.startsWith("Windows")) {
                stclient = getWindowsStClientFile();
            } else {
                if (isVerbose()) {
                    System.out.println("Running on unsupported platform");
                }
            }
            initialized = true;
        }
        boolean supportsHelperClass = true; 
        if (System.getProperty(SVCTAG_STHELPER_SUPPORTED) != null) {
            supportsHelperClass = Boolean.getBoolean(SVCTAG_STHELPER_SUPPORTED);
        }
        if (!supportsHelperClass) {
            return null;
        }
        String path = System.getProperty(SVCTAG_STCLIENT_CMD);
        if (path != null) {
            return path;
        }
        if (stclientPath == null && stclient != null && stclient.exists()) {
            stclientPath = stclient.getAbsolutePath();
        }
        return stclientPath;
    }
    public static Registry getSystemRegistry() {
        if (isSupported()) {
            return registry;
        } else {
            throw new UnsupportedOperationException("Registry class is not supported");
        }
    }
    public static synchronized boolean isSupported() {
        return getSTclient() != null;
    }
    private static List<String> getCommandList() {
        List<String> command = new ArrayList<String>();
        if (System.getProperty(SVCTAG_STCLIENT_CMD) != null) {
            String cmd = getSTclient();
            int len = cmd.length();
            int i = 0;
            while (i < len) {
                char separator = ' ';
                if (cmd.charAt(i) == '"') {
                    separator = '"';
                    i++;
                }
                int j;
                for (j = i+1; j < len; j++) {
                    if (cmd.charAt(j) == separator) {
                        break;
                    }
                }
                if (i == j-1) {
                    command.add("\"\"");
                } else {
                    command.add(cmd.substring(i,j));
                }
                for (i = j+1; i < len; i++) {
                    if (!Character.isSpaceChar(cmd.charAt(i))) {
                        break;
                    }
                }
            }
            if (isVerbose()) {
                System.out.println("Command list:");
                for (String s : command) {
                    System.out.println(s);
                }
            }
        } else {
            command.add(getSTclient());
        }
        return command;
    }
    private static ServiceTag checkReturnError(int exitValue,
                                               String output,
                                               ServiceTag st) throws IOException {
        switch (exitValue) {
            case ST_ERR_REC_NOT_FOUND:
                return null;
            case ST_ERR_NOT_AUTH:
                if (st != null) {
                    throw new UnauthorizedAccessException(
                        "Not authorized to access " + st.getInstanceURN() +
                        " installer_uid=" + st.getInstallerUID());
                } else  {
                    throw new UnauthorizedAccessException(
                        "Not authorized:" + output);
                }
            default:
                throw new IOException("stclient exits with error" +
                     " (" + exitValue + ")\n" + output);
        }
    }
    public ServiceTag addServiceTag(ServiceTag st) throws IOException {
        List<String> command = getCommandList();
        command.add("-a");
        if (st.getInstanceURN().length() > 0) {
            ServiceTag sysSvcTag = getServiceTag(st.getInstanceURN());
            if (sysSvcTag != null) {
                throw new IllegalArgumentException("Instance_urn = " +
                    st.getInstanceURN() + " already exists");
            }
            command.add("-i");
            command.add(st.getInstanceURN());
        }
        command.add("-p");
        command.add(st.getProductName());
        command.add("-e");
        command.add(st.getProductVersion());
        command.add("-t");
        command.add(st.getProductURN());
        if (st.getProductParentURN().length() > 0) {
            command.add("-F");
            command.add(st.getProductParentURN());
        }
        command.add("-P");
        command.add(st.getProductParent());
        if (st.getProductDefinedInstanceID().length() > 0) {
            command.add("-I");
            command.add(st.getProductDefinedInstanceID());
        }
        command.add("-m");
        command.add(st.getProductVendor());
        command.add("-A");
        command.add(st.getPlatformArch());
        command.add("-z");
        command.add(st.getContainer());
        command.add("-S");
        command.add(st.getSource());
        BufferedReader in = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            String output = commandOutput(p);
            if (isVerbose()) {
                System.out.println("Output from stclient -a command:");
                System.out.println(output);
            }
            String urn = "";
            if (p.exitValue() == 0) {
                in = new BufferedReader(new StringReader(output));
                String line = null;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith(INSTANCE_URN_DESC)) {
                        urn = line.substring(INSTANCE_URN_DESC.length());
                        break;
                    }
                }
                if (urn.length() == 0) {
                    throw new IOException("Error in creating service tag:\n" +
                        output);
                }
                return getServiceTag(urn);
            } else {
                return checkReturnError(p.exitValue(), output, st);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    public ServiceTag removeServiceTag(String instanceURN) throws IOException {
        ServiceTag st = getServiceTag(instanceURN);
        if (st == null) {
            return null;
        }
        List<String> command = getCommandList();
        command.add("-d");
        command.add("-i");
        command.add(instanceURN);
        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();
        String output = commandOutput(p);
        if (isVerbose()) {
            System.out.println("Output from stclient -d command:");
            System.out.println(output);
        }
        if (p.exitValue() == 0) {
            return st;
        } else {
            return checkReturnError(p.exitValue(), output, st);
        }
    }
    public ServiceTag updateServiceTag(String instanceURN,
                                       String productDefinedInstanceID)
            throws IOException {
        ServiceTag svcTag = getServiceTag(instanceURN);
        if (svcTag == null) {
            return null;
        }
        List<String> command = getCommandList();
        command.add("-u");
        command.add("-i");
        command.add(instanceURN);
        command.add("-I");
        if (productDefinedInstanceID.length() > 0) {
            command.add(productDefinedInstanceID);
        } else {
            command.add("\"\"");
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();
        String output = commandOutput(p);
        if (isVerbose()) {
            System.out.println("Output from stclient -u command:");
            System.out.println(output);
        }
        if (p.exitValue() == 0) {
            return getServiceTag(instanceURN);
        } else {
            return checkReturnError(p.exitValue(), output, svcTag);
        }
    }
    public ServiceTag getServiceTag(String instanceURN) throws IOException {
        if (instanceURN == null) {
            throw new NullPointerException("instanceURN is null");
        }
        List<String> command = getCommandList();
        command.add("-g");
        command.add("-i");
        command.add(instanceURN);
        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();
        String output = commandOutput(p);
        if (isVerbose()) {
            System.out.println("Output from stclient -g command:");
            System.out.println(output);
        }
        if (p.exitValue() == 0) {
            return parseServiceTag(output);
        } else {
            return checkReturnError(p.exitValue(), output, null);
        }
    }
    private ServiceTag parseServiceTag(String output) throws IOException {
        BufferedReader in = null;
        try {
            Properties props = new Properties();
            in = new BufferedReader(new StringReader(output));
            String line = null;
            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() > 0) {
                    String[] ss = line.trim().split("=", 2);
                    if (ss.length == 2) {
                        props.setProperty(ss[0].trim(), ss[1].trim());
                    } else {
                        props.setProperty(ss[0].trim(), "");
                    }
                }
            }
            String urn = props.getProperty(ST_NODE_INSTANCE_URN);
            String productName = props.getProperty(ST_NODE_PRODUCT_NAME);
            String productVersion = props.getProperty(ST_NODE_PRODUCT_VERSION);
            String productURN = props.getProperty(ST_NODE_PRODUCT_URN);
            String productParent = props.getProperty(ST_NODE_PRODUCT_PARENT);
            String productParentURN = props.getProperty(ST_NODE_PRODUCT_PARENT_URN);
            String productDefinedInstanceID =
                props.getProperty(ST_NODE_PRODUCT_DEFINED_INST_ID);
            String productVendor = props.getProperty(ST_NODE_PRODUCT_VENDOR);
            String platformArch = props.getProperty(ST_NODE_PLATFORM_ARCH);
            String container = props.getProperty(ST_NODE_CONTAINER);
            String source = props.getProperty(ST_NODE_SOURCE);
            int installerUID =
                Util.getIntValue(props.getProperty(ST_NODE_INSTALLER_UID));
            Date timestamp =
                Util.parseTimestamp(props.getProperty(ST_NODE_TIMESTAMP));
            return new ServiceTag(urn,
                                  productName,
                                  productVersion,
                                  productURN,
                                  productParent,
                                  productParentURN,
                                  productDefinedInstanceID,
                                  productVendor,
                                  platformArch,
                                  container,
                                  source,
                                  installerUID,
                                  timestamp);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    public Set<ServiceTag> findServiceTags(String productURN) throws IOException {
        if (productURN == null) {
            throw new NullPointerException("productURN is null");
        }
        List<String> command = getCommandList();
        command.add("-f");
        command.add("-t");
        command.add(productURN);
        BufferedReader in = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            String output = commandOutput(p);
            Set<ServiceTag> instances = new HashSet<ServiceTag>();
            if (p.exitValue() == 0) {
                in = new BufferedReader(new StringReader(output));
                String line = null;
                while ((line = in.readLine()) != null) {
                    String s = line.trim();
                    if (s.startsWith("urn:st:")) {
                        instances.add(getServiceTag(s));
                    }
                }
            } else {
                checkReturnError(p.exitValue(), output, null);
            }
            return instances;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
