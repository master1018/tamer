public final class SADebugServer {
   private SADebugServer() {}
   private static void usage() {
      java.io.PrintStream out = System.out;
      out.println("Usage: jsadebugd [options] <pid> [server-id]");
      out.println("\t\t(to connect to a live java process)");
      out.println("   or  jsadebugd [options] <executable> <core> [server-id]");
      out.println("\t\t(to connect to a core file produced by <executable>)");
      out.println("\t\tserver-id is an optional unique id for this debug server, needed ");
      out.println("\t\tif multiple debug servers are run on the same machine");
      out.println("where options include:");
      out.println("   -h | -help\tto print this help message");
      System.exit(1);
  }
   public static void main(String[] args) {
      if ((args.length < 1) || (args.length > 3)) {
         usage();
      }
      if (args[0].startsWith("-")) {
         usage();
      }
      System.setProperty("sun.jvm.hotspot.debugger.useProcDebugger", "true");
      System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");
      sun.jvm.hotspot.DebugServer.main(args);
   }
}
