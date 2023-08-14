public class TimestampCheck {
    static final String TSKS = "tsks";
    static final String JAR = "old.jar";
    static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            int len = 0;
            for (String h: t.getRequestHeaders().keySet()) {
                if (h.equalsIgnoreCase("Content-length")) {
                    len = Integer.valueOf(t.getRequestHeaders().get(h).get(0));
                }
            }
            byte[] input = new byte[len];
            t.getRequestBody().read(input);
            try {
                int path = 0;
                if (t.getRequestURI().getPath().length() > 1) {
                    path = Integer.parseInt(
                            t.getRequestURI().getPath().substring(1));
                }
                byte[] output = sign(input, path);
                Headers out = t.getResponseHeaders();
                out.set("Content-Type", "application/timestamp-reply");
                t.sendResponseHeaders(200, output.length);
                OutputStream os = t.getResponseBody();
                os.write(output);
            } catch (Exception e) {
                e.printStackTrace();
                t.sendResponseHeaders(500, 0);
            }
            t.close();
        }
        byte[] sign(byte[] input, int path) throws Exception {
            DerValue value = new DerValue(input);
            System.err.println("\nIncoming Request\n===================");
            System.err.println("Version: " + value.data.getInteger());
            DerValue messageImprint = value.data.getDerValue();
            AlgorithmId aid = AlgorithmId.parse(
                    messageImprint.data.getDerValue());
            System.err.println("AlgorithmId: " + aid);
            BigInteger nonce = null;
            while (value.data.available() > 0) {
                DerValue v = value.data.getDerValue();
                if (v.tag == DerValue.tag_Integer) {
                    nonce = v.getBigInteger();
                    System.err.println("nonce: " + nonce);
                } else if (v.tag == DerValue.tag_Boolean) {
                    System.err.println("certReq: " + v.getBoolean());
                }
            }
            System.err.println("\nResponse\n===================");
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(TSKS), "changeit".toCharArray());
            String alias = "ts";
            if (path == 6) alias = "tsbad1";
            if (path == 7) alias = "tsbad2";
            if (path == 8) alias = "tsbad3";
            DerOutputStream statusInfo = new DerOutputStream();
            statusInfo.putInteger(0);
            DerOutputStream token = new DerOutputStream();
            AlgorithmId[] algorithms = {aid};
            Certificate[] chain = ks.getCertificateChain(alias);
            X509Certificate[] signerCertificateChain = null;
            X509Certificate signer = (X509Certificate)chain[0];
            if (path == 5) {   
                signerCertificateChain = new X509Certificate[chain.length];
                for (int i=0; i<chain.length; i++) {
                    signerCertificateChain[i] = (X509Certificate)chain[i];
                }
            } else if (path == 9) {
                signerCertificateChain = new X509Certificate[0];
            } else {
                signerCertificateChain = new X509Certificate[1];
                signerCertificateChain[0] = (X509Certificate)chain[0];
            }
            DerOutputStream tst = new DerOutputStream();
            tst.putInteger(1);
            tst.putOID(new ObjectIdentifier("1.2.3.4"));    
            if (path != 3 && path != 4) {
                tst.putDerValue(messageImprint);
            } else {
                byte[] data = messageImprint.toByteArray();
                if (path == 4) {
                    data[6] = (byte)0x01;
                } else {
                    data[data.length-1] = (byte)0x01;
                    data[data.length-2] = (byte)0x02;
                    data[data.length-3] = (byte)0x03;
                }
                tst.write(data);
            }
            tst.putInteger(1);
            Calendar cal = Calendar.getInstance();
            tst.putGeneralizedTime(cal.getTime());
            if (path == 2) {
                tst.putInteger(1234);
            } else if (path == 1) {
            } else {
                tst.putInteger(nonce);
            }
            DerOutputStream tstInfo = new DerOutputStream();
            tstInfo.write(DerValue.tag_Sequence, tst);
            DerOutputStream tstInfo2 = new DerOutputStream();
            tstInfo2.putOctetString(tstInfo.toByteArray());
            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initSign((PrivateKey)(ks.getKey(
                    alias, "changeit".toCharArray())));
            sig.update(tstInfo.toByteArray());
            ContentInfo contentInfo = new ContentInfo(new ObjectIdentifier(
                    "1.2.840.113549.1.9.16.1.4"),
                    new DerValue(tstInfo2.toByteArray()));
            System.err.println("Signing...");
            System.err.println(new X500Name(signer
                    .getIssuerX500Principal().getName()));
            System.err.println(signer.getSerialNumber());
            SignerInfo signerInfo = new SignerInfo(
                    new X500Name(signer.getIssuerX500Principal().getName()),
                    signer.getSerialNumber(),
                    aid, AlgorithmId.get("DSA"), sig.sign());
            SignerInfo[] signerInfos = {signerInfo};
            PKCS7 p7 =
                    new PKCS7(algorithms, contentInfo, signerCertificateChain,
                    signerInfos);
            ByteArrayOutputStream p7out = new ByteArrayOutputStream();
            p7.encodeSignedData(p7out);
            DerOutputStream response = new DerOutputStream();
            response.write(DerValue.tag_Sequence, statusInfo);
            response.putDerValue(new DerValue(p7out.toByteArray()));
            DerOutputStream out = new DerOutputStream();
            out.write(DerValue.tag_Sequence, response);
            return out.toByteArray();
        }
    }
    public static void main(String[] args) throws Exception {
        Handler h = new Handler();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        HttpContext ctx = server.createContext("/", h);
        server.start();
        String cmd = null;
        if (System.getProperty("java.home").endsWith("jre")) {
            cmd = System.getProperty("java.home") + "/../bin/jarsigner" +
                " -J-Djava.security.egd=file:/dev/./urandom" +
                " -debug -keystore " + TSKS + " -storepass changeit" +
                " -tsa http:
                " -signedjar new.jar " + JAR + " old";
        } else {
            cmd = System.getProperty("java.home") + "/bin/jarsigner" +
                " -J-Djava.security.egd=file:/dev/./urandom" +
                " -debug -keystore " + TSKS + " -storepass changeit" +
                " -tsa http:
                " -signedjar new.jar " + JAR + " old";
        }
        try {
            if (args.length == 0) {         
                jarsigner(cmd, 0, true);    
                jarsigner(cmd, 1, false);   
                jarsigner(cmd, 2, false);
                jarsigner(cmd, 3, false);
                jarsigner(cmd, 4, false);
                jarsigner(cmd, 5, true);    
                jarsigner(cmd, 6, false);   
                jarsigner(cmd, 7, false);   
                jarsigner(cmd, 8, false);   
                jarsigner(cmd, 9, false);   
            } else {                        
                System.err.println("Press Enter to quit server");
                System.in.read();
            }
        } finally {
            server.stop(0);
            new File("x.jar").delete();
        }
    }
    static void jarsigner(String cmd, int path, boolean expected)
            throws Exception {
        System.err.println("Test " + path);
        Process p = Runtime.getRuntime().exec(String.format(cmd, path));
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
        while (true) {
            String s = reader.readLine();
            if (s == null) break;
            System.err.println(s);
        }
        int result = p.waitFor();
        if (expected && result != 0 || !expected && result == 0) {
            throw new Exception("Failed");
        }
    }
}
