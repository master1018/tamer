public class JMap extends Tool {
    public JMap(int m) {
        mode = m;
    }
    public JMap() {
        this(MODE_PMAP);
    }
    protected boolean needsJavaPrefix() {
        return false;
    }
    public String getName() {
        return "jmap";
    }
    protected String getCommandFlags() {
        return "-heap|-heap:format=b|-histo|-permstat|-finalizerinfo";
    }
    protected void printFlagsUsage() {
        System.out.println("    <no option>\tto print same info as Solaris pmap");
        System.out.println("    -heap\tto print java heap summary");
        System.out.println("    -heap:format=b\tto dump java heap in hprof binary format");
        System.out.println("    -histo\tto print histogram of java object heap");
        System.out.println("    -permstat\tto print permanent generation statistics");
        System.out.println("    -finalizerinfo\tto print information on objects awaiting finalization");
        super.printFlagsUsage();
    }
    public static final int MODE_HEAP_SUMMARY = 0;
    public static final int MODE_HISTOGRAM = 1;
    public static final int MODE_PERMSTAT = 2;
    public static final int MODE_PMAP = 3;
    public static final int MODE_HEAP_GRAPH_HPROF_BIN = 4;
    public static final int MODE_HEAP_GRAPH_GXL = 5;
    public static final int MODE_FINALIZERINFO = 6;
    public void run() {
        Tool tool = null;
        switch (mode) {
        case MODE_HEAP_SUMMARY:
            tool = new HeapSummary();
            break;
        case MODE_HISTOGRAM:
            tool = new ObjectHistogram();
            break;
        case MODE_PERMSTAT:
            tool = new PermStat();
            break;
        case MODE_PMAP:
            tool = new PMap();
            break;
        case MODE_HEAP_GRAPH_HPROF_BIN:
            writeHeapHprofBin();
            return;
        case MODE_HEAP_GRAPH_GXL:
            writeHeapGXL();
            return;
        case MODE_FINALIZERINFO:
            tool = new FinalizerInfo();
            break;
        default:
            usage();
            break;
       }
       tool.setAgent(getAgent());
       tool.setDebugeeType(getDebugeeType());
       tool.run();
    }
    public static void main(String[] args) {
        int mode = MODE_PMAP;
        if (args.length > 1 ) {
            String modeFlag = args[0];
            boolean copyArgs = true;
            if (modeFlag.equals("-heap")) {
                mode = MODE_HEAP_SUMMARY;
            } else if (modeFlag.equals("-histo")) {
                mode = MODE_HISTOGRAM;
            } else if (modeFlag.equals("-permstat")) {
                mode = MODE_PERMSTAT;
            } else if (modeFlag.equals("-finalizerinfo")) {
                mode = MODE_FINALIZERINFO;
            } else {
                int index = modeFlag.indexOf("-heap:format=");
                if (index != -1) {
                    String format = modeFlag.substring(1 + modeFlag.indexOf('='));
                    if (format.equals("b")) {
                        mode = MODE_HEAP_GRAPH_HPROF_BIN;
                    } else if (format.equals("x")) {
                        mode = MODE_HEAP_GRAPH_GXL;
                    } else {
                        System.err.println("unknown heap format:" + format);
                        return;
                    }
                } else {
                    copyArgs = false;
                }
            }
            if (copyArgs) {
                String[] newArgs = new String[args.length - 1];
                for (int i = 0; i < newArgs.length; i++) {
                    newArgs[i] = args[i + 1];
                }
                args = newArgs;
            }
        }
        JMap jmap = new JMap(mode);
        jmap.start(args);
        jmap.stop();
    }
    public boolean writeHeapHprofBin(String fileName) {
        try {
            HeapGraphWriter hgw = new HeapHprofBinWriter();
            hgw.write(fileName);
            System.out.println("heap written to " + fileName);
            return true;
        } catch (IOException exp) {
            System.err.println(exp.getMessage());
            return false;
        }
    }
    public boolean writeHeapHprofBin() {
        return writeHeapHprofBin("heap.bin");
    }
    private boolean writeHeapGXL(String fileName) {
        try {
            HeapGraphWriter hgw = new HeapGXLWriter();
            hgw.write(fileName);
            System.out.println("heap written to " + fileName);
            return true;
        } catch (IOException exp) {
            System.err.println(exp.getMessage());
            return false;
        }
    }
    public boolean writeHeapGXL() {
        return writeHeapGXL("heap.xml");
    }
    private int mode;
}
