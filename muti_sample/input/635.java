public class Compilation implements LogEvent {
    private int id;
    private boolean osr;
    private Method method;
    private CallSite call = new CallSite();
    private int osrBci;
    private String icount;
    private String bcount;
    private String special;
    private double start;
    private double end;
    private int attempts;
    private NMethod nmethod;
    private ArrayList<Phase> phases = new ArrayList<Phase>(4);
    private String failureReason;
    Compilation(int id) {
        this.id = id;
    }
    Phase getPhase(String s) {
        for (Phase p : getPhases()) {
            if (p.getName().equals(s)) {
                return p;
            }
        }
        return null;
    }
    double getRegallocTime() {
        return getPhase("regalloc").getElapsedTime();
    }
    public double getStart() {
        return start;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId());
        sb.append(" ");
        sb.append(getMethod());
        sb.append(" ");
        sb.append(getIcount());
        sb.append("+");
        sb.append(getBcount());
        sb.append("\n");
        for (CallSite site : getCall().getCalls()) {
            sb.append(site);
            sb.append("\n");
        }
        return sb.toString();
    }
    public void printShort(PrintStream stream) {
        if (getMethod() == null) {
            stream.println(getSpecial());
        } else {
            int bc = isOsr() ? getOsr_bci() : -1;
            stream.print(getId() + getMethod().decodeFlags(bc) + getMethod().format(bc));
        }
    }
    public void print(PrintStream stream) {
        print(stream, 0, false);
    }
    public void print(PrintStream stream, boolean printInlining) {
        print(stream, 0, printInlining);
    }
    public void print(PrintStream stream, int indent, boolean printInlining) {
        if (getMethod() == null) {
            stream.println(getSpecial());
        } else {
            int bc = isOsr() ? getOsr_bci() : -1;
            stream.print(getId() + getMethod().decodeFlags(bc) + getMethod().format(bc));
            stream.println();
            if (getFailureReason() != null) {
                stream.println("COMPILE FAILED " + getFailureReason());
            }
            if (printInlining && call.getCalls() != null) {
                for (CallSite site : call.getCalls()) {
                    site.print(stream, indent + 2);
                }
            }
        }
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public boolean isOsr() {
        return osr;
    }
    public void setOsr(boolean osr) {
        this.osr = osr;
    }
    public int getOsr_bci() {
        return osrBci;
    }
    public void setOsr_bci(int osrBci) {
        this.osrBci = osrBci;
    }
    public String getIcount() {
        return icount;
    }
    public void setICount(String icount) {
        this.icount = icount;
    }
    public String getBcount() {
        return bcount;
    }
    public void setBCount(String bcount) {
        this.bcount = bcount;
    }
    public String getSpecial() {
        return special;
    }
    public void setSpecial(String special) {
        this.special = special;
    }
    public void setStart(double start) {
        this.start = start;
    }
    public double getEnd() {
        return end;
    }
    public void setEnd(double end) {
        this.end = end;
    }
    public int getAttempts() {
        return attempts;
    }
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    public NMethod getNMethod() {
        return nmethod;
    }
    public void setNMethod(NMethod NMethod) {
        this.nmethod = NMethod;
    }
    public ArrayList<Phase> getPhases() {
        return phases;
    }
    public void setPhases(ArrayList<Phase> phases) {
        this.setPhases(phases);
    }
    public String getFailureReason() {
        return failureReason;
    }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    public CallSite getCall() {
        return call;
    }
    public void setCall(CallSite call) {
        this.call = call;
    }
    public double getElapsedTime() {
        return end - start;
    }
    public Compilation getCompilation() {
        return this;
    }
}
