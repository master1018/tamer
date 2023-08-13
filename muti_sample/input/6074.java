public class ConstExpr extends Tester {
    public static void main(String[] args) {
        (new ConstExpr()).run();
    }
    public static final byte B = (byte) 0xBE;
    public static final short S = (short) 32767;
    public static final int I = -4;
    public static final long l = 4294967296L;
    public static final float f = 3.5f;
    public static final double PI = Math.PI;
    public static final char C = 'C';
    public static final String STR = "cheese";
    public static final char SMILEY = '\u263A';
    public static final String TWOLINES = "ab\ncd";
    public static final double D1 = Double.POSITIVE_INFINITY;
    public static final double D2 = Double.NEGATIVE_INFINITY;
    public static final double D3 = Double.NaN;
    public static final float  F1 = Float.POSITIVE_INFINITY;
    public static final float  F2 = Float.NEGATIVE_INFINITY;
    public static final float  F3 = Float.NaN;
    public static final String NOSTR = null;    
    public static final RoundingMode R = UP;    
    @Test(result={
              "0xbe",
              "32767",
              "-4",
              "4294967296L",
              "3.5f",
              "3.141592653589793",
              "'C'",
              "\"cheese\"",
              "'\\u263a'",
              "\"ab\\ncd\"",
              "1.0/0.0",
              "-1.0/0.0",
              "0.0/0.0",
              "1.0f/0.0f",
              "-1.0f/0.0f",
              "0.0f/0.0f",
              "null",
              "null"
          },
          ordered=true)
    Collection<String> getConstantExpression() {
        final Collection<String> res = new ArrayList<String>();
        thisClassDecl.accept(
            DeclarationVisitors.getSourceOrderDeclarationScanner(
                NO_OP,
                new SimpleDeclarationVisitor() {
                    public void visitFieldDeclaration(FieldDeclaration f) {
                        res.add(f.getConstantExpression());
                    }
                }));
        return res;
    }
}
