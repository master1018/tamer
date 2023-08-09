public class AudioDataStream extends ByteArrayInputStream {
    AudioData ad;
    public AudioDataStream(AudioData data) {
        super(data.buffer);
        this.ad = data;
    }
    AudioData getAudioData() {
        return ad;
    }
}
