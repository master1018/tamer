class Privvy implements PrivilegedAction<Integer> {
    private Integer mValue;
    public Privvy(int val) {
        mValue = new Integer(val + 1);
    }
    public Integer run() {
        return mValue;
    }
}
