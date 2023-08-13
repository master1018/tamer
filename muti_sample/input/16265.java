class GSSLibStub {
    private Oid mech;
    private long pMech;
    static native boolean init(String lib);
    private static native long getMechPtr(byte[] oidDerEncoding);
    static native Oid[] indicateMechs();
    native Oid[] inquireNamesForMech() throws GSSException;
    native void releaseName(long pName);
    native long importName(byte[] name, Oid type);
    native boolean compareName(long pName1, long pName2);
    native long canonicalizeName(long pName);
    native byte[] exportName(long pName) throws GSSException;
    native Object[] displayName(long pName) throws GSSException;
    native long acquireCred(long pName, int lifetime, int usage)
                                        throws GSSException;
    native long releaseCred(long pCred);
    native long getCredName(long pCred);
    native int getCredTime(long pCred);
    native int getCredUsage(long pCred);
    native NativeGSSContext importContext(byte[] interProcToken);
    native byte[] initContext(long pCred, long targetName, ChannelBinding cb,
                              byte[] inToken, NativeGSSContext context);
    native byte[] acceptContext(long pCred, ChannelBinding cb,
                                byte[] inToken, NativeGSSContext context);
    native long[] inquireContext(long pContext);
    native Oid getContextMech(long pContext);
    native long getContextName(long pContext, boolean isSrc);
    native int getContextTime(long pContext);
    native long deleteContext(long pContext);
    native int wrapSizeLimit(long pContext, int flags, int qop, int outSize);
    native byte[] exportContext(long pContext);
    native byte[] getMic(long pContext, int qop, byte[] msg);
    native void verifyMic(long pContext, byte[] token, byte[] msg,
                          MessageProp prop) ;
    native byte[] wrap(long pContext, byte[] msg, MessageProp prop);
    native byte[] unwrap(long pContext, byte[] msgToken, MessageProp prop);
    private static Hashtable<Oid, GSSLibStub>
        table = new Hashtable<Oid, GSSLibStub>(5);
    static GSSLibStub getInstance(Oid mech) throws GSSException {
        GSSLibStub s = table.get(mech);
        if (s == null) {
            s = new GSSLibStub(mech);
            table.put(mech, s);
        }
        return s;
    }
    private GSSLibStub(Oid mech) throws GSSException {
        SunNativeProvider.debug("Created GSSLibStub for mech " + mech);
        this.mech = mech;
        this.pMech = getMechPtr(mech.getDER());
    }
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GSSLibStub)) {
            return false;
        }
        return (mech.equals(((GSSLibStub) obj).getMech()));
    }
    public int hashCode() {
        return mech.hashCode();
    }
    Oid getMech() {
        return mech;
    }
}
