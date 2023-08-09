public class Test6795362 {
    public static void main(String[] args)
    {
        sub();
        if (var_bad != 0)
            throw new InternalError(var_bad + " != 0");
    }
    static long var_bad = -1L;
    static void sub()
    {
        var_bad >>= 65;
        var_bad /= 65;
    }
}
