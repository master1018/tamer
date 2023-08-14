public class Bug4316602 {
    public static void main(String[] args) throws Exception {
        String language = "ja";
        Locale aLocale = new Locale(language);
        if (aLocale.toString().equals(language)) {
            System.out.println("passed");
        } else {
            System.out.println("Bug4316602 failed");
            throw new Exception("Bug4316602 failed");
        }
    }
}
