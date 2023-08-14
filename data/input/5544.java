public class bug6788484 {
    public static void main(String[] args) throws Exception {
        DefaultTableCellHeaderRenderer.getColumnSortOrder(null, 0);
    }
}
