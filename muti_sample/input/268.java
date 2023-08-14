class NameContext {
    private Hashtable table;
    private boolean allowCollisions;
    public static synchronized NameContext forName (String name,
                                                    boolean allowCollisions,
                                                    BatchEnvironment env) {
        NameContext result = null;
        if (name == null) {
            name = "null";
        }
        if (env.nameContexts == null) {
            env.nameContexts = new Hashtable();
        } else {
            result = (NameContext) env.nameContexts.get(name);
        }
        if (result == null) {
            result = new NameContext(allowCollisions);
            env.nameContexts.put(name,result);
        }
        return result;
    }
    public NameContext (boolean allowCollisions) {
        this.allowCollisions = allowCollisions;
        table = new Hashtable();
    }
    public void assertPut (String name) throws Exception {
        String message = add(name);
        if (message != null) {
            throw new Exception(message);
        }
    }
    public void put (String name) {
        if (allowCollisions == false) {
            throw new Error("Must use assertPut(name)");
        }
        add(name);
    }
    private String add (String name) {
        String key = name.toLowerCase();
        Name value = (Name) table.get(key);
        if (value != null) {
            if (!name.equals(value.name)) {
                if (allowCollisions) {
                    value.collisions = true;
                } else {
                    return new String("\"" + name + "\" and \"" + value.name + "\"");
                }
            }
        } else {
            table.put(key,new Name(name,false));
        }
        return null;
    }
    public String get (String name) {
        Name it = (Name) table.get(name.toLowerCase());
        String result = name;
        if (it.collisions) {
            int length = name.length();
            boolean allLower = true;
            for (int i = 0; i < length; i++) {
                if (Character.isUpperCase(name.charAt(i))) {
                    result += "_";
                    result += i;
                    allLower = false;
                }
            }
            if (allLower) {
                result += "_";
            }
        }
        return result;
    }
    public void clear () {
        table.clear();
    }
    public class Name {
        public String name;
        public boolean collisions;
        public Name (String name, boolean collisions) {
            this.name = name;
            this.collisions = collisions;
        }
    }
}
