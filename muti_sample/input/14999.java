public class Bug4621320 {
    public static void main(String args[]) {
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("uk","UA"));
        if
(!dfs.getMonths()[2].equals("\u0431\u0435\u0440\u0435\u0437\u043d\u044f")) {
            throw new RuntimeException();
        }
    }
}
