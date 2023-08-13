public class GetMidiDevice {
    public static void main(String[] args) throws Exception {
        AudioSynthesizer synth = new SoftSynthesizer();
        synth.openStream(null, null);
        Receiver recv = synth.getReceiver();
        if (((SoftReceiver) recv).getMidiDevice() != synth) {
            throw new Exception("SoftReceiver.getMidiDevice() doesn't return "
                    + "instance of the synthesizer");
        }
        synth.close();
    }
}
