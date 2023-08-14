    class NativeAudioStream extends FilterInputStream {
        public NativeAudioStream(InputStream in) throws IOException {
            super(in);
        }
        public int getLength() {
            return 0;
        }
    }
