public class HTMLSeriesReporter {
    private static final int HTMLGEN_FILE_NEW = 1;
    private static final int HTMLGEN_FILE_UPDATE = 2;
    public static String resultsDir = ".";
    public static List groups = new ArrayList();
    public static Map groupNames = new HashMap();
    public static int LEVEL = 2;
    private static final DecimalFormat decimalFormat =
        new DecimalFormat("0.##");
    private static final SimpleDateFormat dateFormat =
        new SimpleDateFormat("EEE, MMM d, yyyy G 'at' HH:mm:ss z");
    static final Comparator numericComparator = new Comparator() {
            public int compare(Object lhs, Object rhs) {
                double lval = -1;
                try {
                    lval = Double.parseDouble((String)lhs);
                }
                catch (NumberFormatException pe) {
                }
                double rval = -1;
                try {
                    rval = Double.parseDouble((String)rhs);
                }
                catch (NumberFormatException pe) {
                }
                double delta = lval - rval;
                return delta == 0 ? 0 : delta < 0 ? -1 : 1;
            }
        };
    private static PrintWriter openFile(String name, int nSwitch) {
        FileOutputStream file = null;
        OutputStreamWriter writer = null;
        try {
            switch (nSwitch) {
                case 1: 
                    file = new FileOutputStream(name, false);
                    break;
                case 2: 
                    file = new FileOutputStream(name, true);
                    break;
            }
            writer = new OutputStreamWriter(file);
        } catch (IOException ee) {
            System.out.println("Error opening file: " + ee);
            System.exit(1);
        }
        return new PrintWriter(new BufferedWriter(writer));
    }
    private static void generateSeriesReport(String resultsDir, ArrayList xmlFileNames) {
        for (int i = 0; i < xmlFileNames.size(); ++i) {
            String xml = (String)xmlFileNames.get(i);
            try {
                J2DAnalyzer.readResults(xml);
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        File reportFile = new File(resultsDir, "series.html");
        PrintWriter w =
            openFile(reportFile.getAbsolutePath(), HTMLGEN_FILE_NEW);
        w.println("<html><body bgcolor='#ffffff'>");
        w.println("<hr size='1'/><center><h2>J2DBench Series</h2></center><hr size='1'/>");
        final SingleResultSetHolder[] results = new SingleResultSetHolder[J2DAnalyzer.results.size()];
        Set propKeys = new HashSet();
        for (int i = 0; i < results.length; ++i) {
            SingleResultSetHolder srsh = (SingleResultSetHolder)J2DAnalyzer.results.get(i);
            Map props = srsh.getProperties();
            Set keys = props.keySet();
            propKeys.addAll(keys);
            results[i] = srsh;
        }
        Map[] uniqueProps = new Map[results.length];
        Map commonProps = new HashMap();
        for (int i = 0; i < results.length; ++i) {
            Map m = new HashMap();
            m.putAll(results[i].getProperties());
            uniqueProps[i] = m;
        }
        {
            Iterator iter = propKeys.iterator();
            loop: while (iter.hasNext()) {
                Object k = iter.next();
                Object v = null;
                for (int i = 0; i < uniqueProps.length; ++i) {
                    Map props = uniqueProps[i];
                    if (i == 0) {
                        v = props.get(k);
                    } else {
                        Object mv = props.get(k);
                        if (!(v == null ? v == mv : v.equals(mv))) {
                            continue loop;
                        }
                    }
                }
                commonProps.put(k, v);
                for (int i = 0; i < uniqueProps.length; ++i) {
                    uniqueProps[i].remove(k);
                }
            }
        }
        String[] hexColor = {
            "#fc9505", "#fcd805", "#fc5c05", "#b5fc05", "1cfc05", "#05fc7a",
            "#44ff88", "#77ff77", "#aaff66", "#ddff55", "#ffff44", "#ffdd33",
        };
        Comparator comparator = new Comparator() {
                public int compare(Object lhs, Object rhs) {
                    return ((String)((Map.Entry)lhs).getKey()).compareTo((String)((Map.Entry)rhs).getKey());
                }
            };
        w.println("<br/>");
        w.println("<table align='center' cols='2' cellspacing='0' cellpadding='0' border='0' width='80%'>");
        w.println("<tr><th colspan='2' bgcolor='#aaaaaa'>Result Set Properties</th></tr>");
        for (int i = 0; i < results.length; ++i) {
            String titl = results[i].getTitle();
            String desc = results[i].getDescription();
            w.println("<tr bgcolor='" + hexColor[i%hexColor.length] + "'><th>"+titl+"</th><td>"+desc+"</td></tr>");
            TreeSet ts = new TreeSet(comparator);
            ts.addAll(uniqueProps[i].entrySet());
            Iterator iter = ts.iterator();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry)iter.next();
                w.println("<tr><td width='30%'><b>"+e.getKey()+"</b></td><td>"+e.getValue()+"</td></tr>");
            }
        }
        w.println("<tr><th colspan='2'>&nbsp;</th></tr>");
        w.println("<tr><th colspan='2' bgcolor='#aaaaaa'>Common Properties</th></tr>");
        {
            TreeSet ts = new TreeSet(comparator);
            ts.addAll(commonProps.entrySet());
            Iterator iter = ts.iterator();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry)iter.next();
                w.println("<tr><td width='30%'><b>"+e.getKey()+"</b></td><td>"+e.getValue()+"</td></tr>");
            }
        }
        w.println("<tr><th colspan='2'>&nbsp;</th></tr>");
        w.println("<tr><th colspan='2' bgcolor='#aaaaaa'>Common Test Options</th></tr>");
        {
            TreeSet ts = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            ts.addAll(ResultHolder.commonkeys.keySet());
            Iterator iter = ts.iterator();
            while (iter.hasNext()) {
                Object key = iter.next();
                Object val = ResultHolder.commonkeymap.get(key);
                w.println("<tr><td width='30%'><b>"+key+"</b></td><td>"+val+"</td></tr>");
            }
        }
        w.println("</table>");
        Map testRuns = new HashMap(); 
        Set testNames = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < results.length; ++i) {
            Enumeration en = results[i].getResultEnumeration();
            while (en.hasMoreElements()) {
                ResultHolder rh = (ResultHolder)en.nextElement();
                String name = rh.getName();
                testNames.add(name);
                ArrayList list = (ArrayList)testRuns.get(name);
                if (list == null) {
                    list = new ArrayList();
                    testRuns.put(name, list);
                }
                list.add(rh);
            }
        }
        w.println("<hr size='1' width='60%'/>");
        w.println("<br/>");
        w.println("<table align='center' cols='2' cellspacing='0' cellpadding='0' border='0' width='80%'>");
        Iterator iter = testNames.iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            w.println("<tr bgcolor='#aaaaaa'><th colspan='2'>"+name+"</th></tr>");
            double bestScore = 0;
            Map optionMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            ArrayList list = (ArrayList)testRuns.get(name);
            Iterator riter = list.iterator();
            while (riter.hasNext()) {
                ResultHolder rh = (ResultHolder)riter.next();
                Hashtable options = rh.getOptions();
                Set entries = options.entrySet();
                Iterator eiter = entries.iterator();
                while (eiter.hasNext()) {
                    Map.Entry e = (Map.Entry)eiter.next();
                    Object key = e.getKey();
                    if (ResultHolder.commonkeys.contains(key)) {
                        continue;
                    }
                    Object val = e.getValue();
                    Map vmap = (Map)optionMap.get(key);
                    if (vmap == null) {
                        boolean numeric = false;
                        try {
                            Integer.parseInt((String)val);
                            numeric = true;
                        }
                        catch (NumberFormatException pe) {
                        }
                        Comparator c = numeric ? numericComparator : String.CASE_INSENSITIVE_ORDER;
                        vmap = new TreeMap(c);
                        optionMap.put(key, vmap);
                    }
                    ArrayList vlist = (ArrayList)vmap.get(val);
                    if (vlist == null) {
                        vlist = new ArrayList();
                        vmap.put(val, vlist);
                    }
                    vlist.add(rh);
                    double score = rh.getScore();
                    if (score > bestScore) {
                        bestScore = score;
                    }
                }
            }
            Iterator oi = optionMap.keySet().iterator();
            while (oi.hasNext()) {
                String optionName = (String)oi.next();
                Map optionValues = (Map)optionMap.get(optionName);
                if (optionValues.size() == 1) continue; 
                StringBuffer grouping = new StringBuffer();
                grouping.append("Grouped by " + optionName + ", Result set");
                Iterator oi2 = optionMap.keySet().iterator();
                while (oi2.hasNext()) {
                    String oname2 = (String)oi2.next();
                    if (oname2.equals(optionName)) continue;
                    Map ov2 = (Map)optionMap.get(oname2);
                    if (ov2.size() == 1) continue;
                    grouping.append(", " + oname2);
                    Iterator ov2i = ov2.entrySet().iterator();
                    grouping.append(" (");
                    boolean comma = false;
                    while (ov2i.hasNext()) {
                        if (comma) grouping.append(", ");
                        grouping.append(((Map.Entry)ov2i.next()).getKey());
                        comma = true;
                    }
                    grouping.append(")");
                }
                w.println("<tr><td colspan='2'>&nbsp;</td></tr>");
                w.println("<tr><td colspan='2'><b>" + grouping.toString() + "</b></td></tr>");
                Iterator vi = optionValues.keySet().iterator();
                while (vi.hasNext()) {
                    String valueName = (String)vi.next();
                    w.print("<tr><td align='right' valign='center' width='10%'>"+valueName+"&nbsp;</td><td>");
                    ArrayList resultList = (ArrayList)optionValues.get(valueName);
                    Comparator c = new Comparator() {
                            public int compare(Object lhs, Object rhs) {
                                ResultSetHolder lh = ((ResultHolder)lhs).rsh;
                                ResultSetHolder rh = ((ResultHolder)rhs).rsh;
                                int li = -1;
                                for (int k = 0; k < results.length; ++k) {
                                    if (results[k] == lh) {
                                        li = k;
                                        break;
                                    }
                                }
                                int ri = -1;
                                for (int k = 0; k < results.length; ++k) {
                                    if (results[k] == rh) {
                                        ri = k;
                                        break;
                                    }
                                }
                                return li - ri;
                            }
                        };
                    w.println("   <div style='height: 5'>&nbsp;</div>");
                    ResultHolder[] sorted = new ResultHolder[resultList.size()];
                    sorted = (ResultHolder[])resultList.toArray(sorted);
                    Arrays.sort(sorted, c);
                    for (int k = 0; k < sorted.length; ++k) {
                        ResultHolder holder = sorted[k];
                        String color = null;
                        for (int n = 0; n < results.length; ++n) {
                            if (results[n] == holder.rsh) {
                                color = hexColor[n];
                            }
                        }
                        double score = holder.getScore();
                        int pix = 0;
                        if (bestScore > 1) {
                            double scale = logScale
                                ? Math.log(score)/Math.log(bestScore)
                                : (score)/(bestScore);
                            pix = (int)(scale*80.0);
                        }
                        w.println("   <div style='width: " + pix +
                                  "%; height: 15; font-size: smaller; valign: center; background-color: " +  color+"'>" +
                                  "<div align='right' style='height: 15'>" + (int)score + "&nbsp;</div></div>");
                    }
                    w.println("</td></tr>");
                }
            }
            w.println("<tr><td colspan='2'>&nbsp;</td></tr>");
        }
        w.println("</table>");
        w.println("<br/>");
        w.println("</body></html>");
        w.flush();
        w.close();
    }
    private static void printUsage() {
        String usage =
            "\njava HTMLSeriesReporter [options] resultfile...   "     +
            "                                     \n\n" +
            "where options include:                "     +
            "                                      \n"   +
            "    -r | -results <result directory>  "     +
            "directory to which reports are stored \n"   +
            "    -ls                               "     +
            "display using logarithmic scale       \n"   +
            "    -resultxml | -xml <xml file path> "     +
            "path to result XML                    \n"   +
            "    -group | -g  <level>              "     +
            "group-level for tests                 \n"   +
            "                                      "     +
            " [ 1 , 2 , 3 or 4 ]                   \n"   +
            "    -analyzermode | -am               "     +
            "mode to be used for finding score     \n"   +
            "                                      "     +
            " [ BEST , WORST , AVERAGE , MIDAVG ]  ";
        System.out.println(usage);
        System.exit(0);
    }
    static boolean logScale = false;
    public static void main(String args[]) {
        String resDir = ".";
        ArrayList results = new ArrayList();
        int group = 2;
        int analyzerMode = 4;
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-ls")) {
                    logScale = true;
                } else if (args[i].startsWith("-results") ||
                    args[i].startsWith("-r"))
                {
                    i++;
                    resDir = args[i];
                } else if (args[i].startsWith("-group") ||
                           args[i].startsWith("-g"))
                {
                    i++;
                    group = Integer.parseInt(args[i]);
                    System.out.println("Grouping Level for tests: " + group);
                } else if (args[i].startsWith("-analyzermode") ||
                           args[i].startsWith("-am"))
                {
                    i++;
                    String strAnalyzerMode = args[i];
                    if(strAnalyzerMode.equalsIgnoreCase("BEST")) {
                        analyzerMode = 0;
                    } else if (strAnalyzerMode.equalsIgnoreCase("WORST")) {
                        analyzerMode = 1;
                    } else if (strAnalyzerMode.equalsIgnoreCase("AVERAGE")) {
                        analyzerMode = 2;
                    } else if (strAnalyzerMode.equalsIgnoreCase("MIDAVG")) {
                        analyzerMode = 3;
                    } else {
                        printUsage();
                    }
                    System.out.println("Analyzer-Mode: " + analyzerMode);
                } else {
                    results.add(args[i]);
                }
            }
        }
        catch(Exception e) {
            printUsage();
        }
        if (resDir != null) {
            J2DAnalyzer.setMode(analyzerMode);
            HTMLSeriesReporter.generateSeriesReport(resDir, results);
        } else {
            printUsage();
        }
    }
}
