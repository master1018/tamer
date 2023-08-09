public class T_athrow_12 {
    public boolean run() {
        try{
            try {
                throw new RuntimeException();
            }catch(RuntimeException e1) {
                return true;
            }
        } catch(RuntimeException e2) {
        }
        return false;
    }
}
