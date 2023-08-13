public class DistinctEntrySetElements {
    public static void main(String[] args) throws Exception {
        final ConcurrentHashMap<String, String> concurrentHashMap =
            new ConcurrentHashMap<>();
        concurrentHashMap.put("One", "Un");
        concurrentHashMap.put("Two", "Deux");
        concurrentHashMap.put("Three", "Trois");
        Set<Map.Entry<String, String>> entrySet = concurrentHashMap.entrySet();
        HashSet<Map.Entry<String, String>> hashSet = new HashSet<>(entrySet);
        if (false == hashSet.equals(entrySet)) {
            throw new RuntimeException("Test FAILED: Sets are not equal.");
        }
        if (hashSet.hashCode() != entrySet.hashCode()) {
            throw new RuntimeException("Test FAILED: Set's hashcodes are not equal.");
        }
    }
}
