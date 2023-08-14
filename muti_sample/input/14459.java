class BarNeg2a<T> {
    BarNeg2a.Inner.InnerMost object = new BarNeg2a.Inner<?>.InnerMost();
    static class Inner<S> {
        static class InnerMost {
        }
    }
}
