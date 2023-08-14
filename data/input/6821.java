public class DLSRegion {
    public final static int OPTION_SELFNONEXCLUSIVE = 0x0001;
    protected List<DLSModulator> modulators = new ArrayList<DLSModulator>();
    protected int keyfrom;
    protected int keyto;
    protected int velfrom;
    protected int velto;
    protected int options;
    protected int exclusiveClass;
    protected int fusoptions;
    protected int phasegroup;
    protected long channel;
    protected DLSSample sample = null;
    protected DLSSampleOptions sampleoptions;
    public List<DLSModulator> getModulators() {
        return modulators;
    }
    public long getChannel() {
        return channel;
    }
    public void setChannel(long channel) {
        this.channel = channel;
    }
    public int getExclusiveClass() {
        return exclusiveClass;
    }
    public void setExclusiveClass(int exclusiveClass) {
        this.exclusiveClass = exclusiveClass;
    }
    public int getFusoptions() {
        return fusoptions;
    }
    public void setFusoptions(int fusoptions) {
        this.fusoptions = fusoptions;
    }
    public int getKeyfrom() {
        return keyfrom;
    }
    public void setKeyfrom(int keyfrom) {
        this.keyfrom = keyfrom;
    }
    public int getKeyto() {
        return keyto;
    }
    public void setKeyto(int keyto) {
        this.keyto = keyto;
    }
    public int getOptions() {
        return options;
    }
    public void setOptions(int options) {
        this.options = options;
    }
    public int getPhasegroup() {
        return phasegroup;
    }
    public void setPhasegroup(int phasegroup) {
        this.phasegroup = phasegroup;
    }
    public DLSSample getSample() {
        return sample;
    }
    public void setSample(DLSSample sample) {
        this.sample = sample;
    }
    public int getVelfrom() {
        return velfrom;
    }
    public void setVelfrom(int velfrom) {
        this.velfrom = velfrom;
    }
    public int getVelto() {
        return velto;
    }
    public void setVelto(int velto) {
        this.velto = velto;
    }
    public void setModulators(List<DLSModulator> modulators) {
        this.modulators = modulators;
    }
    public DLSSampleOptions getSampleoptions() {
        return sampleoptions;
    }
    public void setSampleoptions(DLSSampleOptions sampleOptions) {
        this.sampleoptions = sampleOptions;
    }
}
