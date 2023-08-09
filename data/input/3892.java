public class SpecTests {
    public static void main(String args[]) throws Exception {
        String ILE = "java.lang.IllegalArgumentException";
        String NPE = "java.lang.NullPointerException";
        String names[] =   {"", null, "foo", "foo", "foo", "foo"};
        String actions[] = {"read", "read", "", null, "junk",
                         "read,write,execute,delete,rename"};
        String exps[] = { null, NPE, ILE, ILE, ILE, ILE };
        FilePermission permit;
        for (int i = 0; i < names.length; i++) {
            try {
                permit = new FilePermission(names[i], actions[i]);
            } catch (Exception e) {
                if (exps[i] == null) {
                    throw e;
                } else if (!((e.getClass().getName()).equals(exps[i]))) {
                    throw new Exception("Expecting: " + exps[i] +
                                        " for name:" + names[i] +
                                        " actions:" + actions[i]);
                } else {
                   System.out.println(names[i] + ", [" + actions[i] + "] " +
                         "resulted in " + exps[i] + " as Expected");
                }
           }
           if (exps[i] == null) {
                System.out.println(names[i] + ", [" + actions[i] + "] " +
                         "resulted in No Exception as Expected");
            }
        }
    }
}
