    class Info extends Line.Info {
        private AudioFormat[] formats;
        private int minBufferSize;
        private int maxBufferSize;
        public Info(Class <?> lineClass, AudioFormat format) {
            super(lineClass);
            this.formats = new AudioFormat[] { format };
            this.minBufferSize = AudioSystem.NOT_SPECIFIED;
            this.maxBufferSize = AudioSystem.NOT_SPECIFIED;
        }
        public Info(Class <?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
            super(lineClass);
            this.formats = formats;
            this.minBufferSize = minBufferSize;
            this.maxBufferSize = maxBufferSize;
        }
        public Info(Class <?> lineClass, AudioFormat format, int bufferSize) {
            super(lineClass);
            this.formats = new AudioFormat[] { format };
            this.minBufferSize = bufferSize;
            this.maxBufferSize = bufferSize;
        }
        public AudioFormat[] getFormats(){
            return formats;
        }
        public boolean isFormatSupported(AudioFormat format) {
            if (formats == null) {
                return false;
            }
            for (AudioFormat supported : formats) {
                if (format.matches(supported)) {
                    return true;
                }
            }
            return false;
        }
        public int getMinBufferSize() {
            return minBufferSize;
        }
        public int getMaxBufferSize() {
            return maxBufferSize;
        }
        @Override
        public boolean matches(Line.Info info) {
            if (!super.matches(info)) {
                return false;
            }
            DataLine.Info inf = (DataLine.Info)info;
            if ((minBufferSize != AudioSystem.NOT_SPECIFIED
                    && inf.getMinBufferSize() != AudioSystem.NOT_SPECIFIED 
                    && minBufferSize < inf.getMinBufferSize())
                    || (maxBufferSize != AudioSystem.NOT_SPECIFIED
                            && inf.getMaxBufferSize() != AudioSystem.NOT_SPECIFIED
                            && maxBufferSize > inf.getMaxBufferSize())) {
                return false;
            }
            for (AudioFormat supported : formats) {
                if (!inf.isFormatSupported(supported)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public String toString() {
            String formatStr = (formats.length == 1? "format " + formats[0].toString() 
                    : formats.length + " audio formats"); 
            String bufStr = ""; 
            if (minBufferSize != AudioSystem.NOT_SPECIFIED) {
                bufStr = "and buffers of " + minBufferSize + 
                    " to " + maxBufferSize + " bytes"; 
            }
            return getLineClass() + " supporting " + formatStr + ", " + bufStr; 
        }
    }
    int available();
    void drain();
    void flush();
    int getBufferSize();
    AudioFormat getFormat();
    int getFramePosition();
    float getLevel();
    long getLongFramePosition();
    long getMicrosecondPosition();
    boolean isActive();
    boolean isRunning();
    void start();
    void stop();
}
