public class ExceedMaxDim {
    public static void main(String[] args) throws Exception {
        newInstanceOne();
        newInstanceMulti();
        zeroDimension();
    }
    private static void newInstanceOne() throws Exception {
        Object o = getArrayOf256Dimensions();
        try {
            o = Array.newInstance(o.getClass(), 1);
        } catch (IllegalArgumentException iae) {
            System.out.println("success: newInstanceOne test");
            return;
        }
        throw new Exception("NewArray allowed dimensions > MAXDIM");
    }
    private static void newInstanceMulti() throws Exception {
        Object o = getArrayOf256Dimensions();
        try {
            o = Array.newInstance(o.getClass(), new int[] { 1, 1 });
            o = Array.newInstance(o.getClass(), new int[] { 1 });
        } catch (IllegalArgumentException iae) {
            System.out.println("success: newInstanceMulti test");
            return;
        }
        throw new Exception("MultiNewArray allowed dimensions > MAXDIM");
    }
    private static void zeroDimension() throws Exception {
        try {
            Array.newInstance(Integer.TYPE, new int[0]);
        } catch (IllegalArgumentException iae) {
            System.out.println("success: zeroDimension test");
            return;
        }
        throw new Exception("MultiNewArray allowed dimension == 0");
    }
    private static Object getArrayOf256Dimensions() {
        Object o = Array.newInstance(Integer.TYPE, 0);
        for (int i = 1; i <= 254; i++) {
            o = Array.newInstance(o.getClass(), 1);
        }
        return o;
    }
}
