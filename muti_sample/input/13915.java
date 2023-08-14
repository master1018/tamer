public class SysPropsDumper extends Tool {
   public void run() {
      Properties sysProps = VM.getVM().getSystemProperties();
      PrintStream out = System.out;
      if (sysProps != null) {
         Enumeration keys = sysProps.keys();
         while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            out.print(key);
            out.print(" = ");
            out.println(sysProps.get(key));
         }
      } else {
         out.println("System Properties info not available!");
      }
   }
   public static void main(String[] args) {
      SysPropsDumper pd = new SysPropsDumper();
      pd.start(args);
      pd.stop();
   }
}
