public class DigitalSignature {
    private final MessageDigest md5;
    private final MessageDigest sha;
    private final Signature signature;
    private final Cipher cipher;
    private byte[] md5_hash;
    private byte[] sha_hash;
    public DigitalSignature(int keyExchange) {
        try {
            sha = MessageDigest.getInstance("SHA-1");
            if (keyExchange == CipherSuite.KeyExchange_RSA_EXPORT ||
                    keyExchange == CipherSuite.KeyExchange_RSA ||
                    keyExchange == CipherSuite.KeyExchange_DHE_RSA ||
                    keyExchange == CipherSuite.KeyExchange_DHE_RSA_EXPORT) {
                md5 = MessageDigest.getInstance("MD5");
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                signature = null;
            } else if (keyExchange == CipherSuite.KeyExchange_DHE_DSS ||
                    keyExchange == CipherSuite.KeyExchange_DHE_DSS_EXPORT ) {
                signature = Signature.getInstance("NONEwithDSA");
                cipher = null;
                md5 = null;
            } else {
                cipher = null;
                signature = null;
                md5 = null;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }
    public void init(PrivateKey key) {
        try {
            if (signature != null) {
                signature.initSign(key);
            } else if (cipher != null) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
        } catch (InvalidKeyException e){
            throw new AlertException(AlertProtocol.BAD_CERTIFICATE,
                    new SSLException("init - invalid private key", e));
        }
    }
    public void init(Certificate cert) {
        try {
            if (signature != null) {
                signature.initVerify(cert);
            } else if (cipher != null) {
                cipher.init(Cipher.DECRYPT_MODE, cert);
            }
        } catch (InvalidKeyException e){
            throw new AlertException(AlertProtocol.BAD_CERTIFICATE,
                    new SSLException("init - invalid certificate", e));
        }
    }
    public void update(byte[] data) {
        if (sha != null) {
            sha.update(data);
        }
        if (md5 != null) {
            md5.update(data);
        }
    }
    public void setMD5(byte[] data) {
        md5_hash = data;    
    }
    public void setSHA(byte[] data) {
        sha_hash = data;    
    }
    public byte[] sign() {
        try {
            if (md5 != null && md5_hash == null) {
                md5_hash = new byte[16];
                md5.digest(md5_hash, 0, md5_hash.length);
            }    
            if (md5_hash != null) {
                if (signature != null) {
                    signature.update(md5_hash);
                } else if (cipher != null) {
                    cipher.update(md5_hash);
                }
            }
            if (sha != null && sha_hash == null) {
                sha_hash = new byte[20];
                sha.digest(sha_hash, 0, sha_hash.length);
            }
            if (sha_hash != null) {
                if (signature != null) {
                    signature.update(sha_hash);
                } else if (cipher != null) {
                    cipher.update(sha_hash);
                }
            }
            if (signature != null) {
                return signature.sign();
            } else if (cipher != null) {
                return cipher.doFinal();
            } 
            return new byte[0];
        } catch (DigestException e){
            return new byte[0];
        } catch (SignatureException e){
            return new byte[0];
        } catch (BadPaddingException e){
            return new byte[0];
        } catch (IllegalBlockSizeException e){
            return new byte[0];
        }
    }
    public boolean verifySignature(byte[] data) {
        if (signature != null) {
            try {
                return signature.verify(data);
            } catch (SignatureException e) {
                return false;
            }
        }
        if (cipher != null) {
            final byte[] decrypt;
            try {
                decrypt = cipher.doFinal(data);
            } catch (IllegalBlockSizeException e) {
                return false;
            } catch (BadPaddingException e) {
                return false;
            }
            final byte[] md5_sha;
            if (md5_hash != null && sha_hash != null) {
                md5_sha = new byte[md5_hash.length + sha_hash.length];
                System.arraycopy(md5_hash, 0, md5_sha, 0, md5_hash.length);
                System.arraycopy(sha_hash, 0, md5_sha, md5_hash.length, sha_hash.length);
            } else if (md5_hash != null) {
                md5_sha = md5_hash;
            } else {
                md5_sha = sha_hash;
            }
            return Arrays.equals(decrypt, md5_sha);
        } else if (data == null || data.length == 0) {
            return true;
        } else {
            return false;
        }
    }
}
