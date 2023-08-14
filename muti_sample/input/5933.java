public class SimpleSSLContext {
    SSLContext ssl;
    SimpleSSLContext (String dir) throws IOException {
        try {
            String file = dir+"/testkeys";
            char[] passphrase = "passphrase".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(file), passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            ssl = SSLContext.getInstance ("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            throw new RuntimeException (e.getMessage());
        } catch (KeyStoreException e) {
            throw new RuntimeException (e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException (e.getMessage());
        } catch (CertificateException e) {
            throw new RuntimeException (e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException (e.getMessage());
        }
    }
    SSLContext get () {
        return ssl;
    }
}
