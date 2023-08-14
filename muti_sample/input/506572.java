public class ConnectionStateTLS extends ConnectionState {
    private static byte[] KEY_EXPANSION_LABEL = {
        (byte) 0x6B, (byte) 0x65, (byte) 0x79, (byte) 0x20, (byte) 0x65, 
        (byte) 0x78, (byte) 0x70, (byte) 0x61, (byte) 0x6E, (byte) 0x73, 
        (byte) 0x69, (byte) 0x6F, (byte) 0x6E };
    private static byte[] CLIENT_WRITE_KEY_LABEL = {
        (byte) 0x63, (byte) 0x6C, (byte) 0x69, (byte) 0x65, (byte) 0x6E, 
        (byte) 0x74, (byte) 0x20, (byte) 0x77, (byte) 0x72, (byte) 0x69, 
        (byte) 0x74, (byte) 0x65, (byte) 0x20, (byte) 0x6B, (byte) 0x65, 
        (byte) 0x79 };
    private static byte[] SERVER_WRITE_KEY_LABEL = {
        (byte) 0x73, (byte) 0x65, (byte) 0x72, (byte) 0x76, (byte) 0x65, 
        (byte) 0x72, (byte) 0x20, (byte) 0x77, (byte) 0x72, (byte) 0x69, 
        (byte) 0x74, (byte) 0x65, (byte) 0x20, (byte) 0x6B, (byte) 0x65, 
        (byte) 0x79 };
    private static byte[] IV_BLOCK_LABEL = {
        (byte) 0x49, (byte) 0x56, (byte) 0x20, (byte) 0x62, (byte) 0x6C, 
        (byte) 0x6F, (byte) 0x63, (byte) 0x6B };
    private final Mac encMac;
    private final Mac decMac;
    private final byte[] mac_material_header = new byte[] {0, 3, 1, 0, 0};
    protected ConnectionStateTLS(SSLSessionImpl session) {
        try {
            CipherSuite cipherSuite = session.cipherSuite;
            hash_size = cipherSuite.getMACLength();
            boolean is_exportabe =  cipherSuite.isExportable();
            int key_size = (is_exportabe)
                ? cipherSuite.keyMaterial
                : cipherSuite.expandedKeyMaterial;
            int iv_size = cipherSuite.getBlockSize();
            String algName = cipherSuite.getBulkEncryptionAlgorithm();
            String macName = cipherSuite.getHmacName();
            if (logger != null) {
                logger.println("ConnectionStateTLS.create:");
                logger.println("  cipher suite name: "
                                            + cipherSuite.getName());
                logger.println("  encryption alg name: " + algName);
                logger.println("  mac alg name: " + macName);
                logger.println("  hash size: " + hash_size);
                logger.println("  block size: " + iv_size);
                logger.println("  IV size (== block size):" + iv_size);
                logger.println("  key size: " + key_size);
            }
            byte[] clientRandom = session.clientRandom;
            byte[] serverRandom = session.serverRandom;
            byte[] key_block = new byte[2*hash_size + 2*key_size + 2*iv_size];
            byte[] seed = new byte[clientRandom.length + serverRandom.length];
            System.arraycopy(serverRandom, 0, seed, 0, serverRandom.length);
            System.arraycopy(clientRandom, 0, seed, serverRandom.length,
                    clientRandom.length);
            PRF.computePRF(key_block, session.master_secret,
                    KEY_EXPANSION_LABEL, seed);
            byte[] client_mac_secret = new byte[hash_size];
            byte[] server_mac_secret = new byte[hash_size];
            byte[] client_key = new byte[key_size];
            byte[] server_key = new byte[key_size];
            boolean is_client = !session.isServer;
            is_block_cipher = (iv_size > 0);
            System.arraycopy(key_block, 0, client_mac_secret, 0, hash_size);
            System.arraycopy(key_block, hash_size,
                    server_mac_secret, 0, hash_size);
            System.arraycopy(key_block, 2*hash_size, client_key, 0, key_size);
            System.arraycopy(key_block, 2*hash_size+key_size,
                    server_key, 0, key_size);
            IvParameterSpec clientIV = null;
            IvParameterSpec serverIV = null;
            if (is_exportabe) {
                System.arraycopy(clientRandom, 0,
                        seed, 0, clientRandom.length);
                System.arraycopy(serverRandom, 0,
                        seed, clientRandom.length, serverRandom.length);
                byte[] final_client_key =
                    new byte[cipherSuite.expandedKeyMaterial];
                byte[] final_server_key =
                    new byte[cipherSuite.expandedKeyMaterial];
                PRF.computePRF(final_client_key, client_key,
                        CLIENT_WRITE_KEY_LABEL, seed);
                PRF.computePRF(final_server_key, server_key,
                        SERVER_WRITE_KEY_LABEL, seed);
                client_key = final_client_key;
                server_key = final_server_key;
                if (is_block_cipher) {
                    byte[] iv_block = new byte[2*iv_size];
                    PRF.computePRF(iv_block, null, IV_BLOCK_LABEL, seed);
                    clientIV = new IvParameterSpec(iv_block, 0, iv_size);
                    serverIV = new IvParameterSpec(iv_block, iv_size, iv_size);
                }
            } else if (is_block_cipher) {
                clientIV = new IvParameterSpec(key_block,
                        2*(hash_size+key_size), iv_size);
                serverIV = new IvParameterSpec(key_block,
                        2*(hash_size+key_size)+iv_size, iv_size);
            }
            if (logger != null) {
                logger.println("is exportable: "+is_exportabe);
                logger.println("master_secret");
                logger.print(session.master_secret);
                logger.println("client_random");
                logger.print(clientRandom);
                logger.println("server_random");
                logger.print(serverRandom);
                logger.println("client_mac_secret");
                logger.print(client_mac_secret);
                logger.println("server_mac_secret");
                logger.print(server_mac_secret);
                logger.println("client_key");
                logger.print(client_key);
                logger.println("server_key");
                logger.print(server_key);
                if (clientIV == null) {
                    logger.println("no IV.");
                } else {
                    logger.println("client_iv");
                    logger.print(clientIV.getIV());
                    logger.println("server_iv");
                    logger.print(serverIV.getIV());
                }
            }
            encCipher = Cipher.getInstance(algName);
            decCipher = Cipher.getInstance(algName);
            encMac = Mac.getInstance(macName);
            decMac = Mac.getInstance(macName);
            if (is_client) { 
                encCipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(client_key, algName), clientIV);
                decCipher.init(Cipher.DECRYPT_MODE,
                        new SecretKeySpec(server_key, algName), serverIV);
                encMac.init(new SecretKeySpec(client_mac_secret, macName));
                decMac.init(new SecretKeySpec(server_mac_secret, macName));
            } else { 
                encCipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(server_key, algName), serverIV);
                decCipher.init(Cipher.DECRYPT_MODE,
                        new SecretKeySpec(client_key, algName), clientIV);
                encMac.init(new SecretKeySpec(server_mac_secret, macName));
                decMac.init(new SecretKeySpec(client_mac_secret, macName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AlertException(AlertProtocol.INTERNAL_ERROR,
                    new SSLProtocolException(
                        "Error during computation of security parameters"));
        }
    }
    @Override
    protected byte[] encrypt(byte type, byte[] fragment, int offset, int len) {
        try {
            int content_mac_length = len + hash_size;
            int padding_length = is_block_cipher
                    ? ((8 - (++content_mac_length & 0x07)) & 0x07)
                    : 0;
            byte[] res = new byte[content_mac_length + padding_length];
            System.arraycopy(fragment, offset, res, 0, len);
            mac_material_header[0] = type;
            mac_material_header[3] = (byte) ((0x00FF00 & len) >> 8);
            mac_material_header[4] = (byte) (0x0000FF & len);
            encMac.update(write_seq_num);
            encMac.update(mac_material_header);
            encMac.update(fragment, offset, len);
            encMac.doFinal(res, len);
            if (is_block_cipher) {
                Arrays.fill(res, content_mac_length-1,
                        res.length, (byte) (padding_length));
            }
            if (logger != null) {
                logger.println("SSLRecordProtocol.do_encryption: Generic"
                        + (is_block_cipher
                            ? "BlockCipher with padding["+padding_length+"]:"
                            : "StreamCipher:"));
                logger.print(res);
            }
            byte[] rez = new byte[encCipher.getOutputSize(res.length)];
            encCipher.update(res, 0, res.length, rez);
            incSequenceNumber(write_seq_num);
            return rez;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new AlertException(AlertProtocol.INTERNAL_ERROR,
                    new SSLProtocolException("Error during the encryption"));
        }
    }
    @Override
    protected byte[] decrypt(byte type, byte[] fragment,
            int offset, int len) {
        byte[] data = decCipher.update(fragment, offset, len);
        byte[] content;
        if (is_block_cipher) {
            int padding_length = data[data.length-1];
            for (int i=0; i<padding_length; i++) {
                if (data[data.length-2-i] != padding_length) {
                    throw new AlertException(
                            AlertProtocol.DECRYPTION_FAILED,
                            new SSLProtocolException(
                                "Received message has bad padding"));
                }
            }
            content = new byte[data.length - hash_size - padding_length - 1];
        } else {
            content = new byte[data.length - hash_size];
        }
        mac_material_header[0] = type;
        mac_material_header[3] = (byte) ((0x00FF00 & content.length) >> 8);
        mac_material_header[4] = (byte) (0x0000FF & content.length);
        decMac.update(read_seq_num);
        decMac.update(mac_material_header);
        decMac.update(data, 0, content.length); 
        byte[] mac_value = decMac.doFinal();
        if (logger != null) {
            logger.println("Decrypted:");
            logger.print(data);
            logger.println("Expected mac value:");
            logger.print(mac_value);
        }
        for (int i=0; i<hash_size; i++) {
            if (mac_value[i] != data[i+content.length]) {
                throw new AlertException(AlertProtocol.BAD_RECORD_MAC,
                        new SSLProtocolException("Bad record MAC"));
            }
        }
        System.arraycopy(data, 0, content, 0, content.length);
        incSequenceNumber(read_seq_num);
        return content;
    }
}
