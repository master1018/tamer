public class Exceptions {
    public static void main(String[] args) throws Exception {
        testFileSystemException();
        testDirectoryIteratorException();
    }
    static void testFileSystemException() throws Exception {
        String thisFile = "source";
        String otherFile = "target";
        String reason = "Access denied";
        testFileSystemException(thisFile, otherFile, reason);
        testFileSystemException(null, otherFile, reason);
        testFileSystemException(thisFile, null, reason);
        testFileSystemException(thisFile, otherFile, null);
        FileSystemException exc;
        exc = new FileSystemException(thisFile, otherFile, reason);
        exc = (FileSystemException)deserialize(serialize(exc));
        if (!exc.getFile().equals(thisFile) || !exc.getOtherFile().equals(otherFile))
            throw new RuntimeException("Exception not reconstituted completely");
    }
    static void testFileSystemException(String thisFile,
                                        String otherFile,
                                        String reason)
    {
        FileSystemException exc = new FileSystemException(thisFile, otherFile, reason);
        if (!Objects.equals(thisFile, exc.getFile()))
            throw new RuntimeException("getFile returned unexpected result");
        if (!Objects.equals(otherFile, exc.getOtherFile()))
            throw new RuntimeException("getOtherFile returned unexpected result");
        if (!Objects.equals(reason, exc.getReason()))
            throw new RuntimeException("getReason returned unexpected result");
    }
    static void testDirectoryIteratorException() throws Exception {
        try {
            new DirectoryIteratorException(null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException expected) { }
        DirectoryIteratorException exc;
        exc = new DirectoryIteratorException(new IOException());
        exc = (DirectoryIteratorException)deserialize(serialize(exc));
        IOException ioe = exc.getCause();
        if (ioe == null)
            throw new RuntimeException("Cause should not be null");
        hackCause(exc, null);
        try {
            deserialize(serialize(exc));
            throw new RuntimeException("InvalidObjectException expected");
        } catch (InvalidObjectException expected) { }
        hackCause(exc, new RuntimeException());
        try {
            deserialize(serialize(exc));
            throw new RuntimeException("InvalidObjectException expected");
        } catch (InvalidObjectException expected) { }
    }
    static void hackCause(Throwable t, Throwable cause)
        throws NoSuchFieldException, IllegalAccessException
    {
        Field f = Throwable.class.getDeclaredField("cause");
        f.setAccessible(true);
        f.set(t, cause);
    }
    static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return baos.toByteArray();
    }
    static Object deserialize(byte[] bytes)
        throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}
