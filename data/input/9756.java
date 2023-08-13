public class DLSSampleOptions {
    protected int unitynote;
    protected short finetune;
    protected int attenuation;
    protected long options;
    protected List<DLSSampleLoop> loops = new ArrayList<DLSSampleLoop>();
    public int getAttenuation() {
        return attenuation;
    }
    public void setAttenuation(int attenuation) {
        this.attenuation = attenuation;
    }
    public short getFinetune() {
        return finetune;
    }
    public void setFinetune(short finetune) {
        this.finetune = finetune;
    }
    public List<DLSSampleLoop> getLoops() {
        return loops;
    }
    public long getOptions() {
        return options;
    }
    public void setOptions(long options) {
        this.options = options;
    }
    public int getUnitynote() {
        return unitynote;
    }
    public void setUnitynote(int unitynote) {
        this.unitynote = unitynote;
    }
}
