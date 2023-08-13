public class MockFunction implements SQLite.Function{
    private StringBuffer acc = new StringBuffer();
    public static boolean getAggValueCalled = false;
    public static boolean functionCalled = false;
    public static boolean stepCalled = false;
    public static boolean lastStepCalled = false;
    public String getAggValue() {
        getAggValueCalled = true;
        return acc.toString();
    }
    public void function(FunctionContext fc, String args[]) {
        functionCalled = true;
        if (args.length > 0) {
            fc.set_result(args[0].toLowerCase());
        }
    }
    public void step(FunctionContext fc, String args[]) {
        stepCalled = true;
        for (int i = 0; i < args.length; i++) {
            acc.append(args[i]);
            acc.append(" ");
        }
    }
    public void last_step(FunctionContext fc) {
        lastStepCalled = true;
        fc.set_result(acc.toString());
    }
}