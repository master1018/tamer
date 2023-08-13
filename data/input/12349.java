public class HeapDumper extends Tool {
    private static String DEFAULT_DUMP_FILE = "heap.bin";
    private String dumpFile;
    public HeapDumper(String dumpFile) {
        this.dumpFile = dumpFile;
    }
    protected void printFlagsUsage() {
        System.out.println("    <no option>\tto dump heap to " +
            DEFAULT_DUMP_FILE);
        System.out.println("    -f <file>\tto dump heap to <file>");
        super.printFlagsUsage();
    }
    public void run() {
        System.out.println("Dumping heap to " + dumpFile + " ...");
        try {
            new HeapHprofBinWriter().write(dumpFile);
            System.out.println("Heap dump file created");
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
    public static void main(String args[]) {
        String file = DEFAULT_DUMP_FILE;
        if (args.length > 2) {
            if (args[0].equals("-f")) {
                file = args[1];
                String[] newargs = new String[args.length-2];
                System.arraycopy(args, 2, newargs, 0, args.length-2);
                args = newargs;
            }
        }
        HeapDumper dumper = new HeapDumper(file);
        dumper.start(args);
        dumper.stop();
    }
}
