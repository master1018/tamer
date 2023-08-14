public class EntrySetIteratorRemoveInvalidatesEntry {
    public static void main(String[] args) throws Exception {
        final IdentityHashMap<String, String> identityHashMap =
            new IdentityHashMap<>();
        identityHashMap.put("One", "Un");
        identityHashMap.put("Two", "Deux");
        identityHashMap.put("Three", "Trois");
        Iterator<Map.Entry<String, String>> entrySetIterator =
            identityHashMap.entrySet().iterator();
        Map.Entry<String, String> entry = entrySetIterator.next();
        entrySetIterator.remove();
        try {
            entry.getKey();
            throw new RuntimeException("Test FAILED: Entry not invalidated by removal.");
        } catch (Exception e) { }
    }
}
