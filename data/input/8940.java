public class AddModelPerformerIntIntIntInt {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        SimpleInstrument instrument = new SimpleInstrument();
        ModelPerformer[] performers = new ModelPerformer[2];
        performers[0] = new ModelPerformer();
        performers[0].setExclusiveClass(1);
        performers[0].setKeyFrom(36);
        performers[0].setKeyTo(48);
        performers[0].setVelFrom(16);
        performers[0].setVelTo(80);
        performers[0].setSelfNonExclusive(true);
        performers[0].setDefaultConnectionsEnabled(false);
        performers[0].getConnectionBlocks().add(new ModelConnectionBlock());
        performers[0].getOscillators().add(new ModelByteBufferWavetable(new ModelByteBuffer(new byte[] {1,2,3})));
        performers[1] = new ModelPerformer();
        performers[1].setExclusiveClass(0);
        performers[1].setKeyFrom(12);
        performers[1].setKeyTo(24);
        performers[1].setVelFrom(20);
        performers[1].setVelTo(90);
        performers[1].setSelfNonExclusive(false);
        performers[0].setDefaultConnectionsEnabled(true);
        performers[1].getConnectionBlocks().add(new ModelConnectionBlock());
        performers[1].getOscillators().add(new ModelByteBufferWavetable(new ModelByteBuffer(new byte[] {1,2,3})));
        instrument.add(performers[0],18,40,20,75);
        ModelPerformer[] performers2 = instrument.getPerformers();
        for (int i = 0; i < performers2.length; i++) {
            assertEquals(performers[i].getConnectionBlocks(), performers2[i].getConnectionBlocks());
            assertEquals(performers[i].getExclusiveClass(), performers2[i].getExclusiveClass());
            if(performers[i].getKeyFrom() < 18)
                assertEquals(18, performers2[i].getKeyFrom());
            else
                assertEquals(performers[i].getKeyFrom(), performers2[i].getKeyFrom());
            if(performers[i].getKeyTo() > 40)
                assertEquals(40, performers2[i].getKeyTo());
            else
                assertEquals(performers[i].getKeyTo(), performers2[i].getKeyTo());
            if(performers[i].getVelFrom() < 20)
                assertEquals(20, performers2[i].getVelFrom());
            else
                assertEquals(performers[i].getVelFrom(), performers2[i].getVelFrom());
            if(performers[i].getVelTo() > 75)
                assertEquals(75, performers2[i].getVelTo());
            else
                assertEquals(performers[i].getVelTo(), performers2[i].getVelTo());
            assertEquals(performers[i].getOscillators(), performers2[i].getOscillators());
            assertEquals(performers[i].isSelfNonExclusive(), performers2[i].isSelfNonExclusive());
            assertEquals(performers[i].isDefaultConnectionsEnabled(), performers2[i].isDefaultConnectionsEnabled());
        }
    }
}
