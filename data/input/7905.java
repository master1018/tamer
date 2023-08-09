public class ArrayAssignment {
    public static void main(String argv[]) throws Exception {
        int[][] from = new int[5][5];
        Object[] to = from;
        to = new Object[1];
        if (!to.getClass().isAssignableFrom(from.getClass()))
            throw new Exception("bad array assignment check in reflection");
   }
}
