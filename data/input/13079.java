public class PKCS11 {
    private static final String PKCS11_WRAPPER = "j2pkcs11";
    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                System.loadLibrary(PKCS11_WRAPPER);
                return null;
            }
        });
        initializeLibrary();
    }
    public static void loadNative() {
    }
    private final String pkcs11ModulePath;
    private long pNativeData;
    private static native void initializeLibrary();
    private static native void finalizeLibrary();
    private static final Map<String,PKCS11> moduleMap =
        new HashMap<String,PKCS11>();
    PKCS11(String pkcs11ModulePath, String functionListName) throws IOException {
        connect(pkcs11ModulePath, functionListName);
        this.pkcs11ModulePath = pkcs11ModulePath;
    }
    public static synchronized PKCS11 getInstance(String pkcs11ModulePath, String functionList,
            CK_C_INITIALIZE_ARGS pInitArgs, boolean omitInitialize)
            throws IOException, PKCS11Exception {
        PKCS11 pkcs11 = moduleMap.get(pkcs11ModulePath);
        if (pkcs11 == null) {
            if ((pInitArgs != null)
                    && ((pInitArgs.flags & CKF_OS_LOCKING_OK) != 0)) {
                pkcs11 = new PKCS11(pkcs11ModulePath, functionList);
            } else {
                pkcs11 = new SynchronizedPKCS11(pkcs11ModulePath, functionList);
            }
            if (omitInitialize == false) {
                try {
                    pkcs11.C_Initialize(pInitArgs);
                } catch (PKCS11Exception e) {
                    if (e.getErrorCode() != CKR_CRYPTOKI_ALREADY_INITIALIZED) {
                        throw e;
                    }
                }
            }
            moduleMap.put(pkcs11ModulePath, pkcs11);
        }
        return pkcs11;
    }
    private native void connect(String pkcs11ModulePath, String functionListName) throws IOException;
    private native void disconnect();
    native void C_Initialize(Object pInitArgs) throws PKCS11Exception;
    public native void C_Finalize(Object pReserved) throws PKCS11Exception;
    public native CK_INFO C_GetInfo() throws PKCS11Exception;
    public native long[] C_GetSlotList(boolean tokenPresent) throws PKCS11Exception;
    public native CK_SLOT_INFO C_GetSlotInfo(long slotID) throws PKCS11Exception;
    public native CK_TOKEN_INFO C_GetTokenInfo(long slotID) throws PKCS11Exception;
    public native long[] C_GetMechanismList(long slotID) throws PKCS11Exception;
    public native CK_MECHANISM_INFO C_GetMechanismInfo(long slotID, long type) throws PKCS11Exception;
    public native long C_OpenSession(long slotID, long flags, Object pApplication, CK_NOTIFY Notify) throws PKCS11Exception;
    public native void C_CloseSession(long hSession) throws PKCS11Exception;
    public native CK_SESSION_INFO C_GetSessionInfo(long hSession) throws PKCS11Exception;
    public native void C_Login(long hSession, long userType, char[] pPin) throws PKCS11Exception;
    public native void C_Logout(long hSession) throws PKCS11Exception;
    public native long C_CreateObject(long hSession, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native long C_CopyObject(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native void C_DestroyObject(long hSession, long hObject) throws PKCS11Exception;
    public native void C_GetAttributeValue(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native void C_SetAttributeValue(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native void C_FindObjectsInit(long hSession, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native long[] C_FindObjects(long hSession, long ulMaxObjectCount) throws PKCS11Exception;
    public native void C_FindObjectsFinal(long hSession) throws PKCS11Exception;
    public native void C_EncryptInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native int C_Encrypt(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native int C_EncryptUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native int C_EncryptFinal(long hSession, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native void C_DecryptInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native int C_Decrypt(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native int C_DecryptUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native int C_DecryptFinal(long hSession, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception;
    public native void C_DigestInit(long hSession, CK_MECHANISM pMechanism) throws PKCS11Exception;
    public native int C_DigestSingle(long hSession, CK_MECHANISM pMechanism, byte[] in, int inOfs, int inLen, byte[] digest, int digestOfs, int digestLen) throws PKCS11Exception;
    public native void C_DigestUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception;
    public native void C_DigestKey(long hSession, long hKey) throws PKCS11Exception;
    public native int C_DigestFinal(long hSession, byte[] pDigest, int digestOfs, int digestLen) throws PKCS11Exception;
    public native void C_SignInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native byte[] C_Sign(long hSession, byte[] pData) throws PKCS11Exception;
    public native void C_SignUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception;
    public native byte[] C_SignFinal(long hSession, int expectedLen) throws PKCS11Exception;
    public native void C_SignRecoverInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native int C_SignRecover(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOufs, int outLen) throws PKCS11Exception;
    public native void C_VerifyInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native void C_Verify(long hSession, byte[] pData, byte[] pSignature) throws PKCS11Exception;
    public native void C_VerifyUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception;
    public native void C_VerifyFinal(long hSession, byte[] pSignature) throws PKCS11Exception;
    public native void C_VerifyRecoverInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception;
    public native int C_VerifyRecover(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOufs, int outLen) throws PKCS11Exception;
    public native long C_GenerateKey(long hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native long[] C_GenerateKeyPair(long hSession,
                                   CK_MECHANISM pMechanism,
                                   CK_ATTRIBUTE[] pPublicKeyTemplate,
                                   CK_ATTRIBUTE[] pPrivateKeyTemplate) throws PKCS11Exception;
    public native byte[] C_WrapKey(long hSession, CK_MECHANISM pMechanism, long hWrappingKey, long hKey) throws PKCS11Exception;
    public native long C_UnwrapKey(long hSession, CK_MECHANISM pMechanism,
                          long hUnwrappingKey, byte[] pWrappedKey,
                          CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native long C_DeriveKey(long hSession, CK_MECHANISM pMechanism,
                          long hBaseKey, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception;
    public native void C_SeedRandom(long hSession, byte[] pSeed) throws PKCS11Exception;
    public native void C_GenerateRandom(long hSession, byte[] randomData) throws PKCS11Exception;
    public String toString() {
        return "Module name: " + pkcs11ModulePath;
    }
    public void finalize() throws Throwable {
        disconnect();
    }
static class SynchronizedPKCS11 extends PKCS11 {
    SynchronizedPKCS11(String pkcs11ModulePath, String functionListName) throws IOException {
        super(pkcs11ModulePath, functionListName);
    }
    synchronized void C_Initialize(Object pInitArgs) throws PKCS11Exception {
        super.C_Initialize(pInitArgs);
    }
    public synchronized void C_Finalize(Object pReserved) throws PKCS11Exception {
        super.C_Finalize(pReserved);
    }
    public synchronized CK_INFO C_GetInfo() throws PKCS11Exception {
        return super.C_GetInfo();
    }
    public synchronized long[] C_GetSlotList(boolean tokenPresent) throws PKCS11Exception {
        return super.C_GetSlotList(tokenPresent);
    }
    public synchronized CK_SLOT_INFO C_GetSlotInfo(long slotID) throws PKCS11Exception {
        return super.C_GetSlotInfo(slotID);
    }
    public synchronized CK_TOKEN_INFO C_GetTokenInfo(long slotID) throws PKCS11Exception {
        return super.C_GetTokenInfo(slotID);
    }
    public synchronized long[] C_GetMechanismList(long slotID) throws PKCS11Exception {
        return super.C_GetMechanismList(slotID);
    }
    public synchronized CK_MECHANISM_INFO C_GetMechanismInfo(long slotID, long type) throws PKCS11Exception {
        return super.C_GetMechanismInfo(slotID, type);
    }
    public synchronized long C_OpenSession(long slotID, long flags, Object pApplication, CK_NOTIFY Notify) throws PKCS11Exception {
        return super.C_OpenSession(slotID, flags, pApplication, Notify);
    }
    public synchronized void C_CloseSession(long hSession) throws PKCS11Exception {
        super.C_CloseSession(hSession);
    }
    public synchronized CK_SESSION_INFO C_GetSessionInfo(long hSession) throws PKCS11Exception {
        return super.C_GetSessionInfo(hSession);
    }
    public synchronized void C_Login(long hSession, long userType, char[] pPin) throws PKCS11Exception {
        super.C_Login(hSession, userType, pPin);
    }
    public synchronized void C_Logout(long hSession) throws PKCS11Exception {
        super.C_Logout(hSession);
    }
    public synchronized long C_CreateObject(long hSession, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        return super.C_CreateObject(hSession, pTemplate);
    }
    public synchronized long C_CopyObject(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        return super.C_CopyObject(hSession, hObject, pTemplate);
    }
    public synchronized void C_DestroyObject(long hSession, long hObject) throws PKCS11Exception {
        super.C_DestroyObject(hSession, hObject);
    }
    public synchronized void C_GetAttributeValue(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        super.C_GetAttributeValue(hSession, hObject, pTemplate);
    }
    public synchronized void C_SetAttributeValue(long hSession, long hObject, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        super.C_SetAttributeValue(hSession, hObject, pTemplate);
    }
    public synchronized void C_FindObjectsInit(long hSession, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        super.C_FindObjectsInit(hSession, pTemplate);
    }
    public synchronized long[] C_FindObjects(long hSession, long ulMaxObjectCount) throws PKCS11Exception {
        return super.C_FindObjects(hSession, ulMaxObjectCount);
    }
    public synchronized void C_FindObjectsFinal(long hSession) throws PKCS11Exception {
        super.C_FindObjectsFinal(hSession);
    }
    public synchronized void C_EncryptInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_EncryptInit(hSession, pMechanism, hKey);
    }
    public synchronized int C_Encrypt(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_Encrypt(hSession, in, inOfs, inLen, out, outOfs, outLen);
    }
    public synchronized int C_EncryptUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_EncryptUpdate(hSession, directIn, in, inOfs, inLen, directOut, out, outOfs, outLen);
    }
    public synchronized int C_EncryptFinal(long hSession, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_EncryptFinal(hSession, directOut, out, outOfs, outLen);
    }
    public synchronized void C_DecryptInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_DecryptInit(hSession, pMechanism, hKey);
    }
    public synchronized int C_Decrypt(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_Decrypt(hSession, in, inOfs, inLen, out, outOfs, outLen);
    }
    public synchronized int C_DecryptUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_DecryptUpdate(hSession, directIn, in, inOfs, inLen, directOut, out, outOfs, outLen);
    }
    public synchronized int C_DecryptFinal(long hSession, long directOut, byte[] out, int outOfs, int outLen) throws PKCS11Exception {
        return super.C_DecryptFinal(hSession, directOut, out, outOfs, outLen);
    }
    public synchronized void C_DigestInit(long hSession, CK_MECHANISM pMechanism) throws PKCS11Exception {
        super.C_DigestInit(hSession, pMechanism);
    }
    public synchronized int C_DigestSingle(long hSession, CK_MECHANISM pMechanism, byte[] in, int inOfs, int inLen, byte[] digest, int digestOfs, int digestLen) throws PKCS11Exception {
        return super.C_DigestSingle(hSession, pMechanism, in, inOfs, inLen, digest, digestOfs, digestLen);
    }
    public synchronized void C_DigestUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception {
        super.C_DigestUpdate(hSession, directIn, in, inOfs, inLen);
    }
    public synchronized void C_DigestKey(long hSession, long hKey) throws PKCS11Exception {
        super.C_DigestKey(hSession, hKey);
    }
    public synchronized int C_DigestFinal(long hSession, byte[] pDigest, int digestOfs, int digestLen) throws PKCS11Exception {
        return super.C_DigestFinal(hSession, pDigest, digestOfs, digestLen);
    }
    public synchronized void C_SignInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_SignInit(hSession, pMechanism, hKey);
    }
    public synchronized byte[] C_Sign(long hSession, byte[] pData) throws PKCS11Exception {
        return super.C_Sign(hSession, pData);
    }
    public synchronized void C_SignUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception {
        super.C_SignUpdate(hSession, directIn, in, inOfs, inLen);
    }
    public synchronized byte[] C_SignFinal(long hSession, int expectedLen) throws PKCS11Exception {
        return super.C_SignFinal(hSession, expectedLen);
    }
    public synchronized void C_SignRecoverInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_SignRecoverInit(hSession, pMechanism, hKey);
    }
    public synchronized int C_SignRecover(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOufs, int outLen) throws PKCS11Exception {
        return super.C_SignRecover(hSession, in, inOfs, inLen, out, outOufs, outLen);
    }
    public synchronized void C_VerifyInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_VerifyInit(hSession, pMechanism, hKey);
    }
    public synchronized void C_Verify(long hSession, byte[] pData, byte[] pSignature) throws PKCS11Exception {
        super.C_Verify(hSession, pData, pSignature);
    }
    public synchronized void C_VerifyUpdate(long hSession, long directIn, byte[] in, int inOfs, int inLen) throws PKCS11Exception {
        super.C_VerifyUpdate(hSession, directIn, in, inOfs, inLen);
    }
    public synchronized void C_VerifyFinal(long hSession, byte[] pSignature) throws PKCS11Exception {
        super.C_VerifyFinal(hSession, pSignature);
    }
    public synchronized void C_VerifyRecoverInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws PKCS11Exception {
        super.C_VerifyRecoverInit(hSession, pMechanism, hKey);
    }
    public synchronized int C_VerifyRecover(long hSession, byte[] in, int inOfs, int inLen, byte[] out, int outOufs, int outLen) throws PKCS11Exception {
        return super.C_VerifyRecover(hSession, in, inOfs, inLen, out, outOufs, outLen);
    }
    public synchronized long C_GenerateKey(long hSession, CK_MECHANISM pMechanism, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        return super.C_GenerateKey(hSession, pMechanism, pTemplate);
    }
    public synchronized long[] C_GenerateKeyPair(long hSession,
                                   CK_MECHANISM pMechanism,
                                   CK_ATTRIBUTE[] pPublicKeyTemplate,
                                   CK_ATTRIBUTE[] pPrivateKeyTemplate) throws PKCS11Exception {
        return super.C_GenerateKeyPair(hSession, pMechanism, pPublicKeyTemplate, pPrivateKeyTemplate);
    }
    public synchronized byte[] C_WrapKey(long hSession, CK_MECHANISM pMechanism, long hWrappingKey, long hKey) throws PKCS11Exception {
        return super.C_WrapKey(hSession, pMechanism, hWrappingKey, hKey);
    }
    public synchronized long C_UnwrapKey(long hSession, CK_MECHANISM pMechanism,
                          long hUnwrappingKey, byte[] pWrappedKey,
                          CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        return super.C_UnwrapKey(hSession, pMechanism, hUnwrappingKey, pWrappedKey, pTemplate);
    }
    public synchronized long C_DeriveKey(long hSession, CK_MECHANISM pMechanism,
    long hBaseKey, CK_ATTRIBUTE[] pTemplate) throws PKCS11Exception {
        return super.C_DeriveKey(hSession, pMechanism, hBaseKey, pTemplate);
    }
    public synchronized void C_SeedRandom(long hSession, byte[] pSeed) throws PKCS11Exception {
        super.C_SeedRandom(hSession, pSeed);
    }
    public synchronized void C_GenerateRandom(long hSession, byte[] randomData) throws PKCS11Exception {
        super.C_GenerateRandom(hSession, randomData);
    }
}
}
