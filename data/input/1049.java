public class AdaptiveMusicCompanionFactory implements AgentFactory {
    @Requires
    private RascalliWSImpl rascalliWS;
    @Requires
    private Executor executor;
    @Requires
    private ConfigService configService;
    @Requires
    private RssManager rssManager;
    @Requires
    private AgentManager agentManager;
    @Requires
    private ChatBot chatBot;
    @Requires(filter = "(type=T-IPAM)")
    private InputProcessor ipTool;
    @Requires(filter = "(type=T-QA)")
    private Effector qaTool;
    @Requires(filter = "(type=T-NALQI)")
    private Effector nalqiTool;
    @Requires(filter = "(type=T-QuestionAnalysis)")
    private Effector questionAnalysisTool;
    @Requires(filter = "(type=T-TextRelevance)")
    private Effector textRelevanceTool;
    public Agent createAgent(User user, AgentConfiguration spec) {
        MBEAgentImpl agent = new MBEAgentImpl(user, spec, new AdaptiveMind(), ipTool, executor, configService, rascalliWS, rssManager, agentManager, getId());
        agent.enableJabber();
        agent.addComponent(qaTool);
        agent.addComponent(nalqiTool);
        agent.addComponent(questionAnalysisTool);
        agent.addComponent(textRelevanceTool);
        agent.addComponent(new ChatBotTool(chatBot));
        final MMGTool mmgTool = new MMGTool();
        agent.addComponent(mmgTool);
        mmgTool.setTemplateFile("templates/master.vm");
        agent.addComponent(new AffectiveDialogueComponent());
        return agent;
    }
    public String getDisplayName() {
        return "Adaptive Music Companion";
    }
    public String getId() {
        return "org.rascalli.mbe.adaptiveMusicCompanion.AdaptiveMusicCompanion-1.1";
    }
}
