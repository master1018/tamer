    public static class Factory {
        private static Spannable.Factory sInstance = new Spannable.Factory();
        public static Spannable.Factory getInstance() {
            return sInstance;
        }
        public Spannable newSpannable(CharSequence source) {
            return new SpannableString(source);
        }
    }
}
