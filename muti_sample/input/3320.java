public class RestoreURL {
    public static void main(String[] args) throws Exception {
        URL origUrl = new URL( "http:
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject( origUrl );
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                                    new ByteArrayInputStream( out.toByteArray() ));
        URL restoredUrl = (URL)ois.readObject();
        ois.close();
        String path = restoredUrl.getPath();
        String query = restoredUrl.getQuery();
        if ((path == null) || !path.equals(origUrl.getPath())) {
            throw new Exception("path not restored");
        }
        if ((query ==null) || !query.equals(origUrl.getQuery())) {
            throw new Exception("query not restored");
        }
    }
}
