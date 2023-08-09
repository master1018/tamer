public abstract class ModelInstrument extends Instrument {
    protected ModelInstrument(Soundbank soundbank, Patch patch, String name,
            Class<?> dataClass) {
        super(soundbank, patch, name, dataClass);
    }
    public ModelDirector getDirector(ModelPerformer[] performers,
            MidiChannel channel, ModelDirectedPlayer player) {
        return new ModelStandardIndexedDirector(performers, player);
    }
    public ModelPerformer[] getPerformers() {
        return new ModelPerformer[0];
    }
    public ModelChannelMixer getChannelMixer(MidiChannel channel,
            AudioFormat format) {
        return null;
    }
    public Patch getPatchAlias() {
        Patch patch = getPatch();
        int program = patch.getProgram();
        int bank = patch.getBank();
        if (bank != 0)
            return patch;
        boolean percussion = false;
        if (getPatch() instanceof ModelPatch)
            percussion = ((ModelPatch)getPatch()).isPercussion();
        if (percussion)
            return new Patch(0x78 << 7, program);
        else
            return new Patch(0x79 << 7, program);
    }
    public String[] getKeys() {
        String[] keys = new String[128];
        for (ModelPerformer performer : getPerformers()) {
            for (int k = performer.getKeyFrom(); k <= performer.getKeyTo(); k++) {
                if (k >= 0 && k < 128 && keys[k] == null) {
                    String name = performer.getName();
                    if (name == null)
                        name = "untitled";
                    keys[k] = name;
                }
            }
        }
        return keys;
    }
    public boolean[] getChannels() {
        boolean percussion = false;
        if (getPatch() instanceof ModelPatch)
            percussion = ((ModelPatch)getPatch()).isPercussion();
        if (percussion) {
            boolean[] ch = new boolean[16];
            for (int i = 0; i < ch.length; i++)
                ch[i] = false;
            ch[9] = true;
            return ch;
        }
        int bank = getPatch().getBank();
        if (bank >> 7 == 0x78 || bank >> 7 == 0x79) {
            boolean[] ch = new boolean[16];
            for (int i = 0; i < ch.length; i++)
                ch[i] = true;
            return ch;
        }
        boolean[] ch = new boolean[16];
        for (int i = 0; i < ch.length; i++)
            ch[i] = true;
        ch[9] = false;
        return ch;
    }
}
