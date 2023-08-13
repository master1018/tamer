class AppletObjectInputStream extends ObjectInputStream
{
    private AppletClassLoader loader;
    public AppletObjectInputStream(InputStream in, AppletClassLoader loader)
            throws IOException, StreamCorruptedException {
        super(in);
        if (loader == null) {
            throw new AppletIllegalArgumentException("appletillegalargumentexception.objectinputstream");
        }
        this.loader = loader;
    }
    private Class primitiveType(char type) {
        switch (type) {
        case 'B': return byte.class;
        case 'C': return char.class;
        case 'D': return double.class;
        case 'F': return float.class;
        case 'I': return int.class;
        case 'J': return long.class;
        case 'S': return short.class;
        case 'Z': return boolean.class;
        default: return null;
        }
    }
    protected Class resolveClass(ObjectStreamClass classDesc)
        throws IOException, ClassNotFoundException {
        String cname = classDesc.getName();
        if (cname.startsWith("[")) {
            Class component;            
            int dcount;                 
            for (dcount=1; cname.charAt(dcount)=='['; dcount++) ;
            if (cname.charAt(dcount) == 'L') {
                component = loader.loadClass(cname.substring(dcount+1,
                                                             cname.length()-1));
            } else {
                if (cname.length() != dcount+1) {
                    throw new ClassNotFoundException(cname);
                }
                component = primitiveType(cname.charAt(dcount));
            }
            int dim[] = new int[dcount];
            for (int i=0; i<dcount; i++) {
                dim[i]=0;
            }
            return Array.newInstance(component, dim).getClass();
        } else {
            return loader.loadClass(cname);
        }
    }
}
