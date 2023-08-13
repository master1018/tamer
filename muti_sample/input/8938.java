public class ListRoots {
    public static void main(String[] args) throws Exception {
        File[] rs = File.listRoots();
        for (int i = 0; i < rs.length; i++) {
            System.out.println(i + ": " + rs[i]);
        }
        File f = new File(System.getProperty("test.src", "."),
                          "ListRoots.java");
        String cp = f.getCanonicalPath();
        for (int i = 0; i < rs.length; i++) {
            if (cp.startsWith(rs[i].getPath())) break;
            if (i == rs.length - 1)
                throw new Exception(cp + " does not have a recognized root");
        }
    }
}
