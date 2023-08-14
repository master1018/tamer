public class AvailableCharsetNames {
    public static void main(String[] args) throws Exception {
        Iterator charsetIterator = Charset.availableCharsets().keySet().iterator();
        while (charsetIterator.hasNext()) {
            String charsetName = (String) charsetIterator.next();
            Charset charset = Charset.forName(charsetName);
            if (!charset.name().equals(charsetName)) {
                throw new Exception("Error: Charset name mismatch - expected "
                                   + charsetName + ", got " + charset.name());
            }
        }
    }
}
