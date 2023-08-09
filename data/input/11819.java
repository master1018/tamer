public class HttpReader implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private Snapshot snapshot;
    private OQLEngine engine;
    public HttpReader (Socket s, Snapshot snapshot, OQLEngine engine) {
        this.socket = s;
        this.snapshot = snapshot;
        this.engine = engine;
    }
    public void run() {
        InputStream in = null;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(
                                socket.getOutputStream())));
            out.println("HTTP/1.0 200 OK");
            out.println("Cache-Control: no-cache");
            out.println("Pragma: no-cache");
            out.println();
            if (in.read() != 'G' || in.read() != 'E'
                    || in.read() != 'T' || in.read() != ' ') {
                outputError("Protocol error");
            }
            int data;
            StringBuffer queryBuf = new StringBuffer();
            while ((data = in.read()) != -1 && data != ' ') {
                char ch = (char) data;
                queryBuf.append(ch);
            }
            String query = queryBuf.toString();
            query = java.net.URLDecoder.decode(query, "UTF-8");
            QueryHandler handler = null;
            if (snapshot == null) {
                outputError("The heap snapshot is still being read.");
                return;
            } else if (query.equals("/")) {
                handler = new AllClassesQuery(true, engine != null);
                handler.setUrlStart("");
                handler.setQuery("");
            } else if (query.startsWith("/oql/")) {
                if (engine != null) {
                    handler = new OQLQuery(engine);
                    handler.setUrlStart("");
                    handler.setQuery(query.substring(5));
                }
            } else if (query.startsWith("/oqlhelp/")) {
                if (engine != null) {
                    handler = new OQLHelp();
                    handler.setUrlStart("");
                    handler.setQuery("");
                }
            } else if (query.equals("/allClassesWithPlatform/")) {
                handler = new AllClassesQuery(false, engine != null);
                handler.setUrlStart("../");
                handler.setQuery("");
            } else if (query.equals("/showRoots/")) {
                handler = new AllRootsQuery();
                handler.setUrlStart("../");
                handler.setQuery("");
            } else if (query.equals("/showInstanceCounts/includePlatform/")) {
                handler = new InstancesCountQuery(false);
                handler.setUrlStart("../../");
                handler.setQuery("");
            } else if (query.equals("/showInstanceCounts/")) {
                handler = new InstancesCountQuery(true);
                handler.setUrlStart("../");
                handler.setQuery("");
            } else if (query.startsWith("/instances/")) {
                handler = new InstancesQuery(false);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(11));
            }  else if (query.startsWith("/newInstances/")) {
                handler = new InstancesQuery(false, true);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(14));
            }  else if (query.startsWith("/allInstances/")) {
                handler = new InstancesQuery(true);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(14));
            }  else if (query.startsWith("/allNewInstances/")) {
                handler = new InstancesQuery(true, true);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(17));
            } else if (query.startsWith("/object/")) {
                handler = new ObjectQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(8));
            } else if (query.startsWith("/class/")) {
                handler = new ClassQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(7));
            } else if (query.startsWith("/roots/")) {
                handler = new RootsQuery(false);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(7));
            } else if (query.startsWith("/allRoots/")) {
                handler = new RootsQuery(true);
                handler.setUrlStart("../");
                handler.setQuery(query.substring(10));
            } else if (query.startsWith("/reachableFrom/")) {
                handler = new ReachableQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(15));
            } else if (query.startsWith("/rootStack/")) {
                handler = new RootStackQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(11));
            } else if (query.startsWith("/histo/")) {
                handler = new HistogramQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(7));
            } else if (query.startsWith("/refsByType/")) {
                handler = new RefsByTypeQuery();
                handler.setUrlStart("../");
                handler.setQuery(query.substring(12));
            } else if (query.startsWith("/finalizerSummary/")) {
                handler = new FinalizerSummaryQuery();
                handler.setUrlStart("../");
                handler.setQuery("");
            } else if (query.startsWith("/finalizerObjects/")) {
                handler = new FinalizerObjectsQuery();
                handler.setUrlStart("../");
                handler.setQuery("");
            }
            if (handler != null) {
                handler.setOutput(out);
                handler.setSnapshot(snapshot);
                handler.run();
            } else {
                outputError("Query '" + query + "' not implemented");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
    private void outputError(String msg) {
        out.println();
        out.println("<html><body bgcolor=\"#ffffff\">");
        out.println(msg);
        out.println("</body></html>");
    }
}
