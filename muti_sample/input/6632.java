public class NewChainedExceptions {
    public static void main(String args[]) {
        Throwable interior = new Exception();
        String    message  = "Good heavens!";
        try {
            throw new IllegalStateException(message, interior);
        } catch(IllegalStateException e) {
            if (!(e.getCause() == interior && e.getMessage() == message))
                throw new RuntimeException("1");
        }
        try {
            throw new IllegalStateException(interior);
        } catch(IllegalStateException e) {
            if (!(e.getCause() == interior &&
                  e.getMessage().equals(interior.toString())))
                throw new RuntimeException("2");
        }
        try {
            throw new IllegalArgumentException(message, interior);
        } catch(IllegalArgumentException e) {
            if (!(e.getCause() == interior && e.getMessage() == message))
                throw new RuntimeException("3");
        }
        try {
            throw new IllegalArgumentException(interior);
        } catch(IllegalArgumentException e) {
            if (!(e.getCause() == interior &&
                  e.getMessage().equals(interior.toString())))
                throw new RuntimeException("4");
        }
        try {
            throw new UnsupportedOperationException(message, interior);
        } catch(UnsupportedOperationException e) {
            if (!(e.getCause() == interior && e.getMessage() == message))
                throw new RuntimeException("5");
        }
        try {
            throw new UnsupportedOperationException(interior);
        } catch(UnsupportedOperationException e) {
            if (!(e.getCause() == interior &&
                  e.getMessage().equals(interior.toString())))
                throw new RuntimeException("6");
        }
    }
}
