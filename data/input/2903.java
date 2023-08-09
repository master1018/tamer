public class NonJavaNames {
    public static class Baz {
        public Baz(){}
    }
    public static interface myInterface {
    }
     NonJavaNames.myInterface create(){
         class Baz2 implements NonJavaNames.myInterface {
             public Baz2(){}
         }
        return new Baz2();
     }
    public static void main(String[] args) throws Exception {
        NonJavaNames.Baz bz = new NonJavaNames.Baz();
        String name;
        if (Class.forName(name=bz.getClass().getName()) != NonJavaNames.Baz.class) {
            System.err.println("Class object from forName does not match object.class.");
            System.err.println("Failures for class ``" + name + "''.");
            throw new RuntimeException();
        }
        NonJavaNames.myInterface bz2 = (new NonJavaNames()).create();
        if (Class.forName(name=bz2.getClass().getName()) != bz2.getClass()) {
            System.err.println("Class object from forName does not match getClass.");
            System.err.println("Failures for class ``" + name + "''.");
            throw new RuntimeException();
        }
        String goodNonJavaClassNames []  = {
            ",",
            "+",
            "-",
            "0",
            "3",
            "Z",
            "]"
        };
        for(String s : goodNonJavaClassNames) {
            System.out.println("Testing good class name ``" + s + "''");
            Class.forName(s);
        }
        String badNonJavaClassNames []  = {
            ";",
            "[",
            "."
        };
        for(String s : badNonJavaClassNames) {
            System.out.println("Testing bad class name ``" + s + "''");
            try {
                Class.forName(s);
            } catch (Exception e) {
                continue;
            }
            throw new RuntimeException("Bad class name ``" + s + "'' accepted.");
        }
    }
}
