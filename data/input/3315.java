class CompilerError extends Error {
    Throwable e;
    public CompilerError(String msg) {
        super(msg);
        this.e = this;
    }
    public CompilerError(Exception e) {
        super(e.getMessage());
        this.e = e;
    }
    public void printStackTrace() {
        if (e == this)
            super.printStackTrace();
        else
            e.printStackTrace();
    }
}
