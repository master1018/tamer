public class T_putstatic_14 extends T_putstatic_1{
    public void run() {
        T_putstatic_1.st_p1 = 1000000;
    }
    public static int getProtectedField(){
        return T_putstatic_1.st_p1;
    }
}
