public class JDiffAntTask {
    public void execute() throws BuildException {
	jdiffHome = project.getProperty("JDIFF_HOME");
	if (jdiffHome == null || jdiffHome.compareTo("") == 0 | 
	    jdiffHome.compareTo("(not set)") == 0) {
	    throw new BuildException("Error: invalid JDIFF_HOME property. Set it in the build file to the directory where jdiff is installed");
	}
        project.log(" JDiff home: " + jdiffHome, Project.MSG_INFO);
	jdiffClassPath = jdiffHome + DIR_SEP + "jdiff.jar" +
	    System.getProperty("path.separator") +
	    jdiffHome + DIR_SEP + "xerces.jar";
        if (!destdir.mkdir() && !destdir.exists()) {
	    throw new BuildException(getDestdir() + " is not a valid directory");
	} else {
	    project.log(" Report location: " + getDestdir() + DIR_SEP 
			+ "changes.html", Project.MSG_INFO);
	}
	if (oldProject == null || newProject == null) {
	    throw new BuildException("Error: two projects are needed, one <old> and one <new>");
	}
	generateJavadoc(oldProject);
	generateJavadoc(newProject);
	generateXML(oldProject);
	generateXML(newProject);
	compareXML(oldProject.getName(), newProject.getName());
	project.log(" Report location: " + getDestdir() + DIR_SEP 
		    + "changes.html", Project.MSG_INFO);
    }
    protected void generateXML(ProjectInfo proj) {
	String apiname = proj.getName();
	Javadoc jd = initJavadoc("Analyzing " + apiname);
	jd.setDestdir(getDestdir());
	addSourcePaths(jd, proj);
	jd.setPackagenames(getPackageList(proj));
	DocletInfo dInfo = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	jd.setDocletPath(new Path(project, jdiffClassPath));
	DocletParam dp1 = dInfo.createParam();
	dp1.setName("-apiname");
	dp1.setValue(apiname);
	DocletParam dp2 = dInfo.createParam();
	dp2.setName("-baseURI");
	dp2.setValue("http:
	DocletParam dp3 = dInfo.createParam();
	dp3.setName("-apidir");
	dp3.setValue(getDestdir().toString());
	jd.perform();
    }
    protected void compareXML(String oldapiname, String newapiname) {
	Javadoc jd = initJavadoc("Comparing versions");
	jd.setDestdir(getDestdir());
	jd.setPrivate(true);
	jd.setSourcefiles(jdiffHome + DIR_SEP + "Null.java");
	DocletInfo dInfo = jd.createDoclet();
	jd.setDoclet("jdiff.JDiff");
	jd.setDocletPath(new Path(project, jdiffClassPath));
	DocletParam dp1 = dInfo.createParam();
	dp1.setName("-oldapi");
	dp1.setValue(oldapiname);
	DocletParam dp2 = dInfo.createParam();
	dp2.setName("-newapi");
	dp2.setValue(newapiname);
	DocletParam dp3 = dInfo.createParam();
	dp3.setName("-oldapidir");
	dp3.setValue(getDestdir().toString());
	DocletParam dp4 = dInfo.createParam();
	dp4.setName("-newapidir");
	dp4.setValue(getDestdir().toString());
	DocletParam dp5 = dInfo.createParam();
	dp5.setName("-javadocold");
	dp5.setValue(".." + DIR_SEP + oldapiname + DIR_SEP);
	DocletParam dp6 = dInfo.createParam();
	dp6.setName("-javadocnew");
	dp6.setValue(".." + DIR_SEP + newapiname + DIR_SEP);
	if (getStats()) {
	    dInfo.createParam().setName("-stats");
	    copyFile(jdiffHome + DIR_SEP + "black.gif",
		     getDestdir().toString() + DIR_SEP + "black.gif");
	    copyFile(jdiffHome + DIR_SEP + "background.gif",
		     getDestdir().toString() + DIR_SEP + "background.gif");
	}
	if (getDocchanges()) {
	    dInfo.createParam().setName("-docchanges");
	}
	jd.perform();
    }
    protected void generateJavadoc(ProjectInfo proj) {	
	String javadoc = proj.getJavadoc();
	if (javadoc != null && javadoc.compareTo("generated") != 0) {
	    project.log("Configured to use existing Javadoc located in " +  
			javadoc, Project.MSG_INFO);
	    return;
	}
	String apiname = proj.getName();
	Javadoc jd = initJavadoc("Javadoc for " + apiname);
	jd.setDestdir(new File(getDestdir().toString() + DIR_SEP + apiname));
	addSourcePaths(jd, proj);
	jd.setPrivate(true);
	jd.setPackagenames(getPackageList(proj));
	jd.perform();
    }
    protected Javadoc initJavadoc(String logMsg) {
	Javadoc jd = new Javadoc();
	jd.setProject(project); 
	jd.setTaskName(logMsg);
	jd.setSource(getSource()); 
	jd.init();
	if (verboseAnt) {
	    jd.setVerbose(true);
	}
	return jd;
    }
    protected void addSourcePaths(Javadoc jd, ProjectInfo proj) {
	Vector dirSets = proj.getDirsets();
	int numDirSets = dirSets.size();
	for (int i = 0; i < numDirSets; i++) {
	    DirSet dirSet = (DirSet)dirSets.elementAt(i);
	    jd.setSourcepath(new Path(project, dirSet.getDir(project).toString()));
	}
    }
    protected String getPackageList(ProjectInfo proj) throws BuildException {
	String packageList = ""; 
	java.lang.StringBuffer sb = new StringBuffer();
	Vector dirSets = proj.getDirsets();
	int numDirSets = dirSets.size();
	boolean addComma = false;
	for (int i = 0; i < numDirSets; i++) {
	    DirSet dirSet = (DirSet)dirSets.elementAt(i);
	    DirectoryScanner dirScanner = dirSet.getDirectoryScanner(project);
	    String[] files = dirScanner.getIncludedDirectories();
	    for (int j = 0; j < files.length; j++) {
		if (!addComma){
		    addComma = true;
		} else {
		    sb.append(",");
		}
		sb.append(files[j]);
	    }
	}
	packageList = sb.toString();
	if (packageList.compareTo("") == 0) {
	    throw new BuildException("Error: no packages found to scan");
	}
	project.log(" Package list: " + packageList, Project.MSG_INFO);
	return packageList;
    }
    protected void copyFile(String src, String dst){
	File srcFile = new File(src);
	File dstFile = new File(dst);
	try {
	    File reportSubdir = new File(getDestdir().toString() + 
					 DIR_SEP + "changes");
	    if (!reportSubdir.mkdir() && !reportSubdir.exists()) {
		project.log("Warning: unable to create " + reportSubdir,
			    Project.MSG_WARN);
	    }
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	} catch (java.io.FileNotFoundException fnfe) {
	    project.log("Warning: unable to copy " + src.toString() + 
			" to " + dst.toString(), Project.MSG_WARN);
	} catch (java.io.IOException ioe) {
	    project.log("Warning: unable to copy " + src.toString() + 
			" to " + dst.toString(), Project.MSG_WARN);
	}
    }
    private Project project;
    public void setProject(Project proj) {
        project = proj;
    }
    static String DIR_SEP = System.getProperty("file.separator");
    private String jdiffHome = "(not set)";
    private String jdiffClassPath = "(not set)";
    private File destdir = new File("jdiff_report");
    public void setDestdir(File value) {
	this.destdir = value;
    }
    public File getDestdir() {
	return this.destdir;
    }
    private boolean verbose = false;
    public void setVerbose(boolean value) {
	this.verbose = value;
    }
    public boolean getVerbose() {
	return this.verbose;
    }
    private boolean verboseAnt = false;
    private boolean docchanges = false;
    public void setDocchanges(boolean value) {
	this.docchanges = value;
    }
    public boolean getDocchanges() {
	return this.docchanges;
    }
    private boolean stats = false;
    public void setStats(boolean value) {
	this.stats = value;
    }
    public boolean getStats() {
	return this.stats;
    }
    private String source = "1.5"; 
    public void setSource(String source) {
        this.source = source;
    }
    public String getSource() {
        return source;
    }
    private ProjectInfo oldProject = null;
    public void addConfiguredOld(ProjectInfo projInfo) {
	oldProject = projInfo;
    }
    private ProjectInfo newProject = null;
    public void addConfiguredNew(ProjectInfo projInfo) {
	newProject = projInfo;
    }
    public static class ProjectInfo {
	private String name;
	public void setName(String value) {
	    name = value;
	}
	public String getName() {
	    return name;
	}
	private String javadoc;
	public void setJavadoc(String value) {
	    javadoc = value;
	}
	public String getJavadoc() {
	    return javadoc;
	}
	private Vector dirsets = new Vector();
	public void setDirset(DirSet value) {
	    dirsets.add(value);
	}
	public Vector getDirsets() {
	    return dirsets;
	}
	public void addDirset(DirSet aDirset) {
	    setDirset(aDirset);
	}
    }
}
