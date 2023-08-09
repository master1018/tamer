public class XMLDSigWithSecMgr implements Runnable {
    private XMLSignatureFactory fac;
    private DigestMethod sha1;
    private CanonicalizationMethod withoutComments;
    private DocumentBuilder db;
    private ServerSocket ss;
    private void setup() throws Exception {
        ss = new ServerSocket(0);
        Thread thr = new Thread(this);
        thr.start();
        fac = XMLSignatureFactory.getInstance();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
        sha1 = fac.newDigestMethod(DigestMethod.SHA1, null);
        withoutComments = fac.newCanonicalizationMethod
            (CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec)null);
    }
    public void run() {
        try {
        for (int i=0; i<2; i++) {
            Socket s = ss.accept();
            s.setTcpNoDelay(true);
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Length: 11\r\n");
            out.print("Content-Type: text/plain\r\n");
            out.print("\r\n");
            out.print("l;ajfdjafd\n");
            out.flush();
            Thread.currentThread().sleep(2000);
            s.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    XMLDSigWithSecMgr() throws Exception {
        setup();
        Document doc = db.newDocument();
        Element envelope = doc.createElementNS
            ("http:
        envelope.setAttributeNS("http:
            "xmlns", "http:
        doc.appendChild(envelope);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair kp = kpg.genKeyPair();
        URI policyURI =
            new File(System.getProperty("test.src", "."), "policy").toURI();
        Policy.setPolicy
            (Policy.getInstance("JavaPolicy", new URIParameter(policyURI)));
        System.setSecurityManager(new SecurityManager());
        try {
            ArrayList refs = new ArrayList();
            refs.add(fac.newReference
                ("", sha1,
                 Collections.singletonList
                    (fac.newTransform(Transform.ENVELOPED,
                     (TransformParameterSpec) null)), null, null));
            refs.add(fac.newReference("http:
                + "/anything.txt", sha1));
            SignedInfo si = fac.newSignedInfo(withoutComments,
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), refs);
            XMLSignature sig = fac.newXMLSignature(si, null);
            DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), envelope);
            sig.sign(dsc);
            DOMValidateContext dvc = new DOMValidateContext
                (kp.getPublic(), envelope.getFirstChild());
            sig = fac.unmarshalXMLSignature(dvc);
            if (!sig.validate(dvc)) {
                throw new Exception
                    ("XMLDSigWithSecMgr signature validation FAILED");
            }
        } catch (SecurityException se) {
            throw new Exception("XMLDSigWithSecMgr FAILED", se);
        }
        ss.close();
    }
    public static void main(String[] args) throws Exception {
        new XMLDSigWithSecMgr();
    }
}
