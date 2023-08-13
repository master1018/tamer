class SignatureValidator {
    private File dir;
    SignatureValidator(File base) {
        dir = base;
    }
    boolean validate(String fn, KeySelector ks, boolean cache)
        throws Exception {
        return validate(fn, ks, null, cache);
    }
    boolean validate(String fn, KeySelector ks, URIDereferencer ud,
        boolean cache) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        Document doc = dbf.newDocumentBuilder().parse(new File(dir, fn));
        NodeList nl =
            doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Couldn't find signature Element");
        }
        Element sigElement = (Element) nl.item(0);
        DOMValidateContext vc = new DOMValidateContext(ks, sigElement);
        vc.setBaseURI(dir.toURI().toString());
        if (cache) {
            vc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        }
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance();
        XMLSignature signature = factory.unmarshalXMLSignature(vc);
        if (ud != null) {
            vc.setURIDereferencer(ud);
        }
        boolean coreValidity = signature.validate(vc);
        if (cache) {
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j=0; i.hasNext(); j++) {
                Reference ref = (Reference) i.next();
                if (!digestInputEqual(ref)) {
                    throw new Exception
                        ("cached data for Reference[" + j + "] is not correct");
                }
                if (ref.getURI() == "") {
                    System.out.println("checking deref data");
                    NodeSetData data = (NodeSetData) ref.getDereferencedData();
                    Iterator ni = data.iterator();
                    while (ni.hasNext()) {
                        Node n = (Node) ni.next();
                        if (n.getNodeType() == Node.COMMENT_NODE) {
                            throw new Exception("dereferenced data for " +
                                " Reference[" + j + " contains comment node");
                        }
                    }
                }
            }
        }
        return coreValidity;
    }
    private boolean digestInputEqual(Reference ref) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        InputStream is = ref.getDigestInputStream();
        int nbytes;
        byte[] buf = new byte[256];
        while ((nbytes = is.read(buf, 0, buf.length)) != -1) {
            md.update(buf, 0, nbytes);
        }
        return Arrays.equals(md.digest(), ref.getDigestValue());
    }
}
