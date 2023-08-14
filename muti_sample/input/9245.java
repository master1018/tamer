class ConstantPoolTarg {
    public static void main(String[] args){
        System.out.println("Howdy!"); 
    }
}
public class ConstantPoolInfo extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    int cpool_count;
    byte[] cpbytes;
    static int expectedMajorVersion;
    static int expectedMinorVersion;
    static int expectedCpoolCount;
    public static final int JAVA_MAGIC                   = 0xcafebabe;
    public static final int CONSTANT_UTF8                = 1;
    public static final int CONSTANT_UNICODE             = 2;
    public static final int CONSTANT_INTEGER             = 3;
    public static final int CONSTANT_FLOAT               = 4;
    public static final int CONSTANT_LONG                = 5;
    public static final int CONSTANT_DOUBLE              = 6;
    public static final int CONSTANT_CLASS               = 7;
    public static final int CONSTANT_STRING              = 8;
    public static final int CONSTANT_FIELD               = 9;
    public static final int CONSTANT_METHOD              = 10;
    public static final int CONSTANT_INTERFACEMETHOD     = 11;
    public static final int CONSTANT_NAMEANDTYPE         = 12;
    ConstantPoolInfo (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new ConstantPoolInfo(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ConstantPoolTarg");
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        String targPathname = System.getProperty("test.classes") + File.separator + "ConstantPoolTarg.class";
        readClassData(new FileInputStream(targPathname));
        if (vm().canGetClassFileVersion()) {
            if (expectedMajorVersion != targetClass.majorVersion()) {
                failure("unexpected major version: actual value: " + targetClass.majorVersion()
                        + "expected value :" + expectedMajorVersion);
            }
            if (expectedMinorVersion != targetClass.minorVersion()) {
                failure("unexpected minor version: actual value: " + targetClass.minorVersion()
                        + "expected value :" + expectedMinorVersion);
            }
        } else {
            System.out.println("can get class version not supported");
        }
        if (vm().canGetConstantPool()) {
            cpool_count = targetClass.constantPoolCount();
            cpbytes = targetClass.constantPool();
            try {
                printcp();
            } catch (IOException x){
                System.out.println("IOexception reading cpool bytes " + x);
            }
            if (expectedCpoolCount != cpool_count) {
                failure("unexpected constant pool count: actual value: " + cpool_count
                        + "expected value :" + expectedCpoolCount);
            }
        } else {
            System.out.println("can get constant pool version not supported");
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("ConstantPoolInfo: passed");
        } else {
            throw new Exception("ConstantPoolInfo: failed");
        }
    }
    public void printcp() throws IOException {
        boolean found = false;
        ByteArrayInputStream bytesStream = new ByteArrayInputStream(cpbytes);
        DataInputStream in = new DataInputStream(bytesStream);
        for (int i = 1; i < cpool_count; i++) {
            int tag = in.readByte();
            System.out.print("const #" + i + ":   ");
            switch(tag) {
                    case CONSTANT_UTF8:
                    String str=in.readUTF();
                    System.out.println("Asciz " + str);
                    if (str.compareTo("Howdy!") == 0) {
                        found = true;
                    }
                    break;
                case CONSTANT_INTEGER:
                    System.out.println("int " + in.readInt());
                    break;
                case CONSTANT_FLOAT:
                    System.out.println("Float " + in.readFloat());
                    break;
                case CONSTANT_LONG:
                    System.out.println("Long " + in.readLong());
                    break;
                case CONSTANT_DOUBLE:
                    System.out.println("Double " + in.readDouble());
                    break;
                case CONSTANT_CLASS:
                    System.out.println("Class " + in.readUnsignedShort());
                    break;
                case CONSTANT_STRING:
                    System.out.println("String " + in.readUnsignedShort());
                    break;
                case CONSTANT_FIELD:
                    System.out.println("Field " + in.readUnsignedShort() + " " + in.readUnsignedShort());
                    break;
                case CONSTANT_METHOD:
                    System.out.println("Method " + in.readUnsignedShort() + " " + in.readUnsignedShort());
                    break;
                case CONSTANT_INTERFACEMETHOD:
                    System.out.println("InterfaceMethod " + in.readUnsignedShort() + " " + in.readUnsignedShort());
                    break;
                case CONSTANT_NAMEANDTYPE:
                    System.out.println("NameAndType " + in.readUnsignedShort() + " " + in.readUnsignedShort());
                    break;
                case 0:
                default:
                    System.out.println("class format error");
            }
        }
        if (!found) {
            failure("expected string \"Howdy!\" not found in constant pool");
        }
    }
    void readClassData(InputStream infile){
        try{
            this.read(new DataInputStream(infile));
        }catch (FileNotFoundException ee) {
            failure("cant read file");
        }catch (Error ee) {
            ee.printStackTrace();
            failure("fatal error");
        } catch (Exception ee) {
            ee.printStackTrace();
            failure("fatal exception");
        }
    }
    public void read(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != JAVA_MAGIC) {
            failure("fatal bad class file format");
        }
        expectedMinorVersion = in.readShort();;
        expectedMajorVersion = in.readShort();
        expectedCpoolCount = in.readUnsignedShort();
        in.close();
    } 
}
