public class StreamConstructor {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater def = new Deflater();
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[10]);
        Inflater inf = new Inflater();
        InflaterInputStream infOS;
        DeflaterOutputStream defOS;
        try {
            defOS = new DeflaterOutputStream(bos, null);
            throw new Exception("didn't catch illegal argument");
        } catch (NullPointerException e){
        }
        try {
            defOS = new DeflaterOutputStream(null, def);
            throw new Exception("didn't catch illegal argument");
        } catch (NullPointerException e){
        }
        try {
            defOS = new DeflaterOutputStream(bos, def, -1);
            throw new Exception("didn't catch illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            infOS = new InflaterInputStream(bis, null);
            throw new Exception("didn't catch illegal argument");
        } catch (NullPointerException e){
        }
        try {
            infOS = new InflaterInputStream(null, inf);
            throw new Exception("didn't catch illegal argument");
        } catch (NullPointerException e){
        }
        try {
            infOS = new InflaterInputStream(bis, inf, -1);
            throw new Exception("didn't catch illegal argument");
        } catch (IllegalArgumentException e) {
        }
    }
}
