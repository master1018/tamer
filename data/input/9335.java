public class CertificateExtensions implements CertAttrSet<Extension> {
    public static final String IDENT = "x509.info.extensions";
    public static final String NAME = "extensions";
    private static final Debug debug = Debug.getInstance("x509");
    private Hashtable<String,Extension> map = new Hashtable<String,Extension>();
    private boolean unsupportedCritExt = false;
    private Map<String,Extension> unparseableExtensions;
    public CertificateExtensions() { }
    public CertificateExtensions(DerInputStream in) throws IOException {
        init(in);
    }
    private void init(DerInputStream in) throws IOException {
        DerValue[] exts = in.getSequence(5);
        for (int i = 0; i < exts.length; i++) {
            Extension ext = new Extension(exts[i]);
            parseExtension(ext);
        }
    }
    private static Class[] PARAMS = {Boolean.class, Object.class};
    private void parseExtension(Extension ext) throws IOException {
        try {
            Class extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {   
                if (ext.isCritical()) {
                    unsupportedCritExt = true;
                }
                if (map.put(ext.getExtensionId().toString(), ext) == null) {
                    return;
                } else {
                    throw new IOException("Duplicate extensions not allowed");
                }
            }
            Constructor cons = ((Class<?>)extClass).getConstructor(PARAMS);
            Object[] passed = new Object[] {Boolean.valueOf(ext.isCritical()),
                    ext.getExtensionValue()};
                    CertAttrSet certExt = (CertAttrSet)cons.newInstance(passed);
                    if (map.put(certExt.getName(), (Extension)certExt) != null) {
                        throw new IOException("Duplicate extensions not allowed");
                    }
        } catch (InvocationTargetException invk) {
            Throwable e = invk.getTargetException();
            if (ext.isCritical() == false) {
                if (unparseableExtensions == null) {
                    unparseableExtensions = new HashMap<String,Extension>();
                }
                unparseableExtensions.put(ext.getExtensionId().toString(),
                        new UnparseableExtension(ext, e));
                if (debug != null) {
                    debug.println("Error parsing extension: " + ext);
                    e.printStackTrace();
                    HexDumpEncoder h = new HexDumpEncoder();
                    System.err.println(h.encodeBuffer(ext.getExtensionValue()));
                }
                return;
            }
            if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                throw (IOException)new IOException(e.toString()).initCause(e);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
    }
    public void encode(OutputStream out)
    throws CertificateException, IOException {
        encode(out, false);
    }
    public void encode(OutputStream out, boolean isCertReq)
    throws CertificateException, IOException {
        DerOutputStream extOut = new DerOutputStream();
        Collection<Extension> allExts = map.values();
        Object[] objs = allExts.toArray();
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof CertAttrSet)
                ((CertAttrSet)objs[i]).encode(extOut);
            else if (objs[i] instanceof Extension)
                ((Extension)objs[i]).encode(extOut);
            else
                throw new CertificateException("Illegal extension object");
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write(DerValue.tag_Sequence, extOut);
        DerOutputStream tmp;
        if (!isCertReq) { 
            tmp = new DerOutputStream();
            tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)3),
                    seq);
        } else
            tmp = seq; 
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Extension) {
            map.put(name, (Extension)obj);
        } else {
            throw new IOException("Unknown extension type.");
        }
    }
    public Object get(String name) throws IOException {
        Object obj = map.get(name);
        if (obj == null) {
            throw new IOException("No extension found with name " + name);
        }
        return (obj);
    }
    public void delete(String name) throws IOException {
        Object obj = map.get(name);
        if (obj == null) {
            throw new IOException("No extension found with name " + name);
        }
        map.remove(name);
    }
    public String getNameByOid(ObjectIdentifier oid) throws IOException {
        for (String name: map.keySet()) {
            if (map.get(name).getExtensionId().equals(oid)) {
                return name;
            }
        }
        return null;
    }
    public Enumeration<Extension> getElements() {
        return map.elements();
    }
    public Collection<Extension> getAllExtensions() {
        return map.values();
    }
    public Map<String,Extension> getUnparseableExtensions() {
        if (unparseableExtensions == null) {
            return Collections.emptyMap();
        } else {
            return unparseableExtensions;
        }
    }
    public String getName() {
        return NAME;
    }
    public boolean hasUnsupportedCriticalExtension() {
        return unsupportedCritExt;
    }
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof CertificateExtensions))
            return false;
        Collection<Extension> otherC =
                ((CertificateExtensions)other).getAllExtensions();
        Object[] objs = otherC.toArray();
        int len = objs.length;
        if (len != map.size())
            return false;
        Extension otherExt, thisExt;
        String key = null;
        for (int i = 0; i < len; i++) {
            if (objs[i] instanceof CertAttrSet)
                key = ((CertAttrSet)objs[i]).getName();
            otherExt = (Extension)objs[i];
            if (key == null)
                key = otherExt.getExtensionId().toString();
            thisExt = map.get(key);
            if (thisExt == null)
                return false;
            if (! thisExt.equals(otherExt))
                return false;
        }
        return this.getUnparseableExtensions().equals(
                ((CertificateExtensions)other).getUnparseableExtensions());
    }
    public int hashCode() {
        return map.hashCode() + getUnparseableExtensions().hashCode();
    }
    public String toString() {
        return map.toString();
    }
}
class UnparseableExtension extends Extension {
    private String name;
    private Throwable why;
    public UnparseableExtension(Extension ext, Throwable why) {
        super(ext);
        name = "";
        try {
            Class extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass != null) {
                Field field = extClass.getDeclaredField("NAME");
                name = (String)(field.get(null)) + " ";
            }
        } catch (Exception e) {
        }
        this.why = why;
    }
    @Override public String toString() {
        return super.toString() +
                "Unparseable " + name + "extension due to\n" + why + "\n\n" +
                new sun.misc.HexDumpEncoder().encodeBuffer(getExtensionValue());
    }
}
