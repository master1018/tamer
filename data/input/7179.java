public class Xdd {
    static void usage() {
        System.out.println("Usage: java Xdd <file>");
        System.out.println("       java Xdd -set <name>=<value> <file>");
        System.out.println("       java Xdd -get <name> <file>");
        System.out.println("       java Xdd -del <name> <file>");
        System.exit(-1);
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 1 && args.length != 3)
            usage();
        Path file = (args.length == 1) ?
            Paths.get(args[0]) : Paths.get(args[2]);
        FileStore store = Files.getFileStore(file);
        if (!store.supportsFileAttributeView(UserDefinedFileAttributeView.class)) {
            System.err.format("UserDefinedFileAttributeView not supported on %s\n", store);
            System.exit(-1);
        }
        UserDefinedFileAttributeView view =
            Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
        if (args.length == 1) {
            System.out.println("    Size  Name");
            System.out.println("--------  --------------------------------------");
            for (String name: view.list()) {
                System.out.format("%8d  %s\n", view.size(name), name);
            }
            return;
        }
        if (args[0].equals("-set")) {
            String[] s = args[1].split("=");
            if (s.length != 2)
                usage();
            String name = s[0];
            String value = s[1];
            view.write(name, Charset.defaultCharset().encode(value));
            return;
        }
        if (args[0].equals("-get")) {
            String name = args[1];
            int size = view.size(name);
            ByteBuffer buf = ByteBuffer.allocateDirect(size);
            view.read(name, buf);
            buf.flip();
            System.out.println(Charset.defaultCharset().decode(buf).toString());
            return;
        }
        if (args[0].equals("-del")) {
            view.delete(args[1]);
            return;
        }
        usage();
    }
 }
