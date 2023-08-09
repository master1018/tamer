public class VCardEntryCounter implements VCardInterpreter {
    private int mCount;
    public int getCount() {
        return mCount;
    }
    public void start() {
    }
    public void end() {
    }
    public void startEntry() {
    }
    public void endEntry() {
        mCount++;
    }
    public void startProperty() {
    }
    public void endProperty() {
    }
    public void propertyGroup(String group) {
    }
    public void propertyName(String name) {
    }
    public void propertyParamType(String type) {
    }
    public void propertyParamValue(String value) {
    }
    public void propertyValues(List<String> values) {
    }    
}
