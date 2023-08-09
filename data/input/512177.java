public class ConcreteSub extends AbstractBase {
    private static void callBase(AbstractBase abs) {
        System.out.println("calling abs.doStuff()");
        abs.doStuff();
    }
    public static void main() {
        ConcreteSub sub = new ConcreteSub();
        try {
            callBase(sub);
        } catch (AbstractMethodError ame) {
            System.out.println("Got expected exception from abs.doStuff().");
        }
        Class absClass = AbstractBase.class;
        Method meth;
        System.out.println("class modifiers=" + absClass.getModifiers());
        try {
            meth = absClass.getMethod("redefineMe", (Class[]) null);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            return;
        }
        System.out.println("meth modifiers=" + meth.getModifiers());
    }
}
