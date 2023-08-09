public final class OAEPParameters extends AlgorithmParametersSpi {
    private String mdName;
    private MGF1ParameterSpec mgfSpec;
    private byte[] p;
    private static ObjectIdentifier OID_MGF1;
    private static ObjectIdentifier OID_PSpecified;
    static {
        try {
            OID_MGF1 = new ObjectIdentifier(new int[] {1,2,840,113549,1,1,8});
        } catch (IOException ioe) {
            OID_MGF1 = null;
        }
        try {
            OID_PSpecified =
                new ObjectIdentifier(new int[] {1,2,840,113549,1,1,9});
        } catch (IOException ioe) {
            OID_PSpecified = null;
        }
    }
    public OAEPParameters() {
    }
    protected void engineInit(AlgorithmParameterSpec paramSpec)
        throws InvalidParameterSpecException {
        if (!(paramSpec instanceof OAEPParameterSpec)) {
            throw new InvalidParameterSpecException
                ("Inappropriate parameter specification");
        }
        OAEPParameterSpec spec = (OAEPParameterSpec) paramSpec;
        mdName = spec.getDigestAlgorithm();
        String mgfName = spec.getMGFAlgorithm();
        if (!mgfName.equalsIgnoreCase("MGF1")) {
            throw new InvalidParameterSpecException("Unsupported mgf " +
                mgfName + "; MGF1 only");
        }
        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
        if (!(mgfSpec instanceof MGF1ParameterSpec)) {
            throw new InvalidParameterSpecException("Inappropriate mgf " +
                "parameters; non-null MGF1ParameterSpec only");
        }
        this.mgfSpec = (MGF1ParameterSpec) mgfSpec;
        PSource pSrc = spec.getPSource();
        if (pSrc.getAlgorithm().equals("PSpecified")) {
            p = ((PSource.PSpecified) pSrc).getValue();
        } else {
            throw new InvalidParameterSpecException("Unsupported pSource " +
                pSrc.getAlgorithm() + "; PSpecified only");
        }
    }
    private static String convertToStandardName(String internalName) {
        if (internalName.equals("SHA")) {
            return "SHA-1";
        } else if (internalName.equals("SHA256")) {
            return "SHA-256";
        } else if (internalName.equals("SHA384")) {
            return "SHA-384";
        } else if (internalName.equals("SHA512")) {
            return "SHA-512";
        } else {
            return internalName;
        }
    }
    protected void engineInit(byte[] encoded)
        throws IOException {
        DerInputStream der = new DerInputStream(encoded);
        mdName = "SHA-1";
        mgfSpec = MGF1ParameterSpec.SHA1;
        p = new byte[0];
        DerValue[] datum = der.getSequence(3);
        for (int i=0; i<datum.length; i++) {
            DerValue data = datum[i];
            if (data.isContextSpecific((byte) 0x00)) {
                mdName = convertToStandardName(AlgorithmId.parse
                    (data.data.getDerValue()).getName());
            } else if (data.isContextSpecific((byte) 0x01)) {
                AlgorithmId val = AlgorithmId.parse(data.data.getDerValue());
                if (!val.getOID().equals((Object) OID_MGF1)) {
                    throw new IOException("Only MGF1 mgf is supported");
                }
                AlgorithmId params = AlgorithmId.parse(
                    new DerValue(val.getEncodedParams()));
                String mgfDigestName = convertToStandardName(params.getName());
                if (mgfDigestName.equals("SHA-1")) {
                    mgfSpec = MGF1ParameterSpec.SHA1;
                } else if (mgfDigestName.equals("SHA-256")) {
                    mgfSpec = MGF1ParameterSpec.SHA256;
                } else if (mgfDigestName.equals("SHA-384")) {
                    mgfSpec = MGF1ParameterSpec.SHA384;
                } else if (mgfDigestName.equals("SHA-512")) {
                    mgfSpec = MGF1ParameterSpec.SHA512;
                } else {
                    throw new IOException(
                        "Unrecognized message digest algorithm");
                }
            } else if (data.isContextSpecific((byte) 0x02)) {
                AlgorithmId val = AlgorithmId.parse(data.data.getDerValue());
                if (!val.getOID().equals((Object) OID_PSpecified)) {
                    throw new IOException("Wrong OID for pSpecified");
                }
                DerInputStream dis = new DerInputStream(val.getEncodedParams());
                p = dis.getOctetString();
                if (dis.available() != 0) {
                    throw new IOException("Extra data for pSpecified");
                }
            } else {
                throw new IOException("Invalid encoded OAEPParameters");
            }
        }
    }
    protected void engineInit(byte[] encoded, String decodingMethod)
        throws IOException {
        if ((decodingMethod != null) &&
            (!decodingMethod.equalsIgnoreCase("ASN.1"))) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        engineInit(encoded);
    }
    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec)
        throws InvalidParameterSpecException {
        if (OAEPParameterSpec.class.isAssignableFrom(paramSpec)) {
            return new OAEPParameterSpec(mdName, "MGF1", mgfSpec,
                new PSource.PSpecified(p));
        } else {
            throw new InvalidParameterSpecException
                ("Inappropriate parameter specification");
        }
    }
    protected byte[] engineGetEncoded() throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream tmp2, tmp3;
        AlgorithmId mdAlgId;
        try {
            mdAlgId = AlgorithmId.get(mdName);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException("AlgorithmId " + mdName +
                                  " impl not found");
        }
        tmp2 = new DerOutputStream();
        mdAlgId.derEncode(tmp2);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0),
                      tmp2);
        tmp2 = new DerOutputStream();
        tmp2.putOID(OID_MGF1);
        AlgorithmId mgfDigestId;
        try {
            mgfDigestId = AlgorithmId.get(mgfSpec.getDigestAlgorithm());
        } catch (NoSuchAlgorithmException nase) {
            throw new IOException("AlgorithmId " +
                    mgfSpec.getDigestAlgorithm() + " impl not found");
        }
        mgfDigestId.encode(tmp2);
        tmp3 = new DerOutputStream();
        tmp3.write(DerValue.tag_Sequence, tmp2);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)1),
                  tmp3);
        tmp2 = new DerOutputStream();
        tmp2.putOID(OID_PSpecified);
        tmp2.putOctetString(p);
        tmp3 = new DerOutputStream();
        tmp3.write(DerValue.tag_Sequence, tmp2);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)2),
                  tmp3);
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, tmp);
        return out.toByteArray();
    }
    protected byte[] engineGetEncoded(String encodingMethod)
        throws IOException {
        if ((encodingMethod != null) &&
            (!encodingMethod.equalsIgnoreCase("ASN.1"))) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        return engineGetEncoded();
    }
    protected String engineToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MD: " + mdName + "\n");
        sb.append("MGF: MGF1" + mgfSpec.getDigestAlgorithm() + "\n");
        sb.append("PSource: PSpecified " +
            (p.length==0? "":Debug.toHexString(new BigInteger(p))) + "\n");
        return sb.toString();
    }
}
