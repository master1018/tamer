public class CollationData_zh_HK extends EmptyListResourceBundle {
    public CollationData_zh_HK() {
        ResourceBundle bundle = LocaleData.getCollationData(Locale.TAIWAN);
        setParent(bundle);
    }
}
