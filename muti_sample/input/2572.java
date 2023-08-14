@ann1(isStatic=false,type="class" ,)
class TestM4 {
    @ann1(type="class",isStatic=false,) public void myMethod() {
        System.out.println(" In side the myMethod");
    }
}
@interface ann1 {
    String type();
    boolean isStatic();
}
