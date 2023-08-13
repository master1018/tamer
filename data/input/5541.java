public final class java_lang_Enum extends AbstractTest {
    public static void main(String[] args) {
        new java_lang_Enum().test(true);
    }
    protected Object getObject() {
        return Alpha.A;
    }
    protected Object getAnotherObject() {
        return Alpha.Z;
    }
    public enum Alpha {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
    }
}
