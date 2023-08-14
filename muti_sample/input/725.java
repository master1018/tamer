public class MisMatch {
    static final int constant = 3;
    static int notConstant = 4;
    private static strictfp class NestedClass {
    }
    protected abstract class AbstractNestedClass {
        void myMethod() throws RuntimeException , Error {}
        abstract void myAbstractMethod();
    }
    void VarArgsMethod1(Number... num) {
        ;
    }
    void VarArgsMethod2(float f, double d, Number... num) {
        ;
    }
}
@interface Colors {
}
interface Inter {
    void interfaceMethod();
}
enum MyEnum {
    RED,
    GREEN,
    BLUE;
}
