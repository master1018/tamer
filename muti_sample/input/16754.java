public class ListSpace {
    public static void main(String[] args) throws Exception {
        File d = new File(".");
        d = new File(d.getCanonicalPath()+" ");
        if (!d.isDirectory())
            return;
        if (d.list() == null)
            throw new RuntimeException("list is null");
    }
}
