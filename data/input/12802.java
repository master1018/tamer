public class Harness {
    BenchInfo[] binfo;
    public Harness(InputStream in) throws IOException, ConfigFormatException {
        Vector bvec = new Vector();
        StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(in));
        tokens.resetSyntax();
        tokens.wordChars(0, 255);
        tokens.whitespaceChars(0, ' ');
        tokens.commentChar('#');
        tokens.quoteChar('"');
        tokens.eolIsSignificant(true);
        tokens.nextToken();
        while (tokens.ttype != StreamTokenizer.TT_EOF) {
            switch (tokens.ttype) {
                case StreamTokenizer.TT_WORD:
                case '"':                       
                    bvec.add(parseBenchInfo(tokens));
                    break;
                default:                        
                    tokens.nextToken();
                    break;
            }
        }
        binfo = (BenchInfo[]) bvec.toArray(new BenchInfo[bvec.size()]);
    }
    BenchInfo parseBenchInfo(StreamTokenizer tokens)
        throws IOException, ConfigFormatException
    {
        float weight = parseBenchWeight(tokens);
        String name = parseBenchName(tokens);
        Benchmark bench = parseBenchClass(tokens);
        String[] args = parseBenchArgs(tokens);
        if (tokens.ttype == StreamTokenizer.TT_EOL)
            tokens.nextToken();
        return new BenchInfo(bench, name, weight, args);
    }
    float parseBenchWeight(StreamTokenizer tokens)
        throws IOException, ConfigFormatException
    {
        float weight;
        switch (tokens.ttype) {
            case StreamTokenizer.TT_WORD:
            case '"':
                try {
                    weight = Float.parseFloat(tokens.sval);
                } catch (NumberFormatException e) {
                    throw new ConfigFormatException("illegal weight value \"" +
                            tokens.sval + "\" on line " + tokens.lineno());
                }
                tokens.nextToken();
                return weight;
            default:
                throw new ConfigFormatException("missing weight value on line "
                        + tokens.lineno());
        }
    }
    String parseBenchName(StreamTokenizer tokens)
        throws IOException, ConfigFormatException
    {
        String name;
        switch (tokens.ttype) {
            case StreamTokenizer.TT_WORD:
            case '"':
                name = tokens.sval;
                tokens.nextToken();
                return name;
            default:
                throw new ConfigFormatException("missing benchmark name on " +
                        "line " + tokens.lineno());
        }
    }
    Benchmark parseBenchClass(StreamTokenizer tokens)
        throws IOException, ConfigFormatException
    {
        Benchmark bench;
        switch (tokens.ttype) {
            case StreamTokenizer.TT_WORD:
            case '"':
                try {
                    Class cls = Class.forName(tokens.sval);
                    bench = (Benchmark) cls.newInstance();
                } catch (Exception e) {
                    throw new ConfigFormatException("unable to instantiate " +
                            "benchmark \"" + tokens.sval + "\" on line " +
                            tokens.lineno());
                }
                tokens.nextToken();
                return bench;
            default:
                throw new ConfigFormatException("missing benchmark class " +
                        "name on line " + tokens.lineno());
        }
    }
    String[] parseBenchArgs(StreamTokenizer tokens)
        throws IOException, ConfigFormatException
    {
        Vector vec = new Vector();
        for (;;) {
            switch (tokens.ttype) {
                case StreamTokenizer.TT_EOF:
                case StreamTokenizer.TT_EOL:
                    return (String[]) vec.toArray(new String[vec.size()]);
                case StreamTokenizer.TT_WORD:
                case '"':
                    vec.add(tokens.sval);
                    tokens.nextToken();
                    break;
                default:
                    throw new ConfigFormatException("unrecognized arg token " +
                            "on line " + tokens.lineno());
            }
        }
    }
    public void runBenchmarks(Reporter reporter, boolean verbose) {
        for (int i = 0; i < binfo.length; i++) {
            if (verbose)
                System.out.println("Running benchmark " + i + " (" +
                        binfo[i].getName() + ")");
            try {
                binfo[i].runBenchmark();
            } catch (Exception e) {
                System.err.println("Error: benchmark " + i + " failed: " + e);
                e.printStackTrace();
            }
            cleanup();
        }
        try {
            reporter.writeReport(binfo, System.getProperties());
        } catch (IOException e) {
            System.err.println("Error: failed to write benchmark report");
        }
    }
    protected void cleanup() {
        System.gc();
    }
}
