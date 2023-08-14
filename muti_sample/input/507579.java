public final class CodeStatistics {
    private static final boolean DEBUG = false;
    public static int runningDeltaRegisters = 0;
    public static int runningDeltaInsns = 0;
    public static int runningTotalInsns = 0;
    public static int dexRunningDeltaRegisters = 0;
    public static int dexRunningDeltaInsns = 0;
    public static int dexRunningTotalInsns = 0;
    public static int runningOriginalBytes = 0;
    private CodeStatistics() {
    }
    public static void updateOriginalByteCount(int count) {
        runningOriginalBytes += count;
    }
    public static void updateDexStatistics(DalvCode nonOptCode,
            DalvCode code) {
        if (DEBUG) {
            System.err.println("dex insns (old/new) "
                    + nonOptCode.getInsns().codeSize()
                    + "/" + code.getInsns().codeSize()
                    + " regs (o/n) "
                    + nonOptCode.getInsns().getRegistersSize()
                    + "/" + code.getInsns().getRegistersSize()
            );
        }
        dexRunningDeltaInsns
            += (code.getInsns().codeSize()
                - nonOptCode.getInsns().codeSize());
        dexRunningDeltaRegisters
            += (code.getInsns().getRegistersSize()
                - nonOptCode.getInsns().getRegistersSize());
        dexRunningTotalInsns += code.getInsns().codeSize();
    }
    public static void updateRopStatistics(RopMethod nonOptRmeth,
            RopMethod rmeth) {
        int oldCountInsns
                = nonOptRmeth.getBlocks().getEffectiveInstructionCount();
        int oldCountRegs = nonOptRmeth.getBlocks().getRegCount();
        if (DEBUG) {
            System.err.println("insns (old/new): "
                    + oldCountInsns + "/"
                    + rmeth.getBlocks().getEffectiveInstructionCount()
                    + " regs (o/n):" + oldCountRegs
                    + "/"  +  rmeth.getBlocks().getRegCount());
        }
        int newCountInsns
                = rmeth.getBlocks().getEffectiveInstructionCount();
        runningDeltaInsns
            += (newCountInsns - oldCountInsns);
        runningDeltaRegisters
            += (rmeth.getBlocks().getRegCount() - oldCountRegs);
        runningTotalInsns += newCountInsns;
    }
    public static void dumpStatistics(PrintStream out) {
        out.printf("Optimizer Delta Rop Insns: %d total: %d "
                + "(%.2f%%) Delta Registers: %d\n",
                runningDeltaInsns,
                runningTotalInsns,
                (100.0 * (((float) runningDeltaInsns)
                        / (runningTotalInsns + Math.abs(runningDeltaInsns)))),
                runningDeltaRegisters);
        out.printf("Optimizer Delta Dex Insns: Insns: %d total: %d "
                + "(%.2f%%) Delta Registers: %d\n",
                dexRunningDeltaInsns,
                dexRunningTotalInsns,
                (100.0 * (((float) dexRunningDeltaInsns)
                        / (dexRunningTotalInsns
                                + Math.abs(dexRunningDeltaInsns)))),
                dexRunningDeltaRegisters);
        out.printf("Original bytecode byte count: %d\n",
                runningOriginalBytes);
    }
}
