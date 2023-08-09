    public static class Info extends Line.Info {
        private String name;
        private boolean isSource;
        public static final Info MICROPHONE = new Info(Port.class,
                "MICROPHONE", true); 
        public static final Info LINE_IN = new Info(Port.class, "LINE_IN", true); 
        public static final Info COMPACT_DISC = new Info(Port.class,
                "COMPACT_DISC", true); 
        public static final Info SPEAKER = new Info(Port.class, "SPEAKER", 
                false);
        public static final Info HEADPHONE = new Info(Port.class, "HEADPHONES", 
                false);
        public static final Info LINE_OUT = new Info(Port.class, "LINE_OUT", 
                false);
        public Info(Class<?> lineClass, String name, boolean isSource) {
            super(lineClass);
            this.name = name;
            this.isSource = isSource;
        }
        public String getName() {
            return this.name;
        }
        public boolean isSource() {
            return this.isSource;
        }
        public boolean matches(Line.Info info) {
            if (super.matches(info) && Port.Info.class.equals(info.getClass())
                    && name.equals(((Port.Info) info).getName())
                    && isSource == ((Port.Info) info).isSource()) {
                return true;
            }
            return false;
        }
        public final boolean equals(Object obj) {
            return this == obj;
        }
        public final int hashCode() {
            return name.hashCode() ^ getLineClass().hashCode();
        }
        public final String toString() {
            return name + (isSource ? " source port" : " target port"); 
        }
    }
}
