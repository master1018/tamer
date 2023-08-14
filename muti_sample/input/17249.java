public class RelativeURL {
    public static void main(String[] args) throws Exception {
        URL base = new URL("http", "Layout", -1, "example1.html");
        URL derived = new URL(base, "Graph.html");
        base = new URL("http", "www.sun.com", -1, "index.html");
        derived = new URL(base, "www.sun.com/index.html");
    }
}
