public abstract class AMD64ThreadContext implements ThreadContext {
    public static final int R15 = 0;
    public static final int R14 = 1;
    public static final int R13 = 2;
    public static final int R12 = 3;
    public static final int R11 = 4;
    public static final int R10 = 5;
    public static final int R9  = 6;
    public static final int R8  = 7;
    public static final int RDI = 8;
    public static final int RSI = 9;
    public static final int RBP = 10;
    public static final int RBX = 11;
    public static final int RDX = 12;
    public static final int RCX = 13;
    public static final int RAX = 14;
    public static final int TRAPNO = 15;
    public static final int ERR = 16;
    public static final int RIP = 17;
    public static final int CS = 18;
    public static final int RFL = 19;
    public static final int RSP = 20;
    public static final int SS = 21;
    public static final int FS = 22;
    public static final int GS = 23;
    public static final int ES = 24;
    public static final int DS = 25;
    public static final int FSBASE = 26;
    public static final int GSBASE = 27;
    public static final int NPRGREG = 28;
    private static final String[] regNames = {
        "r15",  "r14", "r13", "r12", "r11", "r10", "r9", "r8",
        "rdi",  "rsi", "rbp", "rbx", "rdx", "rcx", "rax", "trapno",
        "err",  "rip", "cs",  "rfl", "rsp", "ss",  "fs", "gs",
        "es",   "ds",  "fsbase", "gsbase"
    };
    private long[] data;
    public AMD64ThreadContext() {
        data = new long[NPRGREG];
    }
    public int getNumRegisters() {
        return NPRGREG;
    }
    public String getRegisterName(int index) {
        return regNames[index];
    }
    public void setRegister(int index, long value) {
        data[index] = value;
    }
    public long getRegister(int index) {
        return data[index];
    }
    public abstract void setRegisterAsAddress(int index, Address value);
    public abstract Address getRegisterAsAddress(int index);
}
