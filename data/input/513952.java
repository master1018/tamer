public class PrintCsv {
    public static void main(String[] args)
            throws IOException, ClassNotFoundException {
        if (args.length != 1) {
            System.err.println("Usage: PrintCsv [compiled log file]");
            System.exit(0);
        }
        Root root = Root.fromFile(args[0]);
        printHeaders(System.out);
        MemoryUsage baseline = MemoryUsage.baseline();
        for (LoadedClass loadedClass : root.loadedClasses.values()) {
            if (!loadedClass.systemClass) {
                continue;
            }
            printRow(System.out, baseline, loadedClass);
        }
    }
    static void printHeaders(PrintStream out) {
        out.println("Name"
                + ",Preloaded"
                + ",Median Load Time (us)"
                + ",Median Init Time (us)"
                + ",Process Names"
                + ",Load Count"
                + ",Init Count"
                + ",Managed Heap (B)"
                + ",Native Heap (B)"
                + ",Managed Pages (kB)"
                + ",Native Pages (kB)"
                + ",Other Pages (kB)");
    }
    static void printRow(PrintStream out, MemoryUsage baseline,
            LoadedClass loadedClass) {
        out.print(loadedClass.name);
        out.print(',');
        out.print(loadedClass.preloaded);
        out.print(',');
        out.print(loadedClass.medianLoadTimeMicros());
        out.print(',');
        out.print(loadedClass.medianInitTimeMicros());
        out.print(',');
        out.print('"');
        Set<String> procNames = new TreeSet<String>();
        for (Operation op : loadedClass.loads)
            procNames.add(op.process.name);
        for (Operation op : loadedClass.initializations)
            procNames.add(op.process.name);
        if (procNames.size() <= 3) {
            for (String name : procNames) {
                out.print(name + "\n");
            }
        } else {
            Iterator<String> i = procNames.iterator();
            out.print(i.next() + "\n");
            out.print(i.next() + "\n");
            out.print("...and " + (procNames.size() - 2)
                    + " others.");
        }
        out.print('"');
        out.print(',');
        out.print(loadedClass.loads.size());
        out.print(',');
        out.print(loadedClass.initializations.size());
        if (loadedClass.memoryUsage.isAvailable()) {
            MemoryUsage subtracted
                    = loadedClass.memoryUsage.subtract(baseline);
            out.print(',');
            out.print(subtracted.javaHeapSize());
            out.print(',');
            out.print(subtracted.nativeHeapSize);
            out.print(',');
            out.print(subtracted.javaPagesInK());
            out.print(',');
            out.print(subtracted.nativePagesInK());
            out.print(',');
            out.print(subtracted.otherPagesInK());
        } else {
            out.print(",n/a,n/a,n/a,n/a,n/a");
        }
        out.println();
    }
}
