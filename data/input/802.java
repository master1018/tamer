public class IntegerSubInstruction extends PrimitiveInstruction {
    public IntegerSubInstruction(ImageObjectManager manager) {
        super(manager);
    }
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Process process) {
        MethodContext mc = (MethodContext) process.getActiveContext();
        SILPrimitiveObject<Integer> i1 = (SILPrimitiveObject<Integer>) mc.getReceiver();
        SILPrimitiveObject<Integer> i2 = (SILPrimitiveObject<Integer>) mc.pop();
        SILObject result = manager.newInteger(i1.value - i2.value);
        mc.push(result);
    }
}
