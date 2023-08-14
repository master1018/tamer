public final class Test4993777 extends AbstractTest {
    public static void main(String[] args) {
        new Test4993777().test(true);
    }
    protected ArrayBean getObject() {
        ArrayBean data = new ArrayBean();
        data.setArray(
                new InnerObject("one"),
                new InnerObject("two")
        );
        return data;
    }
    protected ArrayBean getAnotherObject() {
        ArrayBean data = new ArrayBean();
        data.setArray2D(
                new InnerObject[] {
                        new InnerObject("1"),
                        new InnerObject("2"),
                        new InnerObject("3"),
                },
                new InnerObject[] {
                        new InnerObject("4"),
                        new InnerObject("5"),
                        new InnerObject("6"),
                }
        );
        return data;
    }
    public static final class ArrayBean {
        private InnerObject[] array;
        private InnerObject[][] array2D;
        public InnerObject[] getArray() {
            return this.array;
        }
        public void setArray(InnerObject... array) {
            this.array = array;
        }
        public InnerObject[][] getArray2D() {
            return this.array2D;
        }
        public void setArray2D(InnerObject[]... array2D) {
            this.array2D = array2D;
        }
    }
    public static final class InnerObject {
        private String name;
        public InnerObject() {
        }
        private InnerObject(String name) {
            this.name = name;
        }
        public String getName() {
            return this.name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
}
