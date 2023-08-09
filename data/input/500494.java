public class PrintPsTree {
    public static void main(String[] args)
            throws IOException, ClassNotFoundException {
        if (args.length != 1) {
            System.err.println("Usage: PrintCsv [compiled log file]");
            System.exit(0);
        }
        FileInputStream fin = new FileInputStream(args[0]);
        ObjectInputStream oin = new ObjectInputStream(
                new BufferedInputStream(fin));
        Root root = (Root) oin.readObject();
        for (Proc proc : root.processes.values()) {
            if (proc.parent == null) {
                proc.print();                                
            }
        }
    }
}
