public class AionPacketHandler {
    private static final Logger log = Logger.getLogger(AionPacketHandler.class);
    public static AionClientPacket handle(ByteBuffer data, AionConnection client) {
        AionClientPacket msg = null;
        State state = client.getState();
        int id = data.get() & 0xff;
        switch(state) {
            case CONNECTED:
                {
                    switch(id) {
                        case 0x07:
                            msg = new CM_AUTH_GG(data, client);
                            break;
                        case 0x08:
                            msg = new CM_UPDATE_SESSION(data, client);
                            break;
                        default:
                            unknownPacket(state, id);
                    }
                    break;
                }
            case AUTHED_GG:
                {
                    switch(id) {
                        case 0x0B:
                            msg = new CM_LOGIN(data, client);
                            break;
                        default:
                            unknownPacket(state, id);
                    }
                    break;
                }
            case AUTHED_LOGIN:
                {
                    switch(id) {
                        case 0x05:
                            msg = new CM_SERVER_LIST(data, client);
                            break;
                        case 0x02:
                            msg = new CM_PLAY(data, client);
                            break;
                        default:
                            unknownPacket(state, id);
                    }
                    break;
                }
        }
        return msg;
    }
    private static void unknownPacket(State state, int id) {
        log.warn(String.format("Se ha recibido un paquete desconocido del cliente de Aion: 0x%02X Estado=%s", id, state.toString()));
    }
}
