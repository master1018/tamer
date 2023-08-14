public class BadAgent {
    public static void agentmain(String args) {
        throw new RuntimeException("Something bad happened - Bye!");
    }
}
