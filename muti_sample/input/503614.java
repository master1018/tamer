public class CallVerificationStack extends Stack<Object> {
    private static final long serialVersionUID = 1L;
    private static final CallVerificationStack _instance = new CallVerificationStack();
    private final Stack<StackTraceElement> callStack = new Stack<StackTraceElement>();
    private CallVerificationStack() {
    }
    public static CallVerificationStack getInstance() {
        return _instance;
    }
    private void pushCallStack() {
        StackTraceElement[] eles = (new Throwable()).getStackTrace();
        int i;
        for (i = 1; i < eles.length; i++) {
            if (!eles[i].getClassName().equals(this.getClass().getName())) {
                break;
            }
        }
        this.callStack.push(eles[i]);
    }
    public String getCurrentSourceClass() {
        return this.callStack.peek().getClassName();
    }
    public String getCurrentSourceMethod() {
        return this.callStack.peek().getMethodName();
    }
    @Override
    public void clear() {
        this.callStack.clear();
        super.clear();
    }
    @Override
    public Object push(Object o) {
        pushCallStack();
        return super.push(o);
    }
    public void push(boolean val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(char val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(double val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(float val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(int val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(long val) {
        this.push(new BaseTypeWrapper(val));
    }
    public void push(short val) {
        this.push(new BaseTypeWrapper(val));
    }
    @Override
    public Object pop() {
        this.callStack.pop();
        return super.pop();
    }
    public boolean popBoolean() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Boolean value = (Boolean) wrapper.getValue();
        return value.booleanValue();
    }
    public char popChar() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Character value = (Character) wrapper.getValue();
        return value.charValue();
    }
    public double popDouble() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Double value = (Double) wrapper.getValue();
        return value.doubleValue();
    }
    public float popFloat() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Float value = (Float) wrapper.getValue();
        return value.floatValue();
    }
    public int popInt() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Integer value = (Integer) wrapper.getValue();
        return value.intValue();
    }
    public long popLong() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Long value = (Long) wrapper.getValue();
        return value.longValue();
    }
    public short popShort() {
        BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
        Short value = (Short) wrapper.getValue();
        return value.shortValue();
    }
    class BaseTypeWrapper {
        private Object value;
        public BaseTypeWrapper(boolean val) {
            this.value = new Boolean(val);
        }
        public BaseTypeWrapper(byte val) {
            this.value = new Byte(val);
        }
        public BaseTypeWrapper(char val) {
            this.value = new Character(val);
        }
        public BaseTypeWrapper(double val) {
            this.value = new Double(val);
        }
        public BaseTypeWrapper(float val) {
            this.value = new Float(val);
        }
        public BaseTypeWrapper(int val) {
            this.value = new Integer(val);
        }
        public BaseTypeWrapper(long val) {
            this.value = new Long(val);
        }
        public BaseTypeWrapper(short val) {
            this.value = new Short(val);
        }
        public Object getValue() {
            return this.value;
        }
    }
}
