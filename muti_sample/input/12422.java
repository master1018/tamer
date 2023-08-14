public class WriteUTF {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF("Hello, World!");  
        dos.flush();
        if  (baos.size() != dos.size())
            throw new RuntimeException("Miscounted bytes in DataOutputStream.");
    }
}
