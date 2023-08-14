public class CompositeType extends OpenType<CompositeData> {
    static final long serialVersionUID = -5366242454346948798L;
    private TreeMap<String,String> nameToDescription;
    private TreeMap<String,OpenType<?>> nameToType;
    private transient Integer myHashCode = null;
    private transient String  myToString = null;
    private transient Set<String> myNamesSet = null;
    public CompositeType(String        typeName,
                         String        description,
                         String[]      itemNames,
                         String[]      itemDescriptions,
                         OpenType<?>[] itemTypes) throws OpenDataException {
        super(CompositeData.class.getName(), typeName, description, false);
        checkForNullElement(itemNames, "itemNames");
        checkForNullElement(itemDescriptions, "itemDescriptions");
        checkForNullElement(itemTypes, "itemTypes");
        checkForEmptyString(itemNames, "itemNames");
        checkForEmptyString(itemDescriptions, "itemDescriptions");
        if ( (itemNames.length != itemDescriptions.length) || (itemNames.length != itemTypes.length) ) {
            throw new IllegalArgumentException("Array arguments itemNames[], itemDescriptions[] and itemTypes[] "+
                                               "should be of same length (got "+ itemNames.length +", "+
                                               itemDescriptions.length +" and "+ itemTypes.length +").");
        }
        nameToDescription = new TreeMap<String,String>();
        nameToType        = new TreeMap<String,OpenType<?>>();
        String key;
        for (int i=0; i<itemNames.length; i++) {
            key = itemNames[i].trim();
            if (nameToDescription.containsKey(key)) {
                throw new OpenDataException("Argument's element itemNames["+ i +"]=\""+ itemNames[i] +
                                            "\" duplicates a previous item names.");
            }
            nameToDescription.put(key, itemDescriptions[i].trim());
            nameToType.put(key, itemTypes[i]);
        }
    }
    private static void checkForNullElement(Object[] arg, String argName) {
        if ( (arg == null) || (arg.length == 0) ) {
            throw new IllegalArgumentException("Argument "+ argName +"[] cannot be null or empty.");
        }
        for (int i=0; i<arg.length; i++) {
            if (arg[i] == null) {
                throw new IllegalArgumentException("Argument's element "+ argName +"["+ i +"] cannot be null.");
            }
        }
    }
    private static void checkForEmptyString(String[] arg, String argName) {
        for (int i=0; i<arg.length; i++) {
            if (arg[i].trim().equals("")) {
                throw new IllegalArgumentException("Argument's element "+ argName +"["+ i +"] cannot be an empty string.");
            }
        }
    }
    public boolean containsKey(String itemName) {
        if (itemName == null) {
            return false;
        }
        return nameToDescription.containsKey(itemName);
    }
    public String getDescription(String itemName) {
        if (itemName == null) {
            return null;
        }
        return nameToDescription.get(itemName);
    }
    public OpenType<?> getType(String itemName) {
        if (itemName == null) {
            return null;
        }
        return (OpenType<?>) nameToType.get(itemName);
    }
    public Set<String> keySet() {
        if (myNamesSet == null) {
            myNamesSet = Collections.unmodifiableSet(nameToDescription.keySet());
        }
        return myNamesSet; 
    }
    public boolean isValue(Object obj) {
        if (!(obj instanceof CompositeData)) {
            return false;
        }
        CompositeData value = (CompositeData) obj;
        CompositeType valueType = value.getCompositeType();
        return this.isAssignableFrom(valueType);
    }
    @Override
    boolean isAssignableFrom(OpenType<?> ot) {
        if (!(ot instanceof CompositeType))
            return false;
        CompositeType ct = (CompositeType) ot;
        if (!ct.getTypeName().equals(getTypeName()))
            return false;
        for (String key : keySet()) {
            OpenType<?> otItemType = ct.getType(key);
            OpenType<?> thisItemType = getType(key);
            if (otItemType == null ||
                    !thisItemType.isAssignableFrom(otItemType))
                return false;
        }
        return true;
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        CompositeType other;
        try {
            other = (CompositeType) obj;
        } catch (ClassCastException e) {
            return false;
        }
        if ( ! this.getTypeName().equals(other.getTypeName()) ) {
            return false;
        }
        if ( ! this.nameToType.equals(other.nameToType) ) {
            return false;
        }
        return true;
    }
    public int hashCode() {
        if (myHashCode == null) {
            int value = 0;
            value += this.getTypeName().hashCode();
            for (String key : nameToDescription.keySet()) {
                value += key.hashCode();
                value += this.nameToType.get(key).hashCode();
            }
            myHashCode = Integer.valueOf(value);
        }
        return myHashCode.intValue();
    }
    public String toString() {
        if (myToString == null) {
            final StringBuilder result = new StringBuilder();
            result.append(this.getClass().getName());
            result.append("(name=");
            result.append(getTypeName());
            result.append(",items=(");
            int i=0;
            Iterator<String> k=nameToType.keySet().iterator();
            String key;
            while (k.hasNext()) {
                key = k.next();
                if (i > 0) result.append(",");
                result.append("(itemName=");
                result.append(key);
                result.append(",itemType=");
                result.append(nameToType.get(key).toString() +")");
                i++;
            }
            result.append("))");
            myToString = result.toString();
        }
        return myToString;
    }
}
