public class T_new_instance_3 {
    static int i = 123 / 0;
    public static int run() {
        T_new_instance_3 t = new T_new_instance_3();
        return t.i;
    }
}
