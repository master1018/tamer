public class ModelSource {
    public static final ModelIdentifier SOURCE_NONE = null;
    public static final ModelIdentifier SOURCE_NOTEON_KEYNUMBER =
            new ModelIdentifier("noteon", "keynumber");     
    public static final ModelIdentifier SOURCE_NOTEON_VELOCITY =
            new ModelIdentifier("noteon", "velocity");      
    public static final ModelIdentifier SOURCE_EG1 =
            new ModelIdentifier("eg", null, 0);
    public static final ModelIdentifier SOURCE_EG2 =
            new ModelIdentifier("eg", null, 1);
    public static final ModelIdentifier SOURCE_LFO1 =
            new ModelIdentifier("lfo", null, 0);
    public static final ModelIdentifier SOURCE_LFO2 =
            new ModelIdentifier("lfo", null, 1);
    public static final ModelIdentifier SOURCE_MIDI_PITCH =
            new ModelIdentifier("midi", "pitch", 0);            
    public static final ModelIdentifier SOURCE_MIDI_CHANNEL_PRESSURE =
            new ModelIdentifier("midi", "channel_pressure", 0); 
    public static final ModelIdentifier SOURCE_MIDI_POLY_PRESSURE =
            new ModelIdentifier("midi", "poly_pressure", 0);    
    public static final ModelIdentifier SOURCE_MIDI_CC_0 =
            new ModelIdentifier("midi_cc", "0", 0);             
    public static final ModelIdentifier SOURCE_MIDI_RPN_0 =
            new ModelIdentifier("midi_rpn", "0", 0);            
    private ModelIdentifier source = SOURCE_NONE;
    private ModelTransform transform;
    public ModelSource() {
        this.transform = new ModelStandardTransform();
    }
    public ModelSource(ModelIdentifier id) {
        source = id;
        this.transform = new ModelStandardTransform();
    }
    public ModelSource(ModelIdentifier id, boolean direction) {
        source = id;
        this.transform = new ModelStandardTransform(direction);
    }
    public ModelSource(ModelIdentifier id, boolean direction, boolean polarity) {
        source = id;
        this.transform = new ModelStandardTransform(direction, polarity);
    }
    public ModelSource(ModelIdentifier id, boolean direction, boolean polarity,
            int transform) {
        source = id;
        this.transform =
                new ModelStandardTransform(direction, polarity, transform);
    }
    public ModelSource(ModelIdentifier id, ModelTransform transform) {
        source = id;
        this.transform = transform;
    }
    public ModelIdentifier getIdentifier() {
        return source;
    }
    public void setIdentifier(ModelIdentifier source) {
        this.source = source;
    }
    public ModelTransform getTransform() {
        return transform;
    }
    public void setTransform(ModelTransform transform) {
        this.transform = transform;
    }
}
