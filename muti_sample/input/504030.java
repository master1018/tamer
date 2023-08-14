    class Info {
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
        public final boolean equals(Object obj) {
            return this == obj;
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
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + ((description == null) ? 0 : description.hashCode());
            result = PRIME * result + ((name == null) ? 0 : name.hashCode());
            result = PRIME * result + ((vendor == null) ? 0 : vendor.hashCode());
            result = PRIME * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }
        @Override
        public final String toString() {
            return name;
        }
    }
    void close();
    MidiDevice.Info getDeviceInfo();
    int getMaxReceivers();
    int getMaxTransmitters();
    long getMicrosecondPosition();
    Receiver getReceiver() throws MidiUnavailableException;
    List<Receiver> getReceivers();
    Transmitter getTransmitter() throws MidiUnavailableException;
    List<Transmitter> getTransmitters();
    boolean isOpen();
    void open() throws MidiUnavailableException;
}
