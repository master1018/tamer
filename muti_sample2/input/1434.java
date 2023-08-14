public class test {
    public void xestHer1509() throws IOException {
        HashMap<String, String> testUrls = new HashMap<String, String>();
        testUrls.put("http:
        testUrls.put("http:
        testUrls.put("http:
        for (String url : testUrls.keySet()) {
            HttpRecorder recorder = HttpRecorder.wrapInputStreamWithHttpRecord(getTmpDir(), this.getClass().getName(), new URL(url).openStream(), null);
            CrawlURI curi = setupCrawlURI(recorder, url);
            long startTime = System.currentTimeMillis();
            this.extractor.innerProcess(curi);
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info(this.extractor.getClass().getSimpleName() + " took " + elapsed + "ms to process " + url);
            boolean foundIt = false;
            for (Link link : curi.getOutLinks()) {
                logger.info("found link: " + link);
                foundIt = foundIt || link.getDestination().toString().endsWith(testUrls.get(url));
            }
            assertTrue("failed to extract link \"" + testUrls.get(url) + "\" from " + url, foundIt);
        }
    }
}
