public class ImplicitOpenClose {
    public static void main(String[] args) throws Exception {
        Field f = SoftSynthesizer.class.getDeclaredField("testline");
        f.setAccessible(true);
        f.set(null, new DummySourceDataLine());
        Synthesizer synth = new SoftSynthesizer();
        ReferenceCountingDevice rcd = (ReferenceCountingDevice)synth;
        Receiver recv = rcd.getReceiverReferenceCounting();
        if(!synth.isOpen())
            throw new Exception("Synthesizer not open!");
        recv.close();
        if(synth.isOpen())
            throw new Exception("Synthesizer not closed!");
        Receiver recv1 = rcd.getReceiverReferenceCounting();
        if(!synth.isOpen())
            throw new Exception("Synthesizer not open!");
        Receiver recv2 = rcd.getReceiverReferenceCounting();
        if(!synth.isOpen())
            throw new Exception("Synthesizer not open!");
        recv2.close();
        if(!synth.isOpen())
            throw new Exception("Synthesizer was closed!");
        recv1.close();
        if(synth.isOpen())
            throw new Exception("Synthesizer not closed!");
        synth.open();
        Receiver recv3 = rcd.getReceiverReferenceCounting();
        if(!synth.isOpen())
            throw new Exception("Synthesizer not open!");
        recv3.close();
        if(!synth.isOpen())
            throw new Exception("Synthesizer was closed!");
        synth.close();
        if(synth.isOpen())
            throw new Exception("Synthesizer not closed!");
        recv3 = rcd.getReceiverReferenceCounting();
        synth.open();
        if(!synth.isOpen())
            throw new Exception("Synthesizer not open!");
        recv3.close();
        if(!synth.isOpen())
            throw new Exception("Synthesizer was closed!");
        synth.close();
        if(synth.isOpen())
            throw new Exception("Synthesizer not closed!");
    }
}
