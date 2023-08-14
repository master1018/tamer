    public static class SyncMode {
        private String name;
        protected SyncMode(String name) {
            this.name = name;
        }
        public final boolean equals(Object obj) {
            return super.equals(obj);
        }
        public final int hashCode() {
            return super.hashCode();
        }
        public final String toString() {
            return name;
        }
        public static final SyncMode INTERNAL_CLOCK             = new SyncMode("Internal Clock");
        public static final SyncMode MIDI_SYNC                  = new SyncMode("MIDI Sync");
        public static final SyncMode MIDI_TIME_CODE             = new SyncMode("MIDI Time Code");
        public static final SyncMode NO_SYNC                            = new SyncMode("No Timing");
    } 
}
