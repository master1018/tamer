abstract class SpNegoToken extends GSSToken {
    static final int NEG_TOKEN_INIT_ID = 0x00;
    static final int NEG_TOKEN_TARG_ID = 0x01;
    static enum NegoResult {
        ACCEPT_COMPLETE,
        ACCEPT_INCOMPLETE,
        REJECT,
    };
    private int tokenType;
    static final boolean DEBUG = SpNegoContext.DEBUG;
    public static ObjectIdentifier OID;
    static {
        try {
            OID = new ObjectIdentifier(SpNegoMechFactory.
                                       GSS_SPNEGO_MECH_OID.toString());
        } catch (IOException ioe) {
        }
    }
    protected SpNegoToken(int tokenType) {
        this.tokenType = tokenType;
    }
    abstract byte[] encode() throws GSSException;
    byte[] getEncoded() throws IOException, GSSException {
        DerOutputStream token = new DerOutputStream();
        token.write(encode());
        switch (tokenType) {
            case NEG_TOKEN_INIT_ID:
                DerOutputStream initToken = new DerOutputStream();
                initToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                true, (byte) NEG_TOKEN_INIT_ID), token);
                return initToken.toByteArray();
            case NEG_TOKEN_TARG_ID:
                DerOutputStream targToken = new DerOutputStream();
                targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                true, (byte) NEG_TOKEN_TARG_ID), token);
                return targToken.toByteArray();
            default:
                return token.toByteArray();
        }
    }
    final int getType() {
        return tokenType;
    }
    static String getTokenName(int type) {
        switch (type) {
            case NEG_TOKEN_INIT_ID:
                return "SPNEGO NegTokenInit";
            case NEG_TOKEN_TARG_ID:
                return "SPNEGO NegTokenTarg";
            default:
                return "SPNEGO Mechanism Token";
        }
    }
    static NegoResult getNegoResultType(int result) {
        switch (result) {
        case 0:
                return NegoResult.ACCEPT_COMPLETE;
        case 1:
                return NegoResult.ACCEPT_INCOMPLETE;
        case 2:
                return NegoResult.REJECT;
        default:
                return NegoResult.ACCEPT_COMPLETE;
        }
    }
    static String getNegoResultString(int result) {
        switch (result) {
        case 0:
                return "Accept Complete";
        case 1:
                return "Accept InComplete";
        case 2:
                return "Reject";
        default:
                return ("Unknown Negotiated Result: " + result);
        }
    }
    static int checkNextField(int last, int current) throws GSSException {
        if (last < current) {
            return current;
        } else {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                "Invalid SpNegoToken token : wrong order");
        }
    }
}
