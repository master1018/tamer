class CompoundPrintable implements CountingPrintable {
    private final Queue<CountingPrintable> printables;
    private int offset = 0;
    public CompoundPrintable(List<CountingPrintable> printables) {
        this.printables = new LinkedList<CountingPrintable>(printables);
    }
    public int print(final Graphics graphics,
                     final PageFormat pf,
                     final int pageIndex) throws PrinterException {
        int ret = NO_SUCH_PAGE;
        while (printables.peek() != null) {
            ret = printables.peek().print(graphics, pf, pageIndex - offset);
            if (ret == PAGE_EXISTS) {
                break;
            } else {
                offset += printables.poll().getNumberOfPages();
            }
        }
        return ret;
    }
    public int getNumberOfPages() {
        return offset;
    }
}
