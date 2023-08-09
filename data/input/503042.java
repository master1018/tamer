public abstract class BaseMachine implements Machine {
    private final Prototype prototype;
    private TypeBearer[] args;
    private int argCount;
    private Type auxType;
    private int auxInt;
    private Constant auxCst;
    private int auxTarget;
    private SwitchList auxCases;
    private ArrayList<Constant> auxInitValues;
    private int localIndex;
    private RegisterSpec localTarget;
    private TypeBearer[] results;
    private int resultCount;
    public BaseMachine(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        this.prototype = prototype;
        args = new TypeBearer[10];
        results = new TypeBearer[6];
        clearArgs();
    }
    public Prototype getPrototype() {
        return prototype;
    }
    public final void clearArgs() {
        argCount = 0;
        auxType = null;
        auxInt = 0;
        auxCst = null;
        auxTarget = 0;
        auxCases = null;
        auxInitValues = null;
        localIndex = -1;
        localTarget = null;
        resultCount = -1;
    }
    public final void popArgs(Frame frame, int count) {
        ExecutionStack stack = frame.getStack();
        clearArgs();
        if (count > args.length) {
            args = new TypeBearer[count + 10];
        }
        for (int i = count - 1; i >= 0; i--) {
            args[i] = stack.pop();
        }
        argCount = count;
    }
    public void popArgs(Frame frame, Prototype prototype) {
        StdTypeList types = prototype.getParameterTypes();
        int size = types.size();
        popArgs(frame, size);
        for (int i = 0; i < size; i++) {
            if (! Merger.isPossiblyAssignableFrom(types.getType(i), args[i])) {
                throw new SimException("at stack depth " + (size - 1 - i) +
                        ", expected type " + types.getType(i).toHuman() +
                        " but found " + args[i].getType().toHuman());
            }
        }
    }
    public final void popArgs(Frame frame, Type type) {
        popArgs(frame, 1);
        if (! Merger.isPossiblyAssignableFrom(type, args[0])) {
            throw new SimException("expected type " + type.toHuman() +
                    " but found " + args[0].getType().toHuman());
        }
    }
    public final void popArgs(Frame frame, Type type1, Type type2) {
        popArgs(frame, 2);
        if (! Merger.isPossiblyAssignableFrom(type1, args[0])) {
            throw new SimException("expected type " + type1.toHuman() +
                    " but found " + args[0].getType().toHuman());
        }
        if (! Merger.isPossiblyAssignableFrom(type2, args[1])) {
            throw new SimException("expected type " + type2.toHuman() +
                    " but found " + args[1].getType().toHuman());
        }
    }
    public final void popArgs(Frame frame, Type type1, Type type2,
            Type type3) {
        popArgs(frame, 3);
        if (! Merger.isPossiblyAssignableFrom(type1, args[0])) {
            throw new SimException("expected type " + type1.toHuman() +
                    " but found " + args[0].getType().toHuman());
        }
        if (! Merger.isPossiblyAssignableFrom(type2, args[1])) {
            throw new SimException("expected type " + type2.toHuman() +
                    " but found " + args[1].getType().toHuman());
        }
        if (! Merger.isPossiblyAssignableFrom(type3, args[2])) {
            throw new SimException("expected type " + type2.toHuman() +
                    " but found " + args[2].getType().toHuman());
        }
    }
    public final void localArg(Frame frame, int idx) {
        clearArgs();
        args[0] = frame.getLocals().get(idx);
        argCount = 1;
        localIndex = idx;
    }
    public final void auxType(Type type) {
        auxType = type;
    }
    public final void auxIntArg(int value) {
        auxInt = value;
    }
    public final void auxCstArg(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        auxCst = cst;
    }
    public final void auxTargetArg(int target) {
        auxTarget = target;
    }
    public final void auxSwitchArg(SwitchList cases) {
        if (cases == null) {
            throw new NullPointerException("cases == null");
        }
        auxCases = cases;
    }
    public final void auxInitValues(ArrayList<Constant> initValues) {
        auxInitValues = initValues;
    }
    public final void localTarget(int idx, Type type, LocalItem local) {
        localTarget = RegisterSpec.makeLocalOptional(idx, type, local);
    }
    protected final int argCount() {
        return argCount;
    }
    protected final int argWidth() {
        int result = 0;
        for (int i = 0; i < argCount; i++) {
            result += args[i].getType().getCategory();
        }
        return result;
    }
    protected final TypeBearer arg(int n) {
        if (n >= argCount) {
            throw new IllegalArgumentException("n >= argCount");
        }
        try {
            return args[n];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("n < 0");
        }
    }
    protected final Type getAuxType() {
        return auxType;
    }
    protected final int getAuxInt() {
        return auxInt;
    }
    protected final Constant getAuxCst() {
        return auxCst;
    }
    protected final int getAuxTarget() {
        return auxTarget;
    }
    protected final SwitchList getAuxCases() {
        return auxCases;
    }
    protected final ArrayList<Constant> getInitValues() {
        return auxInitValues;
    }
    protected final int getLocalIndex() {
        return localIndex;
    }
    protected final RegisterSpec getLocalTarget() {
        if (localTarget == null) {
            return null;
        }
        if (resultCount != 1) {
            throw new SimException("local target with " + 
                    ((resultCount == 0) ? "no" : "multiple") + " results");
        }
        TypeBearer result = results[0];
        Type resultType = result.getType();
        Type localType = localTarget.getType();
        if (resultType == localType) {
            return localTarget;
        }
        if (! Merger.isPossiblyAssignableFrom(localType, resultType)) {
            throwLocalMismatch(resultType, localType);
            return null;
        }
        if (localType == Type.OBJECT) {
            localTarget = localTarget.withType(result);
        }
        return localTarget;
    }
    protected final void clearResult() {
        resultCount = 0;
    }
    protected final void setResult(TypeBearer result) {
        if (result == null) {
            throw new NullPointerException("result == null");
        }
        results[0] = result;
        resultCount = 1;
    }
    protected final void addResult(TypeBearer result) {
        if (result == null) {
            throw new NullPointerException("result == null");
        }
        results[resultCount] = result;
        resultCount++;
    }
    protected final int resultCount() {
        if (resultCount < 0) {
            throw new SimException("results never set");
        }
        return resultCount;
    }
    protected final int resultWidth() {
        int width = 0;
        for (int i = 0; i < resultCount; i++) {
            width += results[i].getType().getCategory();
        }
        return width;
    }
    protected final TypeBearer result(int n) {
        if (n >= resultCount) {
            throw new IllegalArgumentException("n >= resultCount");
        }
        try {
            return results[n];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("n < 0");
        }
    }
    protected final void storeResults(Frame frame) {
        if (resultCount < 0) {
            throw new SimException("results never set");
        }
        if (resultCount == 0) {
            return;
        }
        if (localTarget != null) {
            frame.getLocals().set(getLocalTarget());
        } else {
            ExecutionStack stack = frame.getStack();
            for (int i = 0; i < resultCount; i++) {
                stack.push(results[i]);
            }
        }
    }
    public static void throwLocalMismatch(TypeBearer found,
            TypeBearer local) {
        throw new SimException("local variable type mismatch: " +
                "attempt to set or access a value of type " +
                found.toHuman() + 
                " using a local variable of type " + 
                local.toHuman() +
                ". This is symptomatic of .class transformation tools " +
                "that ignore local variable information.");
    }
}
