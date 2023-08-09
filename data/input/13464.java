public class OptionLister {
    private static final boolean debug = false;
    private List<URL> sources;
    public OptionLister(List<URL> sources) {
        this.sources = sources;
    }
    public void print(PrintStream ps) {
        Comparator<OptionFormat> c = new Comparator<OptionFormat>() {
               public int compare(OptionFormat o1, OptionFormat o2) {
                   OptionFormat of1 = o1;
                   OptionFormat of2 = o2;
                   return (of1.getName().compareTo(of2.getName()));
               }
        };
        Set<OptionFormat> options = new TreeSet<OptionFormat>(c);
        for (URL u : sources) {
            try {
                Reader r = new BufferedReader(
                        new InputStreamReader(u.openStream()));
                Set<OptionFormat> s = new Parser(r).parseOptions();
                options.addAll(s);
            } catch (IOException e) {
                if (debug) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            } catch (ParserException e) {
                System.err.println(u + ": " + e.getMessage());
                System.err.println("Parsing of " + u + " aborted");
            }
        }
        for ( OptionFormat of : options) {
            if (of.getName().compareTo("timestamp") == 0) {
              continue;
            }
            ps.println("-" + of.getName());
        }
    }
}
