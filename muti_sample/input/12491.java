public class ModelMappedInstrument extends ModelInstrument {
    private ModelInstrument ins;
    public ModelMappedInstrument(ModelInstrument ins, Patch patch) {
        super(ins.getSoundbank(), patch, ins.getName(), ins.getDataClass());
        this.ins = ins;
    }
    public Object getData() {
        return ins.getData();
    }
    public ModelPerformer[] getPerformers() {
        return ins.getPerformers();
    }
    public ModelDirector getDirector(ModelPerformer[] performers,
            MidiChannel channel, ModelDirectedPlayer player) {
        return ins.getDirector(performers, channel, player);
    }
    public ModelChannelMixer getChannelMixer(MidiChannel channel,
            AudioFormat format) {
        return ins.getChannelMixer(channel, format);
    }
}
