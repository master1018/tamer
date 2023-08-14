public class Headers implements Map<String,List<String>> {
        HashMap<String,List<String>> map;
        public Headers () {map = new HashMap<String,List<String>>(32);}
        private String normalize (String key) {
            if (key == null) {
                return null;
            }
            int len = key.length();
            if (len == 0) {
                return key;
            }
            char[] b = key.toCharArray();
            if (b[0] >= 'a' && b[0] <= 'z') {
                b[0] = (char)(b[0] - ('a' - 'A'));
            }
            for (int i=1; i<len; i++) {
                if (b[i] >= 'A' && b[i] <= 'Z') {
                    b[i] = (char) (b[i] + ('a' - 'A'));
                }
            }
            return new String(b);
        }
        public int size() {return map.size();}
        public boolean isEmpty() {return map.isEmpty();}
        public boolean containsKey(Object key) {
            if (key == null) {
                return false;
            }
            if (!(key instanceof String)) {
                return false;
            }
            return map.containsKey (normalize((String)key));
        }
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }
        public List<String> get(Object key) {
            return map.get(normalize((String)key));
        }
        public String getFirst (String key) {
            List<String> l = map.get(normalize((String)key));
            if (l == null) {
                return null;
            }
            return l.get(0);
        }
        public List<String> put(String key, List<String> value) {
            return map.put (normalize(key), value);
        }
        public void add (String key, String value) {
            String k = normalize(key);
            List<String> l = map.get(k);
            if (l == null) {
                l = new LinkedList<String>();
                map.put(k,l);
            }
            l.add (value);
        }
        public void set (String key, String value) {
            LinkedList<String> l = new LinkedList<String>();
            l.add (value);
            put (key, l);
        }
        public List<String> remove(Object key) {
            return map.remove(normalize((String)key));
        }
        public void putAll(Map<? extends String,? extends List<String>> t)  {
            map.putAll (t);
        }
        public void clear() {map.clear();}
        public Set<String> keySet() {return map.keySet();}
        public Collection<List<String>> values() {return map.values();}
        public Set<Map.Entry<String, List<String>>> entrySet() {
            return map.entrySet();
        }
        public boolean equals(Object o) {return map.equals(o);}
        public int hashCode() {return map.hashCode();}
    }
