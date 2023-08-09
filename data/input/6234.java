public class CRLExtensions {
    private Hashtable<String,Extension> map = new Hashtable<String,Extension>();
    private boolean unsupportedCritExt = false;
    public CRLExtensions() { }
    public CRLExtensions(DerInputStream in) throws CRLException {
        init(in);
    }
    private void init(DerInputStream derStrm) throws CRLException {
        try {
            DerInputStream str = derStrm;
            byte nextByte = (byte)derStrm.peekByte();
            if (((nextByte & 0x0c0) == 0x080) &&
                ((nextByte & 0x01f) == 0x000)) {
                DerValue val = str.getDerValue();
                str = val.data;
            }
            DerValue[] exts = str.getSequence(5);
            for (int i = 0; i < exts.length; i++) {
                Extension ext = new Extension(exts[i]);
                parseExtension(ext);
            }
        } catch (IOException e) {
            throw new CRLException("Parsing error: " + e.toString());
        }
    }
    private static final Class[] PARAMS = {Boolean.class, Object.class};
    private void parseExtension(Extension ext) throws CRLException {
        try {
            Class extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {   
                if (ext.isCritical())
                    unsupportedCritExt = true;
                if (map.put(ext.getExtensionId().toString(), ext) != null)
                    throw new CRLException("Duplicate extensions not allowed");
                return;
            }
            Constructor cons = ((Class<?>)extClass).getConstructor(PARAMS);
            Object[] passed = new Object[] {Boolean.valueOf(ext.isCritical()),
                                            ext.getExtensionValue()};
            CertAttrSet crlExt = (CertAttrSet)cons.newInstance(passed);
            if (map.put(crlExt.getName(), (Extension)crlExt) != null) {
                throw new CRLException("Duplicate extensions not allowed");
            }
        } catch (InvocationTargetException invk) {
            throw new CRLException(invk.getTargetException().getMessage());
        } catch (Exception e) {
            throw new CRLException(e.toString());
        }
    }
    public void encode(OutputStream out, boolean isExplicit)
    throws CRLException {
        try {
            DerOutputStream extOut = new DerOutputStream();
            Collection<Extension> allExts = map.values();
            Object[] objs = allExts.toArray();
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof CertAttrSet)
                    ((CertAttrSet)objs[i]).encode(extOut);
                else if (objs[i] instanceof Extension)
                    ((Extension)objs[i]).encode(extOut);
                else
                    throw new CRLException("Illegal extension object");
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write(DerValue.tag_Sequence, extOut);
            DerOutputStream tmp = new DerOutputStream();
            if (isExplicit)
                tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                             true, (byte)0), seq);
            else
                tmp = seq;
            out.write(tmp.toByteArray());
        } catch (IOException e) {
            throw new CRLException("Encoding error: " + e.toString());
        } catch (CertificateException e) {
            throw new CRLException("Encoding error: " + e.toString());
        }
    }
    public Extension get(String alias) {
        X509AttributeName attr = new X509AttributeName(alias);
        String name;
        String id = attr.getPrefix();
        if (id.equalsIgnoreCase(X509CertImpl.NAME)) { 
            int index = alias.lastIndexOf(".");
            name = alias.substring(index + 1);
        } else
            name = alias;
        return map.get(name);
    }
    public void set(String alias, Object obj) {
        map.put(alias, (Extension)obj);
    }
    public void delete(String alias) {
        map.remove(alias);
    }
    public Enumeration<Extension> getElements() {
        return map.elements();
    }
    public Collection<Extension> getAllExtensions() {
        return map.values();
    }
    public boolean hasUnsupportedCriticalExtension() {
        return unsupportedCritExt;
    }
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof CRLExtensions))
            return false;
        Collection<Extension> otherC =
                        ((CRLExtensions)other).getAllExtensions();
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
        return true;
    }
    public int hashCode() {
        return map.hashCode();
    }
    public String toString() {
        return map.toString();
    }
}
