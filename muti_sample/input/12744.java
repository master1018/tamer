public class TestIndexedJarWithBadSignature {
    public static void main(String...args) throws Throwable {
        try (JarInputStream jis = new JarInputStream(
                 new FileInputStream(System.getProperty("test.src", ".") +
                                     System.getProperty("file.separator") +
                                     "BadSignedJar.jar")))
        {
            JarEntry je1 = jis.getNextJarEntry();
            while(je1!=null){
                System.out.println("Jar Entry1==>"+je1.getName());
                je1 = jis.getNextJarEntry(); 
            }
            throw new RuntimeException(
                "Test Failed:Security Exception not being thrown");
        } catch (IOException ie){
            ie.printStackTrace();
        } catch (SecurityException e) {
            System.out.println("Test passed: Security Exception thrown as expected");
        }
    }
}
