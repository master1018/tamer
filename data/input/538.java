public class EntityDCTranslationMap extends TranslationMap {
    public EntityDCTranslationMap() {
        super();
        map.add(new TranslationMapElement(EntityConst.KEY_NAME, DCElementSetNames.DCCREATOR, "", ""));
        map.add(new TranslationMapElement(EntityConst.SUBJ_HEAD, DCElementSetNames.DCSUBJECT, "", ""));
        map.add(new TranslationMapElement(EntityConst.HISTORY, DCElementSetNames.DCDESCRIPTION, "", ""));
        map.add(new TranslationMapElement(EntityConst.FUNCTIONS, DCElementSetNames.DCDESCRIPTION, "", ""));
        map.add(new TranslationMapElement(EntityConst.BEGIN_DATE_EXT, DCElementSetNames.DCCOVERAGE, DCElementSetNames.QUALTEMPORAL, ""));
        map.add(new TranslationMapElement(EntityConst.END_DATE_EXT, DCElementSetNames.DCCOVERAGE, DCElementSetNames.QUALTEMPORAL, ""));
    }
}
