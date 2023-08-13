public class StandaloneQualifiedSuper {
    public class AS { }
    public class BS { }
    public class CS { }
    public class A extends AS {
        A() { super(); }
        public class B extends BS {
            B() { super(); }
            public class C extends CS {
                C() { super(); }
                void test() {
                    System.out.println(B.super);  
                }
            }
        }
    }
}
