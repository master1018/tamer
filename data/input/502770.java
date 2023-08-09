public class RecoverySystem {
    private static final String TAG = "RecoverySystem";
    private static final File DEFAULT_KEYSTORE =
        new File("/system/etc/security/otacerts.zip");
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static int LOG_FILE_MAX_LENGTH = 64 * 1024;
    public interface ProgressListener {
        public void onProgress(int progress);
    }
    private static HashSet<Certificate> getTrustedCerts(File keystore)
        throws IOException, GeneralSecurityException {
        HashSet<Certificate> trusted = new HashSet<Certificate>();
        if (keystore == null) {
            keystore = DEFAULT_KEYSTORE;
        }
        ZipFile zip = new ZipFile(keystore);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                trusted.add(cf.generateCertificate(zip.getInputStream(entry)));
            }
        } finally {
            zip.close();
        }
        return trusted;
    }
    public static void verifyPackage(File packageFile,
                                     ProgressListener listener,
                                     File deviceCertsZipFile)
        throws IOException, GeneralSecurityException {
        long fileLen = packageFile.length();
        RandomAccessFile raf = new RandomAccessFile(packageFile, "r");
        try {
            int lastPercent = 0;
            long lastPublishTime = System.currentTimeMillis();
            if (listener != null) {
                listener.onProgress(lastPercent);
            }
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);
            if (footer[2] != (byte)0xff || footer[3] != (byte)0xff) {
                throw new SignatureException("no signature in file (no footer)");
            }
            int commentSize = (footer[4] & 0xff) | ((footer[5] & 0xff) << 8);
            int signatureStart = (footer[0] & 0xff) | ((footer[1] & 0xff) << 8);
            Log.v(TAG, String.format("comment size %d; signature start %d",
                                     commentSize, signatureStart));
            byte[] eocd = new byte[commentSize + 22];
            raf.seek(fileLen - (commentSize + 22));
            raf.readFully(eocd);
            if (eocd[0] != (byte)0x50 || eocd[1] != (byte)0x4b ||
                eocd[2] != (byte)0x05 || eocd[3] != (byte)0x06) {
                throw new SignatureException("no signature in file (bad footer)");
            }
            for (int i = 4; i < eocd.length-3; ++i) {
                if (eocd[i  ] == (byte)0x50 && eocd[i+1] == (byte)0x4b &&
                    eocd[i+2] == (byte)0x05 && eocd[i+3] == (byte)0x06) {
                    throw new SignatureException("EOCD marker found after start of EOCD");
                }
            }
            BerInputStream bis = new BerInputStream(
                new ByteArrayInputStream(eocd, commentSize+22-signatureStart, signatureStart));
            ContentInfo info = (ContentInfo)ContentInfo.ASN1.decode(bis);
            SignedData signedData = info.getSignedData();
            if (signedData == null) {
                throw new IOException("signedData is null");
            }
            Collection encCerts = signedData.getCertificates();
            if (encCerts.isEmpty()) {
                throw new IOException("encCerts is empty");
            }
            Iterator it = encCerts.iterator();
            X509Certificate cert = null;
            if (it.hasNext()) {
                cert = new X509CertImpl((org.apache.harmony.security.x509.Certificate)it.next());
            } else {
                throw new SignatureException("signature contains no certificates");
            }
            List sigInfos = signedData.getSignerInfos();
            SignerInfo sigInfo;
            if (!sigInfos.isEmpty()) {
                sigInfo = (SignerInfo)sigInfos.get(0);
            } else {
                throw new IOException("no signer infos!");
            }
            HashSet<Certificate> trusted = getTrustedCerts(
                deviceCertsZipFile == null ? DEFAULT_KEYSTORE : deviceCertsZipFile);
            PublicKey signatureKey = cert.getPublicKey();
            boolean verified = false;
            for (Certificate c : trusted) {
                if (c.getPublicKey().equals(signatureKey)) {
                    verified = true;
                    break;
                }
            }
            if (!verified) {
                throw new SignatureException("signature doesn't match any trusted key");
            }
            String da = sigInfo.getdigestAlgorithm();
            String dea = sigInfo.getDigestEncryptionAlgorithm();
            String alg = null;
            if (da == null || dea == null) {
                alg = cert.getSigAlgName();
            } else {
                alg = da + "with" + dea;
            }
            Signature sig = Signature.getInstance(alg);
            sig.initVerify(cert);
            long toRead = fileLen - commentSize - 2;
            long soFar = 0;
            raf.seek(0);
            byte[] buffer = new byte[4096];
            boolean interrupted = false;
            while (soFar < toRead) {
                interrupted = Thread.interrupted();
                if (interrupted) break;
                int size = buffer.length;
                if (soFar + size > toRead) {
                    size = (int)(toRead - soFar);
                }
                int read = raf.read(buffer, 0, size);
                sig.update(buffer, 0, read);
                soFar += read;
                if (listener != null) {
                    long now = System.currentTimeMillis();
                    int p = (int)(soFar * 100 / toRead);
                    if (p > lastPercent &&
                        now - lastPublishTime > PUBLISH_PROGRESS_INTERVAL_MS) {
                        lastPercent = p;
                        lastPublishTime = now;
                        listener.onProgress(lastPercent);
                    }
                }
            }
            if (listener != null) {
                listener.onProgress(100);
            }
            if (interrupted) {
                throw new SignatureException("verification was interrupted");
            }
            if (!sig.verify(sigInfo.getEncryptedDigest())) {
                throw new SignatureException("signature digest verification failed");
            }
        } finally {
            raf.close();
        }
    }
    public static void installPackage(Context context, File packageFile)
        throws IOException {
        String filename = packageFile.getCanonicalPath();
        if (filename.startsWith("/cache/")) {
            filename = "CACHE:" + filename.substring(7);
        } else if (filename.startsWith("/data/")) {
            filename = "DATA:" + filename.substring(6);
        } else {
            throw new IllegalArgumentException(
                "Must start with /cache or /data: " + filename);
        }
        Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
        String arg = "--update_package=" + filename;
        bootCommand(context, arg);
    }
    public static void rebootWipeUserData(Context context)
        throws IOException {
        bootCommand(context, "--wipe_data");
    }
    private static void bootCommand(Context context, String arg) throws IOException {
        RECOVERY_DIR.mkdirs();  
        COMMAND_FILE.delete();  
        LOG_FILE.delete();
        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg);
            command.write("\n");
        } finally {
            command.close();
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");
        throw new IOException("Reboot failed (no permissions?)");
    }
    public static String handleAftermath() {
        String log = null;
        try {
            log = FileUtils.readTextFile(LOG_FILE, -LOG_FILE_MAX_LENGTH, "...\n");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No recovery log file");
        } catch (IOException e) {
            Log.e(TAG, "Error reading recovery log", e);
        }
        String[] names = RECOVERY_DIR.list();
        for (int i = 0; names != null && i < names.length; i++) {
            File f = new File(RECOVERY_DIR, names[i]);
            if (!f.delete()) {
                Log.e(TAG, "Can't delete: " + f);
            } else {
                Log.i(TAG, "Deleted: " + f);
            }
        }
        return log;
    }
    private void RecoverySystem() { }  
}
