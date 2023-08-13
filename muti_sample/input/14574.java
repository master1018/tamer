public class Test4132698 extends BeanContextMembershipEvent {
    public static void main(String[] args) throws Exception {
        BeanContextSupport bcs = new BeanContextSupport();
        try {
            new Test4132698(bcs, null);
        }
        catch (NullPointerException exception) {
            return; 
        }
        catch (Exception exception) {
            throw new Error("Should have caught NullPointerException but caught something else.", exception);
        }
        throw new Error("Failure to catch null changes argument.");
    }
    public Test4132698(BeanContext bc, Object[] objects) {
        super(bc, objects);
    }
}
