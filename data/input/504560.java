public class T_jsr_w_3 {
    public int i1;
    private void setfield() {
        i1 = 1000;
    }
    public boolean run() {
       setfield();
       if(i1 == 1000)
           return true;
       return false;
    }
}
