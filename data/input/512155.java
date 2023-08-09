final class emmaTask extends SuppressableTask
{
    public emmaTask ()
    {
        m_tasks = new ArrayList ();
    }
    public synchronized void execute () throws BuildException
    {
        log (IAppConstants.APP_VERBOSE_BUILD_ID, Project.MSG_VERBOSE);
        if (isEnabled ())
        {
            while (! m_tasks.isEmpty ())
            {
                final NestedTask task = (NestedTask) m_tasks.remove (0);
                final String name = getTaskName ();
                try
                {
                    setTaskName (task.getTaskName ());
                    task.execute ();
                }
                finally
                {
                    setTaskName (name);
                }
            }
        }
    }
    public NestedTask createInstr ()
    {
        return addTask (new instrTask (this), getNestedTaskName ("instr"));
    }
    public NestedTask createMerge ()
    {
        return addTask (new mergeTask (this), getNestedTaskName ("merge"));
    }
    public NestedTask createReport ()
    {
        return addTask (new reportTask (this), getNestedTaskName ("report"));
    }
    protected NestedTask addTask (final NestedTask task, final String pseudoName)
    {
        initTask (task, pseudoName);
        m_tasks.add (task);
        return task;
    }
    protected void initTask (final NestedTask task, final String pseudoName)
    {
        task.setTaskName (pseudoName);
        task.setProject (getProject ());
        task.setLocation (getLocation ());
        task.setOwningTarget (getOwningTarget ());
        task.init ();
    }
    protected String getNestedTaskName (final String subname)
    {
        return getTaskName ().concat (".").concat (subname);
    }
    private final List  m_tasks;
} 
