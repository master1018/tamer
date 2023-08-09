public class DLSInstrument extends ModelInstrument {
    protected int preset = 0;
    protected int bank = 0;
    protected boolean druminstrument = false;
    protected byte[] guid = null;
    protected DLSInfo info = new DLSInfo();
    protected List<DLSRegion> regions = new ArrayList<DLSRegion>();
    protected List<DLSModulator> modulators = new ArrayList<DLSModulator>();
    public DLSInstrument() {
        super(null, null, null, null);
    }
    public DLSInstrument(DLSSoundbank soundbank) {
        super(soundbank, null, null, null);
    }
    public DLSInfo getInfo() {
        return info;
    }
    public String getName() {
        return info.name;
    }
    public void setName(String name) {
        info.name = name;
    }
    public ModelPatch getPatch() {
        return new ModelPatch(bank, preset, druminstrument);
    }
    public void setPatch(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch)patch).isPercussion()) {
            druminstrument = true;
            bank = patch.getBank();
            preset = patch.getProgram();
        } else {
            druminstrument = false;
            bank = patch.getBank();
            preset = patch.getProgram();
        }
    }
    public Object getData() {
        return null;
    }
    public List<DLSRegion> getRegions() {
        return regions;
    }
    public List<DLSModulator> getModulators() {
        return modulators;
    }
    public String toString() {
        if (druminstrument)
            return "Drumkit: " + info.name
                    + " bank #" + bank + " preset #" + preset;
        else
            return "Instrument: " + info.name
                    + " bank #" + bank + " preset #" + preset;
    }
    private ModelIdentifier convertToModelDest(int dest) {
        if (dest == DLSModulator.CONN_DST_NONE)
            return null;
        if (dest == DLSModulator.CONN_DST_GAIN)
            return ModelDestination.DESTINATION_GAIN;
        if (dest == DLSModulator.CONN_DST_PITCH)
            return ModelDestination.DESTINATION_PITCH;
        if (dest == DLSModulator.CONN_DST_PAN)
            return ModelDestination.DESTINATION_PAN;
        if (dest == DLSModulator.CONN_DST_LFO_FREQUENCY)
            return ModelDestination.DESTINATION_LFO1_FREQ;
        if (dest == DLSModulator.CONN_DST_LFO_STARTDELAY)
            return ModelDestination.DESTINATION_LFO1_DELAY;
        if (dest == DLSModulator.CONN_DST_EG1_ATTACKTIME)
            return ModelDestination.DESTINATION_EG1_ATTACK;
        if (dest == DLSModulator.CONN_DST_EG1_DECAYTIME)
            return ModelDestination.DESTINATION_EG1_DECAY;
        if (dest == DLSModulator.CONN_DST_EG1_RELEASETIME)
            return ModelDestination.DESTINATION_EG1_RELEASE;
        if (dest == DLSModulator.CONN_DST_EG1_SUSTAINLEVEL)
            return ModelDestination.DESTINATION_EG1_SUSTAIN;
        if (dest == DLSModulator.CONN_DST_EG2_ATTACKTIME)
            return ModelDestination.DESTINATION_EG2_ATTACK;
        if (dest == DLSModulator.CONN_DST_EG2_DECAYTIME)
            return ModelDestination.DESTINATION_EG2_DECAY;
        if (dest == DLSModulator.CONN_DST_EG2_RELEASETIME)
            return ModelDestination.DESTINATION_EG2_RELEASE;
        if (dest == DLSModulator.CONN_DST_EG2_SUSTAINLEVEL)
            return ModelDestination.DESTINATION_EG2_SUSTAIN;
        if (dest == DLSModulator.CONN_DST_KEYNUMBER)
            return ModelDestination.DESTINATION_KEYNUMBER;
        if (dest == DLSModulator.CONN_DST_CHORUS)
            return ModelDestination.DESTINATION_CHORUS;
        if (dest == DLSModulator.CONN_DST_REVERB)
            return ModelDestination.DESTINATION_REVERB;
        if (dest == DLSModulator.CONN_DST_VIB_FREQUENCY)
            return ModelDestination.DESTINATION_LFO2_FREQ;
        if (dest == DLSModulator.CONN_DST_VIB_STARTDELAY)
            return ModelDestination.DESTINATION_LFO2_DELAY;
        if (dest == DLSModulator.CONN_DST_EG1_DELAYTIME)
            return ModelDestination.DESTINATION_EG1_DELAY;
        if (dest == DLSModulator.CONN_DST_EG1_HOLDTIME)
            return ModelDestination.DESTINATION_EG1_HOLD;
        if (dest == DLSModulator.CONN_DST_EG1_SHUTDOWNTIME)
            return ModelDestination.DESTINATION_EG1_SHUTDOWN;
        if (dest == DLSModulator.CONN_DST_EG2_DELAYTIME)
            return ModelDestination.DESTINATION_EG2_DELAY;
        if (dest == DLSModulator.CONN_DST_EG2_HOLDTIME)
            return ModelDestination.DESTINATION_EG2_HOLD;
        if (dest == DLSModulator.CONN_DST_FILTER_CUTOFF)
            return ModelDestination.DESTINATION_FILTER_FREQ;
        if (dest == DLSModulator.CONN_DST_FILTER_Q)
            return ModelDestination.DESTINATION_FILTER_Q;
        return null;
    }
    private ModelIdentifier convertToModelSrc(int src) {
        if (src == DLSModulator.CONN_SRC_NONE)
            return null;
        if (src == DLSModulator.CONN_SRC_LFO)
            return ModelSource.SOURCE_LFO1;
        if (src == DLSModulator.CONN_SRC_KEYONVELOCITY)
            return ModelSource.SOURCE_NOTEON_VELOCITY;
        if (src == DLSModulator.CONN_SRC_KEYNUMBER)
            return ModelSource.SOURCE_NOTEON_KEYNUMBER;
        if (src == DLSModulator.CONN_SRC_EG1)
            return ModelSource.SOURCE_EG1;
        if (src == DLSModulator.CONN_SRC_EG2)
            return ModelSource.SOURCE_EG2;
        if (src == DLSModulator.CONN_SRC_PITCHWHEEL)
            return ModelSource.SOURCE_MIDI_PITCH;
        if (src == DLSModulator.CONN_SRC_CC1)
            return new ModelIdentifier("midi_cc", "1", 0);
        if (src == DLSModulator.CONN_SRC_CC7)
            return new ModelIdentifier("midi_cc", "7", 0);
        if (src == DLSModulator.CONN_SRC_CC10)
            return new ModelIdentifier("midi_cc", "10", 0);
        if (src == DLSModulator.CONN_SRC_CC11)
            return new ModelIdentifier("midi_cc", "11", 0);
        if (src == DLSModulator.CONN_SRC_RPN0)
            return new ModelIdentifier("midi_rpn", "0", 0);
        if (src == DLSModulator.CONN_SRC_RPN1)
            return new ModelIdentifier("midi_rpn", "1", 0);
        if (src == DLSModulator.CONN_SRC_POLYPRESSURE)
            return ModelSource.SOURCE_MIDI_POLY_PRESSURE;
        if (src == DLSModulator.CONN_SRC_CHANNELPRESSURE)
            return ModelSource.SOURCE_MIDI_CHANNEL_PRESSURE;
        if (src == DLSModulator.CONN_SRC_VIBRATO)
            return ModelSource.SOURCE_LFO2;
        if (src == DLSModulator.CONN_SRC_MONOPRESSURE)
            return ModelSource.SOURCE_MIDI_CHANNEL_PRESSURE;
        if (src == DLSModulator.CONN_SRC_CC91)
            return new ModelIdentifier("midi_cc", "91", 0);
        if (src == DLSModulator.CONN_SRC_CC93)
            return new ModelIdentifier("midi_cc", "93", 0);
        return null;
    }
    private ModelConnectionBlock convertToModel(DLSModulator mod) {
        ModelIdentifier source = convertToModelSrc(mod.getSource());
        ModelIdentifier control = convertToModelSrc(mod.getControl());
        ModelIdentifier destination_id =
                convertToModelDest(mod.getDestination());
        int scale = mod.getScale();
        double f_scale;
        if (scale == Integer.MIN_VALUE)
            f_scale = Double.NEGATIVE_INFINITY;
        else
            f_scale = scale / 65536.0;
        if (destination_id != null) {
            ModelSource src = null;
            ModelSource ctrl = null;
            ModelConnectionBlock block = new ModelConnectionBlock();
            if (control != null) {
                ModelSource s = new ModelSource();
                if (control == ModelSource.SOURCE_MIDI_PITCH) {
                    ((ModelStandardTransform)s.getTransform()).setPolarity(
                            ModelStandardTransform.POLARITY_BIPOLAR);
                } else if (control == ModelSource.SOURCE_LFO1
                        || control == ModelSource.SOURCE_LFO2) {
                    ((ModelStandardTransform)s.getTransform()).setPolarity(
                            ModelStandardTransform.POLARITY_BIPOLAR);
                }
                s.setIdentifier(control);
                block.addSource(s);
                ctrl = s;
            }
            if (source != null) {
                ModelSource s = new ModelSource();
                if (source == ModelSource.SOURCE_MIDI_PITCH) {
                    ((ModelStandardTransform)s.getTransform()).setPolarity(
                            ModelStandardTransform.POLARITY_BIPOLAR);
                } else if (source == ModelSource.SOURCE_LFO1
                        || source == ModelSource.SOURCE_LFO2) {
                    ((ModelStandardTransform)s.getTransform()).setPolarity(
                            ModelStandardTransform.POLARITY_BIPOLAR);
                }
                s.setIdentifier(source);
                block.addSource(s);
                src = s;
            }
            ModelDestination destination = new ModelDestination();
            destination.setIdentifier(destination_id);
            block.setDestination(destination);
            if (mod.getVersion() == 1) {
                if (mod.getTransform() == DLSModulator.CONN_TRN_CONCAVE) {
                    if (src != null) {
                        ((ModelStandardTransform)src.getTransform())
                                .setTransform(
                                    ModelStandardTransform.TRANSFORM_CONCAVE);
                        ((ModelStandardTransform)src.getTransform())
                                .setDirection(
                                    ModelStandardTransform.DIRECTION_MAX2MIN);
                    }
                    if (ctrl != null) {
                        ((ModelStandardTransform)ctrl.getTransform())
                                .setTransform(
                                    ModelStandardTransform.TRANSFORM_CONCAVE);
                        ((ModelStandardTransform)ctrl.getTransform())
                                .setDirection(
                                    ModelStandardTransform.DIRECTION_MAX2MIN);
                    }
                }
            } else if (mod.getVersion() == 2) {
                int transform = mod.getTransform();
                int src_transform_invert = (transform >> 15) & 1;
                int src_transform_bipolar = (transform >> 14) & 1;
                int src_transform = (transform >> 10) & 8;
                int ctr_transform_invert = (transform >> 9) & 1;
                int ctr_transform_bipolar = (transform >> 8) & 1;
                int ctr_transform = (transform >> 4) & 8;
                if (src != null) {
                    int trans = ModelStandardTransform.TRANSFORM_LINEAR;
                    if (src_transform == DLSModulator.CONN_TRN_SWITCH)
                        trans = ModelStandardTransform.TRANSFORM_SWITCH;
                    if (src_transform == DLSModulator.CONN_TRN_CONCAVE)
                        trans = ModelStandardTransform.TRANSFORM_CONCAVE;
                    if (src_transform == DLSModulator.CONN_TRN_CONVEX)
                        trans = ModelStandardTransform.TRANSFORM_CONVEX;
                    ((ModelStandardTransform)src.getTransform())
                            .setTransform(trans);
                    ((ModelStandardTransform)src.getTransform())
                            .setPolarity(src_transform_bipolar == 1);
                    ((ModelStandardTransform)src.getTransform())
                            .setDirection(src_transform_invert == 1);
                }
                if (ctrl != null) {
                    int trans = ModelStandardTransform.TRANSFORM_LINEAR;
                    if (ctr_transform == DLSModulator.CONN_TRN_SWITCH)
                        trans = ModelStandardTransform.TRANSFORM_SWITCH;
                    if (ctr_transform == DLSModulator.CONN_TRN_CONCAVE)
                        trans = ModelStandardTransform.TRANSFORM_CONCAVE;
                    if (ctr_transform == DLSModulator.CONN_TRN_CONVEX)
                        trans = ModelStandardTransform.TRANSFORM_CONVEX;
                    ((ModelStandardTransform)ctrl.getTransform())
                            .setTransform(trans);
                    ((ModelStandardTransform)ctrl.getTransform())
                            .setPolarity(ctr_transform_bipolar == 1);
                    ((ModelStandardTransform)ctrl.getTransform())
                            .setDirection(ctr_transform_invert == 1);
                }
            }
            block.setScale(f_scale);
            return block;
        }
        return null;
    }
    public ModelPerformer[] getPerformers() {
        List<ModelPerformer> performers = new ArrayList<ModelPerformer>();
        Map<String, DLSModulator> modmap = new HashMap<String, DLSModulator>();
        for (DLSModulator mod: getModulators()) {
            modmap.put(mod.getSource() + "x" + mod.getControl() + "=" +
                    mod.getDestination(), mod);
        }
        Map<String, DLSModulator> insmodmap =
                new HashMap<String, DLSModulator>();
        for (DLSRegion zone: regions) {
            ModelPerformer performer = new ModelPerformer();
            performer.setName(zone.getSample().getName());
            performer.setSelfNonExclusive((zone.getFusoptions() &
                    DLSRegion.OPTION_SELFNONEXCLUSIVE) != 0);
            performer.setExclusiveClass(zone.getExclusiveClass());
            performer.setKeyFrom(zone.getKeyfrom());
            performer.setKeyTo(zone.getKeyto());
            performer.setVelFrom(zone.getVelfrom());
            performer.setVelTo(zone.getVelto());
            insmodmap.clear();
            insmodmap.putAll(modmap);
            for (DLSModulator mod: zone.getModulators()) {
                insmodmap.put(mod.getSource() + "x" + mod.getControl() + "=" +
                        mod.getDestination(), mod);
            }
            List<ModelConnectionBlock> blocks = performer.getConnectionBlocks();
            for (DLSModulator mod: insmodmap.values()) {
                ModelConnectionBlock p = convertToModel(mod);
                if (p != null)
                    blocks.add(p);
            }
            DLSSample sample = zone.getSample();
            DLSSampleOptions sampleopt = zone.getSampleoptions();
            if (sampleopt == null)
                sampleopt = sample.getSampleoptions();
            ModelByteBuffer buff = sample.getDataBuffer();
            float pitchcorrection = (-sampleopt.unitynote * 100) +
                    sampleopt.finetune;
            ModelByteBufferWavetable osc = new ModelByteBufferWavetable(buff,
                    sample.getFormat(), pitchcorrection);
            osc.setAttenuation(osc.getAttenuation() / 65536f);
            if (sampleopt.getLoops().size() != 0) {
                DLSSampleLoop loop = sampleopt.getLoops().get(0);
                osc.setLoopStart((int)loop.getStart());
                osc.setLoopLength((int)loop.getLength());
                if (loop.getType() == DLSSampleLoop.LOOP_TYPE_FORWARD)
                    osc.setLoopType(ModelWavetable.LOOP_TYPE_FORWARD);
                if (loop.getType() == DLSSampleLoop.LOOP_TYPE_RELEASE)
                    osc.setLoopType(ModelWavetable.LOOP_TYPE_RELEASE);
                else
                    osc.setLoopType(ModelWavetable.LOOP_TYPE_FORWARD);
            }
            performer.getConnectionBlocks().add(
                    new ModelConnectionBlock(SoftFilter.FILTERTYPE_LP12,
                        new ModelDestination(
                            new ModelIdentifier("filter", "type", 1))));
            performer.getOscillators().add(osc);
            performers.add(performer);
        }
        return performers.toArray(new ModelPerformer[performers.size()]);
    }
    public byte[] getGuid() {
        return guid == null ? null : Arrays.copyOf(guid, guid.length);
    }
    public void setGuid(byte[] guid) {
        this.guid = guid == null ? null : Arrays.copyOf(guid, guid.length);
    }
}
