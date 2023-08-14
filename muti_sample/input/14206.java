public class NegInt {
    public static void main(String[] args) throws Exception {
        DerOutputStream out;
        out = new DerOutputStream();
        out.putInteger(-128);
        if(out.toByteArray().length != 3) {
            throw new Exception("-128 encode error");
        }
        out = new DerOutputStream();
        out.putInteger(-129);
        if(out.toByteArray().length != 4) {
            throw new Exception("-129 encode error");
        }
    }
}
