class OCSPRequest {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean dump = false;
    private final List<CertId> certIds;
    OCSPRequest(CertId certId) {
        this.certIds = Collections.singletonList(certId);
    }
    OCSPRequest(List<CertId> certIds) {
        this.certIds = certIds;
    }
    byte[] encodeBytes() throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream requestsOut = new DerOutputStream();
        for (CertId certId : certIds) {
            DerOutputStream certIdOut = new DerOutputStream();
            certId.encode(certIdOut);
            requestsOut.write(DerValue.tag_Sequence, certIdOut);
        }
        tmp.write(DerValue.tag_Sequence, requestsOut);
        DerOutputStream tbsRequest = new DerOutputStream();
        tbsRequest.write(DerValue.tag_Sequence, tmp);
        DerOutputStream ocspRequest = new DerOutputStream();
        ocspRequest.write(DerValue.tag_Sequence, tbsRequest);
        byte[] bytes = ocspRequest.toByteArray();
        if (dump) {
            HexDumpEncoder hexEnc = new HexDumpEncoder();
            System.out.println("OCSPRequest bytes are... ");
            System.out.println(hexEnc.encode(bytes));
        }
        return bytes;
    }
    List<CertId> getCertIds() {
        return certIds;
    }
}
