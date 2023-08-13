public class JSnap extends Tool {
    public void run() {
        final PrintStream out = System.out;
        if (PerfMemory.initialized()) {
            PerfDataPrologue prologue = PerfMemory.prologue();
            if (prologue.accessible()) {
                PerfMemory.iterate(new PerfMemory.PerfDataEntryVisitor() {
                        public boolean visit(PerfDataEntry pde) {
                            if (pde.supported()) {
                                out.print(pde.name());
                                out.print('=');
                                out.println(pde.valueAsString());
                            }
                            return true;
                        }
                    });
            } else {
                out.println("PerfMemory is not accessible");
            }
        } else {
            out.println("PerfMemory is not initialized");
        }
    }
    public static void main(String[] args) {
        JSnap js = new JSnap();
        js.start(args);
        js.stop();
    }
}
