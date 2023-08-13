public class KDCReq {
    public KDCReqBody reqBody;
    private int pvno;
    private int msgType;
    private PAData[] pAData = null; 
    public KDCReq(PAData[] new_pAData, KDCReqBody new_reqBody,
            int req_type) throws IOException {
        pvno = Krb5.PVNO;
        msgType = req_type;
        if (new_pAData != null) {
            pAData = new PAData[new_pAData.length];
            for (int i = 0; i < new_pAData.length; i++) {
                if (new_pAData[i] == null) {
                    throw new IOException("Cannot create a KDCRep");
                } else {
                    pAData[i] = (PAData) new_pAData[i].clone();
                }
            }
        }
        reqBody = new_reqBody;
    }
    public KDCReq() {
    }
    public KDCReq(byte[] data, int req_type) throws Asn1Exception,
            IOException, KrbException {
        init(new DerValue(data), req_type);
    }
    public KDCReq(DerValue der, int req_type) throws Asn1Exception,
            IOException, KrbException {
        init(der, req_type);
    }
    protected void init(DerValue encoding, int req_type) throws Asn1Exception,
            IOException, KrbException {
        DerValue der, subDer;
        BigInteger bint;
        if ((encoding.getTag() & 0x1F) != req_type) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & 0x01F) == 0x01) {
            bint = subDer.getData().getBigInteger();
            this.pvno = bint.intValue();
            if (this.pvno != Krb5.PVNO) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADVERSION);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & 0x01F) == 0x02) {
            bint = subDer.getData().getBigInteger();
            this.msgType = bint.intValue();
            if (this.msgType != req_type) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_MSG_TYPE);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if ((der.getData().peekByte() & 0x1F) == 0x03) {
            subDer = der.getData().getDerValue();
            DerValue subsubDer = subDer.getData().getDerValue();
            if (subsubDer.getTag() != DerValue.tag_SequenceOf) {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
            Vector<PAData> v = new Vector<>();
            while (subsubDer.getData().available() > 0) {
                v.addElement(new PAData(subsubDer.getData().getDerValue()));
            }
            if (v.size() > 0) {
                pAData = new PAData[v.size()];
                v.copyInto(pAData);
            }
        } else {
            pAData = null;
        }
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & 0x01F) == 0x04) {
            DerValue subsubDer = subDer.getData().getDerValue();
            reqBody = new KDCReqBody(subsubDer, msgType);
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream temp, bytes, out;
        temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(pvno));
        out = new DerOutputStream();
        out.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x01), temp);
        temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(msgType));
        out.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x02), temp);
        if (pAData != null && pAData.length > 0) {
            temp = new DerOutputStream();
            for (int i = 0; i < pAData.length; i++) {
                temp.write(pAData[i].asn1Encode());
            }
            bytes = new DerOutputStream();
            bytes.write(DerValue.tag_SequenceOf, temp);
            out.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x03), bytes);
        }
        out.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x04), reqBody.asn1Encode(msgType));
        bytes = new DerOutputStream();
        bytes.write(DerValue.tag_Sequence, out);
        out = new DerOutputStream();
        out.write(DerValue.createTag(DerValue.TAG_APPLICATION,
                true, (byte) msgType), bytes);
        return out.toByteArray();
    }
    public byte[] asn1EncodeReqBody() throws Asn1Exception, IOException {
        return reqBody.asn1Encode(msgType);
    }
}
