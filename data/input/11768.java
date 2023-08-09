class InvalidInferredTypes {
    <T extends List<? super T>> T makeList() {
        return null;
    }
    public void test() {
        List<? super String> l = makeList();
    }
}
