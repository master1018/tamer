public class test {
	private void registerIoSessionNow(IoSessionImpl session) throws IOException {
		SocketChannel channel = session.getChannel();
		if(channel == null) {
			throw new Error("通道不能为空");
		}
		channel.configureBlocking(false);                
		if(session.getClass() == IoSessionImpl.class) {
			SelectionKey selectKey = channel.register(selector, SelectionKey.OP_READ, session);  
			session.setSelectionKey(selectKey);   
			this.onRegisterSession(session);
		}
		else if(session.getClass() == ClientIoSession.class) {  
			ClientIoSession clientIoSession = (ClientIoSession) session;
			SelectionKey selectKey = channel.register(selector, SelectionKey.OP_CONNECT, session);
			channel.connect(clientIoSession.getConnectAddress());
			session.setSelectionKey(selectKey);
		}
		session.lastAccessTime = System.currentTimeMillis();
		session.lastReadTime = System.currentTimeMillis();
		session.lastWriteTime = System.currentTimeMillis();
		session.getOwnerAcceptor().getTimer().schedule(session.getCheckOverTime(), 
				session.getOwnerAcceptor().getConfigure().getOverTimeIntervalCheckTime(), 
				session.getOwnerAcceptor().getConfigure().getOverTimeIntervalCheckTime());
	}
}
