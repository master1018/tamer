public class KtabCheck {
    public static void main(String[] args) throws Exception {
        System.out.println("Checking " + Arrays.toString(args));
        KeyTab ktab = KeyTab.getInstance(args[0]);
        Set<String> expected = new HashSet<>();
        for (int i=1; i<args.length; i += 2) {
            expected.add(args[i]+":"+args[i+1]);
        }
        for (KeyTabEntry e: ktab.getEntries()) {
            String vne = e.getKey().getKeyVersionNumber() + ":" +
                    e.getKey().getEType();
            if (!expected.contains(vne)) {
                throw new Exception("No " + vne + " in expected");
            }
            expected.remove(vne);
        }
        if (!expected.isEmpty()) {
            throw new Exception("Extra elements in expected");
        }
    }
}
