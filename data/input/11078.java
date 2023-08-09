public final class Test4994637 extends AbstractTest<HashMap> {
    public static void main(String[] args) {
        new Test4994637().test(true);
    }
    @Override
    protected CustomMap getObject() {
        return new CustomMap();
    }
    @Override
    protected CustomMap getAnotherObject() {
        CustomMap map = new CustomMap();
        map.clear();
        map.put(null, "zero");
        return map;
    }
    public static final class CustomMap extends HashMap<String, String> {
        public CustomMap() {
            put("1", "one");
            put("2", "two");
            put("3", "three");
        }
    }
}
