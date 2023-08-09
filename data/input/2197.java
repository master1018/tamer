public class SvcTagClient {
    private static File xmlFile;
    private static RegistrationData registration;
    private static String instanceURN;
    private static String productName;
    private static String productVersion;
    private static String productURN;
    private static String productParent;
    private static String productParentURN = ""; 
    private static String productDefinedInstanceID = ""; 
    private static String productVendor;
    private static String platformArch;
    private static String container;
    private static String source;
    private static final int ST_ERR_REC_NOT_FOUND = 225;
    public static void main(String[] args) throws Exception {
        String path = System.getProperty("stclient.registry.path");
        if (path == null) {
            System.err.println("stclient registry path missing");
            System.exit(-1);
        }
        xmlFile = new File(path);
        if (xmlFile.exists()) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(xmlFile));
            registration = RegistrationData.loadFromXML(in);
        } else {
            registration = new RegistrationData();
        }
        boolean add = false;
        boolean delete = false;
        boolean update = false;
        boolean get = false;
        boolean find = false;
        int count = 0;
        while (count < args.length) {
            String arg = args[count];
            if (!arg.startsWith("-")) {
                System.err.println("Invalid option:" + arg);
                System.exit(-1);
            }
            if (arg.equals("-a")) {
                add = true;
            } else if (arg.equals("-d")) {
                delete = true;
            } else if (arg.equals("-u")) {
                update = true;
            } else if (arg.equals("-g")) {
                get = true;
            } else if (arg.equals("-f")) {
                find = true;
                productURN = "";
            } else if (arg.equals("-t")) {
                productURN = args[++count];
            } else if (arg.equals("-i")) {
                instanceURN = args[++count];
            } else if (arg.equals("-p")) {
                productName = args[++count];
            } else if (arg.equals("-e")) {
                productVersion = args[++count];
            } else if (arg.equals("-t")) {
                productURN = args[++count];
            } else if (arg.equals("-F")) {
                productParentURN = args[++count];
            } else if (arg.equals("-P")) {
                productParent = args[++count];
            } else if (arg.equals("-I")) {
                productDefinedInstanceID = args[++count];
            } else if (arg.equals("-m")) {
                productVendor = args[++count];
            } else if (arg.equals("-A")) {
                platformArch = args[++count];
            } else if (arg.equals("-z")) {
                container = args[++count];
            } else if (arg.equals("-S")) {
                source = args[++count];
            } else {
                System.err.println("Invalid option:" + arg);
                System.exit(-1);
            }
            count++;
        }
        if (add) {
            addServiceTag();
        } else if (delete) {
            deleteServiceTag();
        } else if (update) {
            updateServiceTag();
        } else if (get) {
            getServiceTag();
        } else if (find) {
            findServiceTags();
        } else {
            System.err.println("Error");
            System.exit(-1);
        }
        updateXmlFile();
    }
    private static String OUTPUT = "Product instance URN=";
    private static void addServiceTag() {
        if (instanceURN == null) {
            instanceURN = ServiceTag.generateInstanceURN();
        }
        ServiceTag st = ServiceTag.newInstance(instanceURN,
                                               productName,
                                               productVersion,
                                               productURN,
                                               productParent,
                                               productParentURN,
                                               productDefinedInstanceID,
                                               productVendor,
                                               platformArch,
                                               container,
                                               source);
        registration.addServiceTag(st);
        System.out.println(OUTPUT + st.getInstanceURN());
    }
    private static void deleteServiceTag() {
        registration.removeServiceTag(instanceURN);
        System.out.println("instance_urn=" + instanceURN + " deleted");
    }
    private static void updateServiceTag() {
        registration.updateServiceTag(instanceURN, productDefinedInstanceID);
        System.out.println("instance_urn=" + instanceURN + " updated");
    }
    private static void getServiceTag() {
        ServiceTag st = registration.getServiceTag(instanceURN);
        if (st == null) {
            System.err.println("instance_urn=" + instanceURN + " not found");
            System.exit(ST_ERR_REC_NOT_FOUND);
        } else {
            System.out.println(st);
        }
    }
    private static void findServiceTags() {
        Set<ServiceTag> set = registration.getServiceTags();
        for (ServiceTag st : set) {
            if (st.getProductURN().equals(productURN)) {
                System.out.println(st.getInstanceURN());
            }
        }
        if (set.size() == 0) {
            System.out.println("No records found");
            System.exit(ST_ERR_REC_NOT_FOUND);
        }
    }
    private static void updateXmlFile() throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(xmlFile));
        try {
            registration.storeToXML(out);
        } finally {
            out.close();
        }
    }
}
