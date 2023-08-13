public class test {
    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        e.getChannel().setReadable(true);
    }
}
