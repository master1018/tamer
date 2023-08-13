public class SearchPath {
    private String pathString;
    private String[] pathArray;
    public SearchPath(String searchPath) {
        StringTokenizer st = new StringTokenizer(searchPath, File.pathSeparator);
        List<String> dlist = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            dlist.add(st.nextToken());
        }
        pathString = searchPath;
        pathArray = dlist.toArray(new String[dlist.size()]);
    }
    public boolean isEmpty() {
        return (pathArray.length == 0);
    }
    public String asString() {
        return pathString;
    }
    public String[] asArray() {
        return pathArray.clone();
    }
    public File resolve(String relativeFileName) {
        for (String element : pathArray) {
            File path = new File(element, relativeFileName);
            if (path.exists()) {
                return path;
            }
        }
        return null;
    }
    public String[] children(String relativeDirName, FilenameFilter filter) {
        SortedSet<String> s = new TreeSet<String>();  
        for (String element : pathArray) {
            File path = new File(element, relativeDirName);
            if (path.exists()) {
                String[] childArray = path.list(filter);
                if (childArray != null) {
                    for (int j = 0; j < childArray.length; j++) {
                        if (!s.contains(childArray[j])) {
                            s.add(childArray[j]);
                        }
                    }
                }
            }
        }
        return s.toArray(new String[s.size()]);
    }
}
