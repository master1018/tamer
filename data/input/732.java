public class StaticImport {
    public static void main(String[] args) {
        out.println(sin(PI/6));
        if (abs(1.0 - 2*sin(PI/6)) > 0.000001)
            throw new Error();
    }
}
