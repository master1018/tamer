public class Bot {
    public DB db;
    private String url;
    public Bot(DB db, String url, String archivePath) {
        this.db = db;
        this.url = url + archivePath;
        Patterns.getPatterns(GUI.urls.pattern);
    }
    public void check() {
        new Get(url).control();
        GUI.progress.setValue(1);
    }
    public void forums() {
        String content = new Get(url).toString();
        Parser parser = new Parser(db, content);
        parser.forums();
        GUI.progress.setValue(1);
    }
    public void forumsPageCount() {
        final int[] forums = db.forums();
        for (int i = 0; i < forums.length; i++) {
            String regex = String.format(Patterns.forumsPageCountUrl, forums[i]);
            String content;
            content = new Get(url + regex).toString();
            Parser parser = new Parser(db, content);
            parser.forumPageCount(forums[i]);
            GUI.progress.setValue(i);
        }
    }
    public void posts() {
        int[] topics = db.topics();
        for (int i = 0; i < topics.length; i++) {
            String topicUrl = url + String.format(Patterns.topicsPageCountUrl, topics[i]);
            String content = new Get(topicUrl).toString();
            Parser parser = new Parser(db, content);
            int pageCount = parser.topicPageCount(topics[i]);
            for (int j = 1; j <= pageCount; j++) {
                String regex = String.format(Patterns.postsUrl, topics[i], j);
                content = new Get(url + regex).toString();
                parser = new Parser(db, content);
                int addedPostCount = parser.posts(topics[i]);
                db.postCountInc(topics[i], addedPostCount);
            }
            GUI.progress.setValue(i);
        }
    }
    public void topics() {
        final int[] forums = db.forums();
        for (int i = 0; i < forums.length; i++) {
            for (int j = 1; j <= db.forumPageCount(forums[i]); j++) {
                final int index = j;
                String content = new Get(url + "f-" + forums[i] + "-p-" + index + ".html").toString();
                Parser parser = new Parser(db, content);
                parser.topics(forums[i]);
            }
            GUI.progress.setValue(i + 1);
        }
    }
}
