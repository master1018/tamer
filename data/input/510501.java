    public static class Info {
        private String name;
        private String vendor;
        private String description;
        private String version;        
        protected Info(String name, String vendor, String description, String version) {
            this.name = name;
            this.vendor = vendor;
            this.description = description;
            this.version = version;
        }
        @Override
        public final boolean equals(Object another) {
            return this == another;
        }
        public final String getDescription() {
            return description;
        }
        public final String getName() {
            return name;
        }
        public final String getVendor() {
            return vendor;
        }
        public final String getVersion() {
            return version;
        }
        @Override
        public final int hashCode() {
            return name.hashCode() + vendor.hashCode() + description.hashCode() + version.hashCode();
        }
        @Override
        public final String toString() {
            return name + ", version " + version; 
        }
    }
    Line getLine(Line.Info info) throws LineUnavailableException;
    int getMaxLines(Line.Info info);
    Mixer.Info getMixerInfo();
    Line.Info[] getSourceLineInfo();
    Line.Info[] getSourceLineInfo(Line.Info info);
    Line[] getSourceLines();
    Line.Info[] getTargetLineInfo();
    Line.Info[] getTargetLineInfo(Line.Info info);
    Line[] getTargetLines();
    boolean isLineSupported(Line.Info info);
    boolean isSynchronizationSupported(Line[] lines, boolean maintainSync);
    void synchronize(Line[] lines, boolean maintainSync);
    void unsynchronize(Line[] lines);
}
