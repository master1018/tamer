class OpenBook implements Pageable {
    private PageFormat mFormat;
    private Printable mPainter;
    OpenBook(PageFormat format, Printable painter) {
        mFormat = format;
        mPainter = painter;
    }
    public int getNumberOfPages(){
        return UNKNOWN_NUMBER_OF_PAGES;
    }
    public PageFormat getPageFormat(int pageIndex) {
        return mFormat;
    }
    public Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException
    {
        return mPainter;
    }
}
