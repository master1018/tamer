public class StockName implements Formattable {
    private String symbol, companyName, frenchCompanyName;
    public StockName(String symbol, String companyName,
                     String frenchCompanyName)
    {
        this.symbol = symbol;
        this.companyName = companyName;
        this.frenchCompanyName = frenchCompanyName;
    }
    public void formatTo(Formatter fmt, int f, int width, int precision) {
        StringBuilder sb = new StringBuilder();
        String name = companyName;
        if (fmt.locale().equals(Locale.FRANCE))
            name = frenchCompanyName;
        boolean alternate = (f & ALTERNATE) == ALTERNATE;
        boolean usesymbol = alternate || (precision != -1 && precision < 10);
        String out = (usesymbol ? symbol : name);
        if (precision == -1 || out.length() < precision) {
            sb.append(out);
        } else {
            sb.append(out.substring(0, precision - 1)).append('*');
        }
        int len = sb.length();
        if (len < width)
            for (int i = 0; i < width - len; i++)
                if ((f & LEFT_JUSTIFY) == LEFT_JUSTIFY)
                    sb.append(' ');
                else
                    sb.insert(0, ' ');
        fmt.format(sb.toString());
    }
    public String toString() {
        return String.format("%s - %s", symbol, companyName);
    }
    public static void main(String [] args) {
        StockName sn = new StockName("HUGE", "Huge Fruit, Inc.",
                                     "Fruit Titanesque, Inc.");
        CharBuffer cb = CharBuffer.allocate(128);
        Formatter fmt = new Formatter(cb);
        fmt.format("%s", sn);            
        test(cb, "Huge Fruit, Inc.");
        fmt.format("%s", sn.toString()); 
        test(cb, "HUGE - Huge Fruit, Inc.");
        fmt.format("%#s", sn);           
        test(cb, "HUGE");
        fmt.format("%-10.8s", sn);       
        test(cb, "HUGE      ");
        fmt.format("%.12s", sn);         
        test(cb, "Huge Fruit,*");
        fmt.format(Locale.FRANCE, "%25s", sn);
        test(cb, "   Fruit Titanesque, Inc.");
    }
    private static void test(CharBuffer cb, String exp) {
        cb.limit(cb.position());
        cb.rewind();
        if (!cb.toString().equals(exp))
            throw new RuntimeException("expect: '" + exp + "'; got: '"
                                       + cb.toString() + "'");
        cb.clear();
    }
}
