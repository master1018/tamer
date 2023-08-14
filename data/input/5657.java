class BasicTest<T extends @A Object> extends @B LinkedList<T> implements @C List<T> {
    void test() {
        Class<?> c = @A String.class;
        Object o = (@A Object) "foo";
        String s = new @A String("bar");
        boolean b = o instanceof @A Object;
        @A Map<@B List<@C String>, @D String> map =
            new @A HashMap<@B List<@C String>, @D String>();
        Class<? extends @A String> c2 = @A String.class;
    }
    void test2() @C @D throws @A IllegalArgumentException, @B IOException {
    }
    void test3(Object @A... objs) {
    }
}
