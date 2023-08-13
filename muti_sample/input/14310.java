public class BogusGrants {
   public static void main(String args[]) throws Exception {
       String dir = System.getProperty("test.src", ".");
       for (int i=0; i < args.length; i++) {
           try {
               PolicyParser pp = new PolicyParser(true);
               String pfile =  new File(dir, args[i]).getPath();
               pp.read(new FileReader(pfile));
               Enumeration ge = pp.grantElements();
               if (ge.hasMoreElements()) {
                   throw new Exception("PolicyFile " + pfile + " grant entry should not parse but it did");
               }
           } catch
               (sun.security.provider.PolicyParser.ParsingException p) {
               System.out.println("Passed test " + i +
                                  ": Bogus grant entry caught " +
                                  p.getMessage());
           }
       }
   }
}
