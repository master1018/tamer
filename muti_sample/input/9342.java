public class Test7009359 {
    public static void main (String[] args) {
        for(int i = 0; i < 1000000; i++) {
            if(!stringmakerBUG(null).equals("NPE")) {
                System.out.println("StringBuffer(null) does not throw NPE");
                System.exit(97);
            }
        }
    }
    public static String stringmakerBUG(String str) {
       try {
           return new StringBuffer(str).toString();
       } catch (NullPointerException e) {
           return "NPE";
       }
    }
}
