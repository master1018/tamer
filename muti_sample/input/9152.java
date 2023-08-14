public class ModPowPowersof2 {
      public static void main(String args[]) throws Exception {
          String[] command = new String[4];
          int n = 0;
          command[n++] = System.getProperty("java.home") + File.separator +
              "bin" + File.separator + "java";
          if (System.getProperty("java.class.path") != null) {
              command[n++] = "-classpath";
              command[n++] = System.getProperty("java.class.path");
          }
          command[n++] = "ModPowPowersof2$ModTester";
          Process p = null;
          p = Runtime.getRuntime().exec(command);
          BufferedReader in = new BufferedReader(new InputStreamReader(
                                             p.getInputStream()));
          String s;
          s = in.readLine();
          if (s == null)
              throw new RuntimeException("ModPow causes vm crash");
     }
    public static class ModTester {
        public static void main(String [] args) {
            BigInteger two = BigInteger.valueOf(2);
            BigInteger four = BigInteger.valueOf(4);
            two.modPow(two, BigInteger.valueOf(4));
            two.modPow(two, BigInteger.valueOf(8));
            two.modPow(four, BigInteger.valueOf(8));
            System.out.println("success");
        }
    }
}
