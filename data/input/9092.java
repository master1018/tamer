class CheckDataVersion {
    static final String datafile = "tablea1.txt";
    static final String FILEVERSIONKEY = "FILEVERSION=";
    static final String DATAVERSIONKEY = "DATAVERSION=";
    static String fileVersion;
    static String dataVersion;
    static boolean checked = false;
    static void check() {
        if (!checked) {
            try {
                FileReader fr = new FileReader(new File(System.getProperty("test.src", "."), datafile));
                BufferedReader in = new BufferedReader(fr);
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith(FILEVERSIONKEY)) {
                        fileVersion = line.substring(FILEVERSIONKEY.length());
                    }
                    if (line.startsWith(DATAVERSIONKEY)) {
                        dataVersion = line.substring(DATAVERSIONKEY.length());
                    }
                    if (fileVersion != null && dataVersion != null) {
                        break;
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        String sep = File.separator;
                        DataInputStream dis = new DataInputStream(new FileInputStream(System.getProperty("java.home")+sep+"lib"+sep+"currency.data"));
                        int magic = dis.readInt();
                        if (magic != 0x43757244) {
                            throw new RuntimeException("The magic number in the JRE's currency data is incorrect.  Expected: 0x43757244, Got: 0x"+magic);
                        }
                        int fileVerNumber = dis.readInt();
                        int dataVerNumber = dis.readInt();
                        if (Integer.parseInt(fileVersion) != fileVerNumber ||
                            Integer.parseInt(dataVersion) != dataVerNumber) {
                            throw new RuntimeException("Test data and JRE's currency data are inconsistent.  test: (file: "+fileVersion+" data: "+dataVersion+"), JRE: (file: "+fileVerNumber+" data: "+dataVerNumber+")");
                        }
System.out.println("test: (file: "+fileVersion+" data: "+dataVersion+"), JRE: (file: "+fileVerNumber+" data: "+dataVerNumber+")");
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                    return null;
                }
            });
            checked = true;
        }
    }
}
