abstract class V9BranchDecoder extends BranchDecoder
                       implements  V9InstructionDecoder {
    static boolean getPredictTaken(int instruction) {
        return (PREDICTION_MASK & instruction) != 0;
    }
}
