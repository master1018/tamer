abstract class MySuper implements MyInterface {
}
class sagtarg extends MySuper {
    public static void main(String[] args){
        String stringVar = "localVar1";
        int    intVar = 89;
        List<String> genVar = null;
        System.out.println("Howdy!");
        String myStr = "";
        synchronized(myStr) {
            try {
                myStr.wait();
            } catch (InterruptedException ee) {
            }
        }
        System.out.println("Goodbye from sagtarg!");
    }
    public void myMethod() {
    }
}
