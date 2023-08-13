public class CompilerThreadStat implements java.io.Serializable {
    private String name;
    private long taskCount;
    private long compileTime;
    private MethodInfo lastMethod;
    CompilerThreadStat(String name, long taskCount, long time, MethodInfo lastMethod) {
        this.name = name;
        this.taskCount = taskCount;
        this.compileTime = time;
        this.lastMethod = lastMethod;
    };
    public String getName() {
        return name;
    }
    public long getCompileTaskCount() {
        return taskCount;
    }
    public long getCompileTime() {
        return compileTime;
    }
    public MethodInfo getLastCompiledMethodInfo() {
        return lastMethod;
    }
    public String toString() {
        return getName() + " compileTasks = " + getCompileTaskCount()
            + " compileTime = " + getCompileTime();
    }
    private static final long serialVersionUID = 6992337162326171013L;
}
