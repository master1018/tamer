public class T_putstatic_1 {
    public static int st_i1;
    protected static int st_p1;
    private static int st_pvt1;
    public void run() {
        st_i1 = 1000000;
    }
    public static int getPvtField()
    {
        return st_pvt1;
    }
}
