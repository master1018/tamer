public class ForInnerClass {
    private class Inner {
    }
    protected class Protected {
    }
    public static void main(String[] args) throws Exception {
        int m = 0;
        m = Inner.class.getModifiers() & (~Modifier.SYNCHRONIZED);
        if (m != Modifier.PRIVATE)
            throw new Exception("Access bits for innerclass not from " +
                                "InnerClasses attribute");
        m = Protected.class.getModifiers() & (~Modifier.SYNCHRONIZED);
        if (m != Modifier.PROTECTED)
            throw new Exception("Protected inner class wronged modifiers");
    }
}
