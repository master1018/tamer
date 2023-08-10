public class ResultListCell extends ResultCell {
    public LocalisedList items;
    private int maxColumnItems;
    protected boolean xlsExpand;
    public ResultListCell(String type) {
        super(type);
        items = new LocalisedList();
        list = true;
        xlsExpand = false;
    }
    public boolean isList() {
        return items.size() > 1 ? true : false;
    }
    public String toString() {
        if (items.size() == 0 || list == false) return super.toString();
        Iterator i = this.items.iterator();
        StringBuffer sb = new StringBuffer();
        while (i.hasNext()) {
            ResultCell c = (ResultCell) i.next();
            sb.append(c.toString());
            if (i.hasNext()) sb.append(" ; ");
        }
        return sb.toString();
    }
    public ResultCell getFirstItem() {
        return (ResultCell) items.get(0);
    }
    public String getExtraText() {
        return getExtraItemsWithToken("");
    }
    public String getExtraHTML() {
        return getExtraItemsWithToken("<br> ");
    }
    public String getExtraItemsWithToken(String separator) {
        String ret = "";
        Iterator i = items.iterator();
        if (!i.hasNext()) return ""; else i.next();
        while (i.hasNext()) {
            ResultCell element = (ResultCell) i.next();
            ret += "- " + element.toString() + (i.hasNext() ? separator : "");
        }
        return ret;
    }
    public ResultListCell(String clang, String type) {
        super(type);
        items = new LocalisedList(clang);
    }
    public int compareTo(Object o) {
        ResultListCell rc2 = (ResultListCell) o;
        if (items.size() == 0) return -1;
        if (rc2.getItems().size() == 0) return 1;
        Comparable c = (Comparable) rc2.getItems().get(0);
        Comparable c0 = (Comparable) items.get(0);
        return c0.compareTo(c);
    }
    public LocalisedList getItems() {
        return items;
    }
    public void setItems(LocalisedList items) {
        this.items = items;
    }
    public boolean isXlsExpand() {
        return xlsExpand;
    }
    public void setXlsExpand(boolean xlsExpand) {
        this.xlsExpand = xlsExpand;
    }
    public int appendXlsCells(WritableSheet sheet, int x, int y, boolean header) throws RowsExceededException, WriteException {
        if (!xlsExpand) return super.appendXlsCells(sheet, x, y, header);
        Iterator i = items.iterator();
        ResultCell rc_saved = null;
        int xapp = 0;
        int xapp_saved = 0;
        while (i.hasNext()) {
            ResultCell rc = (ResultCell) i.next();
            rc_saved = rc;
            xapp_saved = rc.appendXlsCells(sheet, x + xapp, y, header);
            xapp += xapp_saved;
        }
        if (items.size() < maxColumnItems) {
            for (int k = 0; k < maxColumnItems - items.size(); k++) {
                if (header) xapp += rc_saved.appendXlsCells(sheet, x + xapp, y, header); else xapp += xapp_saved;
            }
        }
        return xapp;
    }
    public int getMaxColumnItems() {
        return maxColumnItems;
    }
    public void setMaxColumnItems(int maxColumnItems) {
        this.maxColumnItems = maxColumnItems;
    }
}
