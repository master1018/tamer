public class BadEnvp {
    public static void main(String[] args) throws Exception {
        Runtime r = Runtime.getRuntime();
        java.io.File dir = new java.io.File(".");
        String[] envpWithNull = {"FOO=BAR",null};
        try {
            r.exec("echo", envpWithNull);
            throw new Exception("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {} 
        try {
            r.exec("echo", envpWithNull, dir);
            throw new Exception("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {} 
        try {
            r.exec(new String[]{"echo"}, envpWithNull);
            throw new Exception("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {} 
        try {
            r.exec(new String[]{"echo"}, envpWithNull, dir);
            throw new Exception("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {} 
    }
}
