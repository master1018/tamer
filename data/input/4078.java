class RepStepTarg {
    RepStepTarg() {
    }
    Boolean get() { return new Boolean(true) ; }
    public static void main(String[] args) {
        for (int i = 0 ; i < 2000; i++) {
            Boolean  o = new RepStepTarg().get();
        }
    }
}
