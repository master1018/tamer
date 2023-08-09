public class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
    private KeyStore keyStore;
    @Override
    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks != null) {
            keyStore = ks;
        } else {
            if (System.getProperty("javax.net.ssl.trustStore") == null) {
                String file = System.getProperty("java.home")
                    + java.io.File.separator + "etc" + java.io.File.separator
                    + "security" + java.io.File.separator
                    + "cacerts.bks";
                System.setProperty("javax.net.ssl.trustStore", file);
            }
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String keyStoreName = AccessController
                    .doPrivileged(new java.security.PrivilegedAction<String>() {
                        public String run() {
                            return System
                                    .getProperty("javax.net.ssl.trustStore");
                        }
                    });
            String keyStorePwd = null;
            if (keyStoreName == null || keyStoreName.equalsIgnoreCase("NONE")
                    || keyStoreName.length() == 0) {
                try {
                    keyStore.load(null, null);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            } else {
                keyStorePwd = AccessController
                        .doPrivileged(new java.security.PrivilegedAction<String>() {
                            public String run() {
                                return System
                                        .getProperty("javax.net.ssl.trustStorePassword");
                            }
                        });
                char[] pwd;
                if (keyStorePwd == null) {
                    pwd = new char[0];
                } else {
                    pwd = keyStorePwd.toCharArray();
                }
                try {
                    keyStore.load(new FileInputStream(new File(keyStoreName)), pwd);
                } catch (FileNotFoundException e) {
                    throw new KeyStoreException(e);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            }
        }
    }
    @Override
    public void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException(
                "ManagerFactoryParameters not supported");
    }
    @Override
    public TrustManager[] engineGetTrustManagers() {
        if (keyStore == null) {
            throw new IllegalStateException(
                    "TrustManagerFactory is not initialized");
        }
        return new TrustManager[] { new TrustManagerImpl(keyStore) };
    }
}
