public class TestWrite {
    public static void main(String args[])
        throws Exception
    {
        ByteArrayOutputStream bos;
        OutputStreamWriter osw;
        byte[] array;
        try{
            bos = new ByteArrayOutputStream();
            osw = new OutputStreamWriter(bos, "EUCJIS");
            osw.write('a');
            for(int count = 0; count < 10000; ++count)
                osw.write('\u3042');   
            osw.close();
            array = bos.toByteArray();
        } catch (UnsupportedEncodingException e){
            System.err.println("Unsupported encoding - EUCJIS. ext "
                               + " may not be properly installed. ext is  "
                               + " required for the test to run properly ");
            throw new Exception("Environment is incorrect");
        }
    }
}
