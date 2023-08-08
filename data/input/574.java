public class TitleAnnotation extends Annotation {
    public static final int typeIndexID = JCasRegistry.register(TitleAnnotation.class);
    public static final int type = typeIndexID;
    public int getTypeIndexID() {
        return typeIndexID;
    }
    protected TitleAnnotation() {
    }
    public TitleAnnotation(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }
    public TitleAnnotation(JCas jcas) {
        super(jcas);
        readObject();
    }
    public TitleAnnotation(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }
    private void readObject() {
    }
    public String getValue() {
        if (TitleAnnotation_Type.featOkTst && ((TitleAnnotation_Type) jcasType).casFeat_value == null) jcasType.jcas.throwFeatMissing("value", "it.webscience.uima.annotations.eventData.TitleAnnotation");
        return jcasType.ll_cas.ll_getStringValue(addr, ((TitleAnnotation_Type) jcasType).casFeatCode_value);
    }
    public void setValue(String v) {
        if (TitleAnnotation_Type.featOkTst && ((TitleAnnotation_Type) jcasType).casFeat_value == null) jcasType.jcas.throwFeatMissing("value", "it.webscience.uima.annotations.eventData.TitleAnnotation");
        jcasType.ll_cas.ll_setStringValue(addr, ((TitleAnnotation_Type) jcasType).casFeatCode_value, v);
    }
}
