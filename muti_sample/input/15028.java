class WindowsSecurity {
    private WindowsSecurity() { }
    private static long openProcessToken(int access) {
        try {
            return OpenProcessToken(GetCurrentProcess(), access);
        } catch (WindowsException x) {
            return 0L;
        }
    }
    static final long processTokenWithDuplicateAccess =
        openProcessToken(TOKEN_DUPLICATE);
    static final long processTokenWithQueryAccess =
        openProcessToken(TOKEN_QUERY);
    static interface Privilege {
        void drop();
    }
    static Privilege enablePrivilege(String priv) {
        final long pLuid;
        try {
            pLuid = LookupPrivilegeValue(priv);
        } catch (WindowsException x) {
            throw new AssertionError(x);
        }
        long hToken = 0L;
        boolean impersontating = false;
        boolean elevated = false;
        try {
            hToken = OpenThreadToken(GetCurrentThread(),
                                     TOKEN_ADJUST_PRIVILEGES, false);
            if (hToken == 0L && processTokenWithDuplicateAccess != 0L) {
                hToken = DuplicateTokenEx(processTokenWithDuplicateAccess,
                    (TOKEN_ADJUST_PRIVILEGES|TOKEN_IMPERSONATE));
                SetThreadToken(0L, hToken);
                impersontating = true;
            }
            if (hToken != 0L) {
                AdjustTokenPrivileges(hToken, pLuid, SE_PRIVILEGE_ENABLED);
                elevated = true;
            }
        } catch (WindowsException x) {
        }
        final long token = hToken;
        final boolean stopImpersontating = impersontating;
        final boolean needToRevert = elevated;
        return new Privilege() {
            @Override
            public void drop() {
                try {
                    if (stopImpersontating) {
                        SetThreadToken(0L, 0L);
                    } else {
                        if (needToRevert) {
                            AdjustTokenPrivileges(token, pLuid, 0);
                        }
                    }
                } catch (WindowsException x) {
                    throw new AssertionError(x);
                }
            }
        };
    }
}
