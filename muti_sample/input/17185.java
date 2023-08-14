    class AudioTranslatorStream extends NativeAudioStream {
        private int length = 0;
        public AudioTranslatorStream(InputStream in) throws IOException {
            super(in);
            throw new InvalidAudioFormatException();
        }
        public int getLength() {
            return length;
        }
    }
