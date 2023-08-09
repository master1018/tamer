class JavaxSecurityAuthKerberosAccessImpl
        implements JavaxSecurityAuthKerberosAccess {
    public EncryptionKey[] keyTabGetEncryptionKeys(
            KeyTab ktab, PrincipalName principal) {
        return ktab.getEncryptionKeys(principal);
    }
}
