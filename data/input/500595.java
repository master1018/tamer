public class PropertyNode {
    public String propName;
    public String propValue;
    public List<String> propValue_vector;
    public byte[] propValue_bytes;
    public ContentValues paramMap;
    public Set<String> paramMap_TYPE;
    public Set<String> propGroupSet;
    public PropertyNode() {
        propName = "";
        propValue = "";
        propValue_vector = new ArrayList<String>();
        paramMap = new ContentValues();
        paramMap_TYPE = new HashSet<String>();
        propGroupSet = new HashSet<String>();
    }
    public PropertyNode(
            String propName, String propValue, List<String> propValue_vector,
            byte[] propValue_bytes, ContentValues paramMap, Set<String> paramMap_TYPE,
            Set<String> propGroupSet) {
        if (propName != null) {
            this.propName = propName;
        } else {
            this.propName = "";
        }
        if (propValue != null) {
            this.propValue = propValue;
        } else {
            this.propValue = "";
        }
        if (propValue_vector != null) {
            this.propValue_vector = propValue_vector;
        } else {
            this.propValue_vector = new ArrayList<String>();
        }
        this.propValue_bytes = propValue_bytes;
        if (paramMap != null) {
            this.paramMap = paramMap;
        } else {
            this.paramMap = new ContentValues();
        }
        if (paramMap_TYPE != null) {
            this.paramMap_TYPE = paramMap_TYPE;
        } else {
            this.paramMap_TYPE = new HashSet<String>();
        }
        if (propGroupSet != null) {
            this.propGroupSet = propGroupSet;
        } else {
            this.propGroupSet = new HashSet<String>();
        }
    }
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "PropertyNode does not provide hashCode() implementation intentionally.");
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PropertyNode)) {
            return false;
        }
        PropertyNode node = (PropertyNode)obj;
        if (propName == null || !propName.equals(node.propName)) {
            return false;
        } else if (!paramMap_TYPE.equals(node.paramMap_TYPE)) {
            return false;
        } else if (!paramMap_TYPE.equals(node.paramMap_TYPE)) {
            return false;
        } else if (!propGroupSet.equals(node.propGroupSet)) {
            return false;
        }
        if (propValue_bytes != null && Arrays.equals(propValue_bytes, node.propValue_bytes)) {
            return true;
        } else {
            if (!propValue.equals(node.propValue)) {
                return false;
            }
            return (propValue_vector.equals(node.propValue_vector) ||
                    propValue_vector.size() == 1 ||
                    node.propValue_vector.size() == 1);
        }
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("propName: ");
        builder.append(propName);
        builder.append(", paramMap: ");
        builder.append(paramMap.toString());
        builder.append(", paramMap_TYPE: [");
        boolean first = true;
        for (String elem : paramMap_TYPE) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append('"');
            builder.append(elem);
            builder.append('"');
        }
        builder.append("]");
        if (!propGroupSet.isEmpty()) {
            builder.append(", propGroupSet: [");
            first = true;
            for (String elem : propGroupSet) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append('"');
                builder.append(elem);
                builder.append('"');
            }
            builder.append("]");
        }
        if (propValue_vector != null && propValue_vector.size() > 1) {
            builder.append(", propValue_vector size: ");
            builder.append(propValue_vector.size());
        }
        if (propValue_bytes != null) {
            builder.append(", propValue_bytes size: ");
            builder.append(propValue_bytes.length);
        }
        builder.append(", propValue: \"");
        builder.append(propValue);
        builder.append("\"");
        return builder.toString();
    }
}
