@RunWith(JMock.class)
public class LoginFailureMessageProcessorTest {
    Mockery context = new JUnit4Mockery();
    @Test
    public void testProcessMessage() {
        final ConnectionStateListener stateListener = context.mock(ConnectionStateListener.class);
        context.checking(new Expectations() {
            {
                one(stateListener).loginFailed();
            }
        });
        LoginFailureMessage message = new LoginFailureMessage();
        message.setReason(1L);
        LoginFailureMessageProcessor processor = new LoginFailureMessageProcessor(stateListener);
        processor.processMessage(message);
    }
}
