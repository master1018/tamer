class Test6756768a
{
    static boolean var_1 = true;
}
final class Test6756768b
{
    static boolean var_24 = false;
    static int var_25 = 0;
    static boolean var_temp1 = Test6756768a.var_1 = false;
}
public final class Test6756768 extends Test6756768a
{
    final static int var = var_1 ^ (Test6756768b.var_24 ? var_1 : var_1) ? Test6756768b.var_25 : 1;
    static public void main(String[] args) {
        if (var != 0) {
            throw new InternalError("var = " + var);
        }
    }
}
