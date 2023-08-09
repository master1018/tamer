public class T_opc_throw_12 {
    public boolean run() {
        try{
            return test();
        } catch(RuntimeException e2) {
        }
        return false;
    }
    private boolean test() {
        try {
            throw new RuntimeException();
        } catch(RuntimeException e1) {
            return true;
        }
    }
}
