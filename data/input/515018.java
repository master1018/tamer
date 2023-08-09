public abstract class LoginFilter implements InputFilter {
    private boolean mAppendInvalid;  
    LoginFilter(boolean appendInvalid) {
        mAppendInvalid = appendInvalid;
    }
    LoginFilter() {
        mAppendInvalid = false;
    }
    public CharSequence filter(CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
        onStart();
        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);
            if (!isAllowed(c)) onInvalidCharacter(c);
        }
        SpannableStringBuilder modification = null;
        int modoff = 0;
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (isAllowed(c)) {
                modoff++;
            } else {
                if (mAppendInvalid) {
                    modoff++;
                } else {
                    if (modification == null) {
                        modification = new SpannableStringBuilder(source, start, end);
                        modoff = i - start;
                    }
                    modification.delete(modoff, modoff + 1);
                }
                onInvalidCharacter(c);
            }
        }
        for (int i = dend; i < dest.length(); i++) {
            char c = dest.charAt(i);
            if (!isAllowed(c)) onInvalidCharacter(c);
        }
        onStop();
        return modification;
    }
    public void onStart() {
    }
    public void onInvalidCharacter(char c) {
    }
    public void onStop() {
    }
    public abstract boolean isAllowed(char c);
    public static class UsernameFilterGMail extends LoginFilter {
        public UsernameFilterGMail() {
            super(false);
        }
        public UsernameFilterGMail(boolean appendInvalid) {
            super(appendInvalid);
        }
        @Override
        public boolean isAllowed(char c) {
            if ('0' <= c && c <= '9')
                return true;
            if ('a' <= c && c <= 'z')
                return true;
            if ('A' <= c && c <= 'Z')
                return true;
            if ('.' == c)
                return true;
            return false;
        }
    }
    public static class UsernameFilterGeneric extends LoginFilter {
        private static final String mAllowed = "@_-+."; 
        public UsernameFilterGeneric() {
            super(false);
        }
        public UsernameFilterGeneric(boolean appendInvalid) {
            super(appendInvalid);
        }
        @Override
        public boolean isAllowed(char c) {
            if ('0' <= c && c <= '9')
                return true;
            if ('a' <= c && c <= 'z')
                return true;
            if ('A' <= c && c <= 'Z')
                return true;
            if (mAllowed.indexOf(c) != -1)
                return true;
            return false;
        }
    }
    public static class PasswordFilterGMail extends LoginFilter {
        public PasswordFilterGMail() {
            super(false);
        }
        public PasswordFilterGMail(boolean appendInvalid) {
            super(appendInvalid);
        }
        @Override
        public boolean isAllowed(char c) {
            if (32 <= c && c <= 127)
                return true; 
            if (160 <= c && c <= 255)
                return true; 
            return false;
        }
    }
}
