class JDBToolBar extends JToolBar {
    Environment env;
    ExecutionManager runtime;
    ClassManager classManager;
    SourceManager sourceManager;
    CommandInterpreter interpreter;
    JDBToolBar(Environment env) {
        this.env = env;
        this.runtime = env.getExecutionManager();
        this.classManager = env.getClassManager();
        this.sourceManager = env.getSourceManager();
        this.interpreter = new CommandInterpreter(env, true);
        addTool("Run application", "run", "run");
        addTool("Connect to application", "connect", "connect");
        addSeparator();
        addTool("Step into next line", "step", "step");
        addTool("Step over next line", "next", "next");
        addTool("Step out of current method call", "step up", "step up");
        addSeparator();
        addTool("Suspend execution", "interrupt", "interrupt");
        addTool("Continue execution", "cont", "cont");
        addSeparator();
        addTool("Move up one stack frame", "up", "up");
        addTool("Move down one stack frame", "down", "down");
    }
    private void addTool(String toolTip, String labelText, String command) {
        JButton button = new JButton(labelText);
        button.setToolTipText(toolTip);
        final String cmd = command;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.executeCommand(cmd);
            }
        });
        this.add(button);
    }
}
