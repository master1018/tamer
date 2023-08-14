public class MockCallback implements Callback {
    public void columns(String cols[]) {
        System.out.println("&lt;TH&gt;&lt;TR&gt;");
        for (int i = 0; i <= cols.length; i++) {
            System.out.println("&lt;TD&gt;" + cols[i] + "&lt;/TD&gt;");
        }
        System.out.println("&lt;/TR&gt;&lt;/TH&gt;");
    }
    public boolean newrow(String cols[]) {
        System.out.println("&lt;TR&gt;");
        for (int i = 0; i <= cols.length; i++) {
            System.out.println("&lt;TD&gt;" + cols[i] + "&lt;/TD&gt;");
        }
        System.out.println("&lt;/TR&gt;");
        return false;
    }
    public void types(String[] types) {
    }
}
