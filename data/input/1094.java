public class Send_NoteOn_Multiple {
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
        ShortMessage smsg = new ShortMessage();
        smsg.setMessage(ShortMessage.NOTE_ON,0, 60, 64);
        receiver.send(smsg, -1);
        smsg.setMessage(ShortMessage.NOTE_ON,0, 61, 64);
        receiver.send(smsg, -1);
        smsg.setMessage(ShortMessage.NOTE_ON,0, 62, 64);
        receiver.send(smsg, -1);
        soft.read(1);
        assertTrue(soft.findVoice(0,60) != null);
        assertTrue(soft.findVoice(0,61) != null);
        assertTrue(soft.findVoice(0,62) != null);
        smsg.setMessage(ShortMessage.NOTE_ON,0, 60, 0);
        receiver.send(smsg, -1);
        smsg.setMessage(ShortMessage.NOTE_ON,0, 61, 0);
        receiver.send(smsg, -1);
        soft.read(1);
        assertTrue(soft.findVoice(0,60) == null);
        assertTrue(soft.findVoice(0,61) == null);
        assertTrue(soft.findVoice(0,62) != null);
        soft.close();
    }
}
