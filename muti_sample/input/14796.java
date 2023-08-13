public class SeedGeneratorChoice {
    public static void main(String... arguments) throws Exception {
        byte[] bytes;
        SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
        bytes = prng.generateSeed(1);
    }
}
