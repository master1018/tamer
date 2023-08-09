public class SoftInstrument extends Instrument {
    private SoftPerformer[] performers;
    private ModelPerformer[] modelperformers;
    private Object data;
    private ModelInstrument ins;
    public SoftInstrument(ModelInstrument ins) {
        super(ins.getSoundbank(), ins.getPatch(), ins.getName(),
                ins.getDataClass());
        data = ins.getData();
        this.ins = ins;
        initPerformers(((ModelInstrument)ins).getPerformers());
    }
    public SoftInstrument(ModelInstrument ins,
            ModelPerformer[] overrideperformers) {
        super(ins.getSoundbank(), ins.getPatch(), ins.getName(),
                ins.getDataClass());
        data = ins.getData();
        this.ins = ins;
        initPerformers(overrideperformers);
    }
    private void initPerformers(ModelPerformer[] modelperformers) {
        this.modelperformers = modelperformers;
        performers = new SoftPerformer[modelperformers.length];
        for (int i = 0; i < modelperformers.length; i++)
            performers[i] = new SoftPerformer(modelperformers[i]);
    }
    public ModelDirector getDirector(MidiChannel channel,
            ModelDirectedPlayer player) {
        return ins.getDirector(modelperformers, channel, player);
    }
    public ModelInstrument getSourceInstrument() {
        return ins;
    }
    public Object getData() {
        return data;
    }
    public SoftPerformer getPerformer(int index) {
        return performers[index];
    }
}
