public class sergoog2q extends Thread {
    public static String ss1 = "null", ss2 = "null";
    public static void main(String[] args) throws Exception {
        try {
            ss1 = args[0];
        } catch (Exception e) {
        }
        new sergoog2q().run();
    }
    public void run() {
        try {
            SpreadsheetService myService = new SpreadsheetService("exampleCo-exampleApp-1");
            myService.setUserCredentials("33@quicklydone.com", "quicklydone");
            SpreadsheetFeed myFeed = myService.getFeed(new URL("http:
            SpreadsheetEntry se = myFeed.getEntries().get(0);
            WorksheetEntry myWorksheet = se.getWorksheets().get(0);
            ListQuery myListQuery = new ListQuery(myWorksheet.getListFeedUrl());
            myListQuery.setSpreadsheetQuery(ss1);
            ListFeed pp = myService.query(myListQuery, ListFeed.class);
            System.out.println("Entrie size = " + pp.getEntries().size());
            ListEntry myEntry = new ListEntry();
            myEntry = pp.getEntries().get(0);
            TextContent cc = (TextContent) myEntry.getContent();
            String ss = cc.getContent().getPlainText();
            System.out.println(ss);
            sta.wtf(ss, "_____.txt");
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
