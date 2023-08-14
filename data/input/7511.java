abstract class AbstractUserDefinedFileAttributeView
    implements UserDefinedFileAttributeView, DynamicFileAttributeView
{
    protected AbstractUserDefinedFileAttributeView() { }
    protected void checkAccess(String file,
                               boolean checkRead,
                               boolean checkWrite)
    {
        assert checkRead || checkWrite;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (checkRead)
                sm.checkRead(file);
            if (checkWrite)
                sm.checkWrite(file);
            sm.checkPermission(new RuntimePermission("accessUserDefinedAttributes"));
        }
    }
    @Override
    public final String name() {
        return "user";
    }
    @Override
    public final void setAttribute(String attribute, Object value)
        throws IOException
    {
        ByteBuffer bb;
        if (value instanceof byte[]) {
            bb = ByteBuffer.wrap((byte[])value);
        } else {
            bb = (ByteBuffer)value;
        }
        write(attribute, bb);
    }
    @Override
    public final Map<String,Object> readAttributes(String[] attributes)
        throws IOException
    {
        List<String> names = new ArrayList<>();
        for (String name: attributes) {
            if (name.equals("*")) {
                names = list();
                break;
            } else {
                if (name.length() == 0)
                    throw new IllegalArgumentException();
                names.add(name);
            }
        }
        Map<String,Object> result = new HashMap<>();
        for (String name: names) {
            int size = size(name);
            byte[] buf = new byte[size];
            int n = read(name, ByteBuffer.wrap(buf));
            byte[] value = (n == size) ? buf : Arrays.copyOf(buf, n);
            result.put(name, value);
        }
        return result;
    }
}
