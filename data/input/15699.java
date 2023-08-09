public class NoteOverFlowTest {
    public static void main(String[] args) throws Exception
    {
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        AudioInputStream stream = synth.openStream(format, null);
        MidiChannel ch1 = synth.getChannels()[0];
        ch1.programChange(48); 
        for (int i = 0; i < synth.getMaxPolyphony(); i++) {
            ch1.noteOn(64, 64);
            ch1.noteOff(64);
        }
        ch1.noteOn(64, 64);
        ch1.noteOff(64);
        stream.skip(format.getFrameSize() * ((int)(format.getFrameRate() * 20)));
        VoiceStatus[] v = synth.getVoiceStatus();
        for (int i = 0; i < v.length; i++) {
            if(v[i].active)
            {
                throw new RuntimeException("Not all voices are inactive!");
            }
        }
        synth.close();
    }
}
