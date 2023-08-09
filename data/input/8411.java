public class Sleeper {
   private static final String USAGE = "Sleeper [ms]";
   public static void main(String argv[]) {
      long sleep = Long.MAX_VALUE;   
      if (argv.length >= 1) {
        try {
          sleep = Long.parseLong(argv[0]);
        }
        catch (NumberFormatException e) {
          System.err.println(USAGE);
          System.exit(1);
        }
      }
      try { Thread.sleep(sleep); } catch (InterruptedException e) {};
   }
}
