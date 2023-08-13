public class AndroidNature implements IProjectNature {
    private IProject mProject;
    public void configure() throws CoreException {
        configureResourceManagerBuilder(mProject);
        configurePreBuilder(mProject);
        configureApkBuilder(mProject);
    }
    public void deconfigure() throws CoreException {
        removeBuilder(mProject, ResourceManagerBuilder.ID);
        removeBuilder(mProject, PreCompilerBuilder.ID);
        removeBuilder(mProject, ApkBuilder.ID);
    }
    public IProject getProject() {
        return mProject;
    }
    public void setProject(IProject project) {
        mProject = project;
    }
    public static synchronized void setupProjectNatures(IProject project,
            IProgressMonitor monitor) throws CoreException {
        if (project == null || !project.isOpen()) return;
        if (monitor == null) monitor = new NullProgressMonitor();
        addNatureToProjectDescription(project, JavaCore.NATURE_ID, monitor);
        addNatureToProjectDescription(project, AndroidConstants.NATURE, monitor);
    }
    private static void addNatureToProjectDescription(IProject project,
            String natureId, IProgressMonitor monitor) throws CoreException {
        if (!project.hasNature(natureId)) {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            String[] newNatures = new String[natures.length + 1];
            if (natureId.equals(AndroidConstants.NATURE)) {
                System.arraycopy(natures, 0, newNatures, 1, natures.length);
                newNatures[0] = natureId;
            } else {
                System.arraycopy(natures, 0, newNatures, 0, natures.length);
                newNatures[natures.length] = natureId;
            }
            description.setNatureIds(newNatures);
            project.setDescription(description, new SubProgressMonitor(monitor, 10));
        }
    }
    public static void configureResourceManagerBuilder(IProject project)
            throws CoreException {
        IProjectDescription desc = project.getDescription();
        ICommand[] commands = desc.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (ResourceManagerBuilder.ID.equals(commands[i].getBuilderName())) {
                return;
            }
        }
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 1, commands.length);
        ICommand command = desc.newCommand();
        command.setBuilderName(ResourceManagerBuilder.ID);
        newCommands[0] = command;
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }
    public static void configurePreBuilder(IProject project)
            throws CoreException {
        IProjectDescription desc = project.getDescription();
        ICommand[] commands = desc.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (PreCompilerBuilder.ID.equals(commands[i].getBuilderName())) {
                return;
            }
        }
        int index = -1;
        for (int i = 0; i < commands.length; ++i) {
            if (ResourceManagerBuilder.ID.equals(commands[i].getBuilderName())) {
                index = i;
                break;
            }
        }
        index++;
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, index);
        ICommand command = desc.newCommand();
        command.setBuilderName(PreCompilerBuilder.ID);
        newCommands[index] = command;
        System.arraycopy(commands, index, newCommands, index + 1, commands.length-index);
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }
    public static void configureApkBuilder(IProject project)
            throws CoreException {
        IProjectDescription desc = project.getDescription();
        ICommand[] commands = desc.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (ApkBuilder.ID.equals(commands[i].getBuilderName())) {
                return;
            }
        }
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        ICommand command = desc.newCommand();
        command.setBuilderName(ApkBuilder.ID);
        newCommands[commands.length] = command;
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }
    public static boolean removeBuilder(IProject project, String id) throws CoreException {
        IProjectDescription description = project.getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (id.equals(commands[i].getBuilderName())) {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                project.setDescription(description, null);
                return true;
            }
        }
        return false;
    }
}
