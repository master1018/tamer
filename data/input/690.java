public class SocketCepstrumSource implements CepstrumSource {
    private DataInputStream dataReader;
    private static final float UTTERANCE_START = Float.MAX_VALUE;
    private static final float UTTERANCE_END = Float.MIN_VALUE;
    private int cepstrumLength = 13;
    private boolean inUtterance;
    public SocketCepstrumSource(Socket socket) throws IOException {
        inUtterance = false;
        this.dataReader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }
    public Cepstrum getCepstrum() throws IOException {
        float firstValue = dataReader.readFloat();
        if (!inUtterance) {
            if (firstValue == UTTERANCE_START) {
                inUtterance = true;
                return (new Cepstrum(Signal.UTTERANCE_START));
            } else {
                throw new IllegalStateException("No UTTERANCE_START read from socket: " + firstValue + ", while UTTERANCE_START is " + UTTERANCE_START);
            }
        } else {
            if (firstValue == UTTERANCE_END) {
                inUtterance = false;
                return (new Cepstrum(Signal.UTTERANCE_END));
            } else if (firstValue == UTTERANCE_START) {
                throw new IllegalStateException("Too many UTTERANCE_STARTs.");
            } else {
                float[] data = new float[cepstrumLength];
                data[0] = firstValue;
                for (int i = 1; i < cepstrumLength; i++) {
                    data[i] = dataReader.readFloat();
                }
                return (new Cepstrum(data));
            }
        }
    }
}
