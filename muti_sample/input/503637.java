class MockEnum2 implements Serializable {
    private static final long serialVersionUID = -4812214670022262730L;
    enum Sample {
        LARRY, MOE, CURLY
    }
    enum Sample2 {
        RED, BLUE, YELLO
    }
    String str;
    int i;
    Sample samEnum;
    Sample larry = Sample.LARRY;
    String myStr = "LARRY";
    MockEnum2() {
        str = "test";
        i = 99;
        samEnum = larry;
    }
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof MockEnum2)) {
            return false;
        }
        MockEnum2 test = (MockEnum2) arg0;
        if (str.equals(test.str) && i == test.i && samEnum == test.samEnum
                && myStr.equals(test.myStr)) {
            return true;
        }
        return false;
    }
}
