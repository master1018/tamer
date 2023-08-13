public class ReadObject extends Hashtable {
    class ValueWrapper implements Serializable {
        private Object mValue;
        ValueWrapper(Object value) {
            mValue = value;
        }
        Object getValue() {
            return mValue;
        }
    };
    public Object get(Object key) {
        ValueWrapper valueWrapper = (ValueWrapper)super.get(key);
        Object value = valueWrapper.getValue();
        if(value instanceof ValueWrapper)
            throw new RuntimeException("Hashtable.get bug");
        return value;
    }
    public Object put(Object key, Object value) {
        if(value instanceof ValueWrapper)
            throw new RuntimeException(
                "Hashtable.put bug: value is already wrapped");
        ValueWrapper valueWrapper = new ValueWrapper(value);
        super.put(key, valueWrapper);
        return value;
    }
    private static Object copyObject(Object oldObj) {
        Object newObj = null;
        try {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            p.writeObject(oldObj);
            byte[] byteArray = ostream.toByteArray();
            ByteArrayInputStream istream = new ByteArrayInputStream(byteArray);
            ObjectInputStream q = new ObjectInputStream(istream);
            newObj = q.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newObj;
    }
    public static void main(String[] args) {
        ReadObject myHashtable = new ReadObject();
        myHashtable.put("key", "value");
        ReadObject myHashtableCopy = (ReadObject)copyObject(myHashtable);
        String value = (String)myHashtableCopy.get("key");
    }
};
