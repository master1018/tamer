    static class LocalizedString implements Formattable {
        String key;
        public LocalizedString(String key) {
            this.key = key;
        }
        public String toString(java.util.Locale l, Messages messages) {
            return messages.getLocalizedString(l, key);
        }
        public String getKind() {
            return "LocalizedString";
        }
        public String toString() {
            return key;
        }
    }
}
