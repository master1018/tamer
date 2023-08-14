    class AudioStreamSequence extends SequenceInputStream {
        Enumeration e;
        InputStream in;
        public AudioStreamSequence(Enumeration e) {
            super(e);
        }
    }
