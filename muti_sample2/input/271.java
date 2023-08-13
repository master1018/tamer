public class test {
        public ChannelUpstreamHandler getChannelUpstreamHandler() {
            return new NettyRpcClientChannelUpstreamHandler();
        }
}
