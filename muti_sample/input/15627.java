public class BadTwrSyntax implements AutoCloseable {
    public static void main(String... args) throws Exception {
        try(BadTwr twrflow = new BadTwr();;) {
            System.out.println(twrflow.toString());
        }
        try(BadTwr twrflow = new BadTwr();) {
            System.out.println(twrflow.toString());
        }
    }
    public void close() {
        ;
    }
}
