public class CounterOverflow extends DataOutputStream {
    public CounterOverflow(OutputStream out)  {
        super(out);
        written = Integer.MAX_VALUE;
    }
    public static void main(String[] args) throws Exception {
        CounterOverflow dataOut = new CounterOverflow(System.out);
        dataOut.writeByte(1);
        if (dataOut.size() < 0) {
            throw new Exception("Internal counter less than 0.");
        }
    }
}
