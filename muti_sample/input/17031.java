public class SpnegoReqFlags {
    public static void main(String[] args)
            throws Exception {
        new OneKDC(null).writeJAASConf();
        new SpnegoReqFlags().go();
    }
    void go() throws Exception {
        Context c = Context.fromJAAS("client");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_SPNEGO_MECH_OID);
        byte[] token = c.doAs(new Action() {
            @Override
            public byte[] run(Context me, byte[] input) throws Exception {
                me.x().requestCredDeleg(true);
                me.x().requestReplayDet(false);
                me.x().requestSequenceDet(false);
                return me.x().initSecContext(new byte[0], 0, 0);
            }
        }, null);
        DerValue d = new DerValue(token);   
        DerInputStream ins = d.data;        
        d.data.getDerValue();               
        d = d.data.getDerValue();           
        d = d.data.getDerValue();           
        boolean found = false;
        while (d.data.available() > 0) {
            DerValue d2 = d.data.getDerValue();
            if (d2.isContextSpecific((byte)1)) {
                found = true;
                System.out.println("regFlags field located.");
                BitArray ba = d2.data.getUnalignedBitString();
                if (ba.length() != 7) {
                    throw new Exception("reqFlags should contain 7 bits");
                }
                if (!ba.get(0)) {
                    throw new Exception("delegFlag should be true");
                }
                if (ba.get(2) || ba.get(3)) {
                    throw new Exception("replay/sequenceFlag should be false");
                }
            }
        }
        if (!found) {
            System.out.println("Warning: regFlags field not found, too new?");
        }
        c.dispose();
    }
}
