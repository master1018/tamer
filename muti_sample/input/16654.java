public class LogCompilation extends DefaultHandler implements ErrorHandler, Constants {
    public static void usage(int exitcode) {
        System.out.println("Usage: LogCompilation [ -v ] [ -c ] [ -s ] [ -e | -N ] file1 ...");
        System.out.println("  -c:   clean up malformed 1.5 xml");
        System.out.println("  -i:   print inlining decisions");
        System.out.println("  -S:   print compilation statistics");
        System.out.println("  -s:   sort events by start time");
        System.out.println("  -e:   sort events by elapsed time");
        System.out.println("  -N:   sort events by name and start");
        System.exit(exitcode);
    }
    public static void main(String[] args) throws Exception {
        Comparator<LogEvent> defaultSort = LogParser.sortByStart;
        boolean statistics = false;
        boolean printInlining = false;
        boolean cleanup = false;
        int index = 0;
        while (args.length > index) {
            if (args[index].equals("-e")) {
                defaultSort = LogParser.sortByElapsed;
                index++;
            } else if (args[index].equals("-n")) {
                defaultSort = LogParser.sortByNameAndStart;
                index++;
            } else if (args[index].equals("-s")) {
                defaultSort = LogParser.sortByStart;
                index++;
            } else if (args[index].equals("-c")) {
                cleanup = true;
                index++;
            } else if (args[index].equals("-S")) {
                statistics = true;
                index++;
            } else if (args[index].equals("-h")) {
                usage(0);
            } else if (args[index].equals("-i")) {
                printInlining = true;
                index++;
            } else {
                break;
            }
        }
        if (index >= args.length) {
            usage(1);
        }
        while (index < args.length) {
            ArrayList<LogEvent> events = LogParser.parse(args[index], cleanup);
            if (statistics) {
                printStatistics(events, System.out);
            } else {
                Collections.sort(events, defaultSort);
                for (LogEvent c : events) {
                    if (printInlining && c instanceof Compilation) {
                        Compilation comp = (Compilation)c;
                        comp.print(System.out, true);
                    } else {
                        c.print(System.out);
                    }
                }
            }
            index++;
        }
    }
    public static void printStatistics(ArrayList<LogEvent> events, PrintStream out) {
        long cacheSize = 0;
        long maxCacheSize = 0;
        int nmethodsCreated = 0;
        int nmethodsLive = 0;
        int[] attempts = new int[32];
        double regallocTime = 0;
        int maxattempts = 0;
        LinkedHashMap<String, Double> phaseTime = new LinkedHashMap<String, Double>(7);
        LinkedHashMap<String, Integer> phaseNodes = new LinkedHashMap<String, Integer>(7);
        double elapsed = 0;
        for (LogEvent e : events) {
            if (e instanceof Compilation) {
                Compilation c = (Compilation) e;
                c.printShort(out);
                out.printf(" %6.4f\n", c.getElapsedTime());
                attempts[c.getAttempts()]++;
                maxattempts = Math.max(maxattempts,c.getAttempts());
                elapsed += c.getElapsedTime();
                for (Phase phase : c.getPhases()) {
                    out.printf("\t%s %6.4f\n", phase.getName(), phase.getElapsedTime());
                    Double v = phaseTime.get(phase.getName());
                    if (v == null) {
                        v = Double.valueOf(0.0);
                    }
                    phaseTime.put(phase.getName(), Double.valueOf(v.doubleValue() + phase.getElapsedTime()));
                    Integer v2 = phaseNodes.get(phase.getName());
                    if (v2 == null) {
                        v2 = Integer.valueOf(0);
                    }
                    phaseNodes.put(phase.getName(), Integer.valueOf(v2.intValue() + phase.getNodes()));
                }
            } else if (e instanceof MakeNotEntrantEvent) {
                MakeNotEntrantEvent mne = (MakeNotEntrantEvent) e;
                NMethod nm = mne.getNMethod();
                if (mne.isZombie()) {
                    if (nm == null) {
                        System.err.println(mne.getId());
                    }
                    cacheSize -= nm.getSize();
                    nmethodsLive--;
                }
            } else if (e instanceof NMethod) {
                nmethodsLive++;
                nmethodsCreated++;
                NMethod nm = (NMethod) e;
                cacheSize += nm.getSize();
                maxCacheSize = Math.max(cacheSize, maxCacheSize);
            }
        }
        out.printf("NMethods: %d created %d live %d bytes (%d peak) in the code cache\n",
                          nmethodsCreated, nmethodsLive, cacheSize, maxCacheSize);
        out.println("Phase times:");
        for (String name : phaseTime.keySet()) {
            Double v = phaseTime.get(name);
            Integer v2 = phaseNodes.get(name);
            out.printf("%20s %6.4f %d\n", name, v.doubleValue(), v2.intValue());
        }
        out.printf("%20s %6.4f\n", "total", elapsed);
        if (maxattempts > 0) {
            out.println("Distribution of regalloc passes:");
            for (int i = 0; i <= maxattempts; i++) {
                out.printf("%2d %8d\n", i, attempts[i]);
            }
        }
    }
}
