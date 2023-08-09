public class VCardInterpreterCollection implements VCardInterpreter {
    private final Collection<VCardInterpreter> mInterpreterCollection;
    public VCardInterpreterCollection(Collection<VCardInterpreter> interpreterCollection) {
        mInterpreterCollection = interpreterCollection;
    }
    public Collection<VCardInterpreter> getCollection() {
        return mInterpreterCollection;
    }
    public void start() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.start();
        }
    }
    public void end() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.end();
        }
    }
    public void startEntry() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.startEntry();
        }
    }
    public void endEntry() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.endEntry();
        }
    }
    public void startProperty() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.startProperty();
        }
    }
    public void endProperty() {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.endProperty();
        }
    }
    public void propertyGroup(String group) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyGroup(group);
        }
    }
    public void propertyName(String name) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyName(name);
        }
    }
    public void propertyParamType(String type) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyParamType(type);
        }
    }
    public void propertyParamValue(String value) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyParamValue(value);
        }
    }
    public void propertyValues(List<String> values) {
        for (VCardInterpreter builder : mInterpreterCollection) {
            builder.propertyValues(values);
        }
    }
}
