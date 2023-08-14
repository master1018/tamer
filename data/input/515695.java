class MockEnum implements Serializable {
    private static final long serialVersionUID = -1678507713086705252L;
    enum Sample {
        LARRY, MOE, CURLY
    }
    enum Sample2 {
        RED, BLUE, YELLO
    }
    String str;
    int i;
    Sample2 samEnum;
    Sample larry = Sample.LARRY;
    MockEnum() {
        str = "test";
        i = 99;
        samEnum = Sample2.BLUE;
    }
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof MockEnum)) {
            return false;
        }
        MockEnum test = (MockEnum) arg0;
        if (str.equals(test.str) && i == test.i && samEnum == test.samEnum) {
            return true;
        }
        return false;
    }
}
