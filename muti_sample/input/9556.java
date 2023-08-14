public class Test {
    final static String testString = "abracadabra";
    public static void main(String args[]) {
        String params[][] = {
            {"control", testString}
        };
        for (int i=0; i<params.length; i++) {
            try {
                System.out.println("Params :" + testString + " and " + params[i][0] + ", " + params[i][1]);
                if (params[i][1] == null) {
                    System.exit(97);
                }
            } catch (Exception e) {}
        }
    }
}
