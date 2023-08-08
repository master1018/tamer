public class WebsocketModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Channel.class).to(ChannelImpl.class);
        bind(ChannelHandler.class).asEagerSingleton();
        bind(SendFailException.class);
        bind(MessagePattern.class).to(MessagePatternJSON.class);
        bind(Member.class).to(WebSocketMember.class);
        bind(MemberHandler.class);
        bind(Event.class).to(WebsocketEvent.class);
    }
}
