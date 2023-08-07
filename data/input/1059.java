public class ValueObjectDefinitionResolver {
    private Map<String, ValueObject> definitionByIdMap = null;
    private Map<Class<? extends IValueObject>, String> definitionByIFMap = null;
    private ValueObjectDefinitionResolver() {
    }
    private static final class Singleton {
        private static final ValueObjectDefinitionResolver instance = new ValueObjectDefinitionResolver();
    }
    public static final ValueObjectDefinitionResolver getInstance(List<ValueObject> valueObjects) {
        Singleton.instance.populateDefinitionMaps(valueObjects);
        return Singleton.instance;
    }
    private void populateDefinitionMaps(List<ValueObject> valueObjects) {
        for (ValueObject valueObject : valueObjects) {
            this.getDefinitionByIdMap().put(valueObject.getId(), valueObject);
            try {
                @SuppressWarnings("unchecked") Class<? extends IValueObject> ifClass = (Class<? extends IValueObject>) Class.forName(DefinitionProcessor.getTargetFQCN(valueObject));
                this.getDefinitionByIFMap().put(ifClass, valueObject.getId());
            } catch (ClassNotFoundException e) {
                getLogger(this.getClass()).error("Class not found while instantiating VO interface name <" + valueObject.getInterfaceName() + ">. This means that generated code is out of synch. Run JAVO2 compiler and try again.");
            }
        }
    }
    public ValueObject getValueObjectById(String definitionId) {
        ValueObject valueObject = this.getDefinitionByIdMap().get(definitionId);
        if (null == valueObject) {
            getLogger(this.getClass()).error("Cannot find VO definition for the given definition id <" + definitionId + ">.");
            throw new DefinitionNotFoundException(definitionId);
        }
        return valueObject;
    }
    public ValueObject getValueObjectByIFName(Class<? extends IValueObject> definitionIF) {
        String definitionId = this.getDefinitionByIFMap().get(definitionIF);
        if (null == definitionId) {
            getLogger(this.getClass()).error("Cannot find VO definition for the given definition interface <" + definitionIF.getCanonicalName() + ">.");
            throw new DefinitionNotFoundException();
        }
        return this.getValueObjectById(definitionId);
    }
    protected Map<String, ValueObject> getDefinitionByIdMap() {
        return (null == this.definitionByIdMap) ? this.definitionByIdMap = new HashMap<String, ValueObject>() : this.definitionByIdMap;
    }
    protected void setDefinitionByIdMap(Map<String, ValueObject> definitionMap) {
        this.definitionByIdMap = definitionMap;
    }
    protected Map<Class<? extends IValueObject>, String> getDefinitionByIFMap() {
        return (null == this.definitionByIFMap) ? this.definitionByIFMap = new HashMap<Class<? extends IValueObject>, String>() : this.definitionByIFMap;
    }
    protected void setDefinitionByIFMap(Map<Class<? extends IValueObject>, String> definitionByIFMap) {
        this.definitionByIFMap = definitionByIFMap;
    }
}
