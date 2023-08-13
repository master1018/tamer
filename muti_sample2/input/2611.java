public class test {
    final ChannelControlMaster getChannelControlMasterByPatch(PJSynthPatch patch) {
        return channelControlMasterMap.get(patch);
    }
}
