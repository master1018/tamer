public class LoggingNIOChange {
        public static void main(String args[]) throws Exception {
                ConsoleHandler console = new ConsoleHandler();
                XMLFormatter f = new XMLFormatter();
                try {
                console.setEncoding("junk");
                f.getHead(console);
                console.setEncoding(null);
                f.getHead(console);
                }catch (java.io.UnsupportedEncodingException e) {
                }
        }
}
