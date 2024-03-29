public class NegTokenTarg extends SpNegoToken {
    private int negResult = 0;
    private Oid supportedMech = null;
    private byte[] responseToken = null;
    private byte[] mechListMIC = null;
    NegTokenTarg(int result, Oid mech, byte[] token, byte[] mechListMIC)
    {
        super(NEG_TOKEN_TARG_ID);
        this.negResult = result;
        this.supportedMech = mech;
        this.responseToken = token;
        this.mechListMIC = mechListMIC;
    }
    public NegTokenTarg(byte[] in) throws GSSException {
        super(NEG_TOKEN_TARG_ID);
        parseToken(in);
    }
    final byte[] encode() throws GSSException {
        try {
            DerOutputStream targToken = new DerOutputStream();
            DerOutputStream result = new DerOutputStream();
            result.putEnumerated(negResult);
            targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                true, (byte) 0x00), result);
            if (supportedMech != null) {
                DerOutputStream mech = new DerOutputStream();
                byte[] mechType = supportedMech.getDER();
                mech.write(mechType);
                targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                                true, (byte) 0x01), mech);
            }
            if (responseToken != null) {
                DerOutputStream rspToken = new DerOutputStream();
                rspToken.putOctetString(responseToken);
                targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                        true, (byte) 0x02), rspToken);
            }
            if (mechListMIC != null) {
                if (DEBUG) {
                    System.out.println("SpNegoToken NegTokenTarg: " +
                                                "sending MechListMIC");
                }
                DerOutputStream mic = new DerOutputStream();
                mic.putOctetString(mechListMIC);
                targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                        true, (byte) 0x03), mic);
            } else if (GSSUtil.useMSInterop()) {
                if (responseToken != null) {
                    if (DEBUG) {
                        System.out.println("SpNegoToken NegTokenTarg: " +
                                "sending additional token for MS Interop");
                    }
                    DerOutputStream rspToken = new DerOutputStream();
                    rspToken.putOctetString(responseToken);
                    targToken.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                                true, (byte) 0x03), rspToken);
                }
            }
            DerOutputStream out = new DerOutputStream();
            out.write(DerValue.tag_Sequence, targToken);
            return out.toByteArray();
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                "Invalid SPNEGO NegTokenTarg token : " + e.getMessage());
        }
    }
    private void parseToken(byte[] in) throws GSSException {
        try {
            DerValue der = new DerValue(in);
            if (!der.isContextSpecific((byte) NEG_TOKEN_TARG_ID)) {
                throw new IOException("SPNEGO NegoTokenTarg : " +
                        "did not have the right token type");
            }
            DerValue tmp1 = der.data.getDerValue();
            if (tmp1.tag != DerValue.tag_Sequence) {
                throw new IOException("SPNEGO NegoTokenTarg : " +
                        "did not have the Sequence tag");
            }
            int lastField = -1;
            while (tmp1.data.available() > 0) {
                DerValue tmp2 = tmp1.data.getDerValue();
                if (tmp2.isContextSpecific((byte)0x00)) {
                    lastField = checkNextField(lastField, 0);
                    negResult = tmp2.data.getEnumerated();
                    if (DEBUG) {
                        System.out.println("SpNegoToken NegTokenTarg: negotiated" +
                                    " result = " + getNegoResultString(negResult));
                    }
                } else if (tmp2.isContextSpecific((byte)0x01)) {
                    lastField = checkNextField(lastField, 1);
                    ObjectIdentifier mech = tmp2.data.getOID();
                    supportedMech = new Oid(mech.toString());
                    if (DEBUG) {
                        System.out.println("SpNegoToken NegTokenTarg: " +
                                    "supported mechanism = " + supportedMech);
                    }
                } else if (tmp2.isContextSpecific((byte)0x02)) {
                    lastField = checkNextField(lastField, 2);
                    responseToken = tmp2.data.getOctetString();
                } else if (tmp2.isContextSpecific((byte)0x03)) {
                    lastField = checkNextField(lastField, 3);
                    if (!GSSUtil.useMSInterop()) {
                        mechListMIC = tmp2.data.getOctetString();
                        if (DEBUG) {
                            System.out.println("SpNegoToken NegTokenTarg: " +
                                                "MechListMIC Token = " +
                                                getHexBytes(mechListMIC));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                "Invalid SPNEGO NegTokenTarg token : " + e.getMessage());
        }
    }
    int getNegotiatedResult() {
        return negResult;
    }
    public Oid getSupportedMech() {
        return supportedMech;
    }
    byte[] getResponseToken() {
        return responseToken;
    }
    byte[] getMechListMIC() {
        return mechListMIC;
    }
}
