public class ApplicationTool extends JPanel {
    private static final long serialVersionUID = 310966063293205714L;
    private ExecutionManager runtime;
    private TypeScript script;
    private static final String PROMPT = "Input:";
    public ApplicationTool(Environment env) {
        super(new BorderLayout());
        this.runtime = env.getExecutionManager();
        this.script = new TypeScript(PROMPT, false); 
        this.add(script);
        script.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runtime.sendLineToApplication(script.readln());
            }
        });
        runtime.addApplicationEchoListener(new TypeScriptOutputListener(script));
        runtime.addApplicationOutputListener(new TypeScriptOutputListener(script));
        runtime.addApplicationErrorListener(new TypeScriptOutputListener(script));
    }
}
