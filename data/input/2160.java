public class Send_NoteOn_AllChannels {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        SoftTestUtils soft = new SoftTestUtils();
        MidiChannel channel = soft.synth.getChannels()[0];
        Receiver receiver = soft.synth.getReceiver();
        for (int i = 0; i < 15; i++) {
            if(i == 9) i++;
            ShortMessage smsg = new ShortMessage();
            smsg.setMessage(ShortMessage.NOTE_ON,i, 60, 64);
            receiver.send(smsg, -1);
            soft.read(1);
            VoiceStatus voice = soft.findVoice(i,60);
            assertTrue(voice != null);
            smsg.setMessage(ShortMessage.NOTE_ON,i, 60, 0);
            receiver.send(smsg, -1);
            soft.read(1);
            voice = soft.findVoice(i,60);
            assertTrue(voice == null);
            soft.read(1);
        }
        soft.close();
    }
}
