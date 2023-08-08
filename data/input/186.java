public class BetaPruneTest extends TestCase {
    @Test
    public void testAlphaBeta() {
        StraightLogic logic = new StraightLogic();
        Player xPlayer = PlayerFactory.createPlayerWithPly(PlayerFactory.AlphaBeta, Player.XMARK, 2);
        xPlayer.logic(logic);
        xPlayer.score(new BoardEvaluation());
        Player oPlayer = PlayerFactory.createPlayerWithPly(PlayerFactory.AlphaBeta, Player.OMARK, 2);
        oPlayer.logic(logic);
        oPlayer.score(new BoardEvaluation());
        TicTacToeBoard board = new TicTacToeBoard();
        TicTacToeState state = new TicTacToeState(board, logic);
        algs.model.gametree.AlphaBetaEvaluation ae = new algs.model.gametree.AlphaBetaEvaluation(2);
        IGameMove move = ae.bestMove(state, xPlayer, oPlayer);
        System.out.println("best move:" + move);
        assertEquals(1, ((PlaceMark) move).getColumn());
        assertEquals(1, ((PlaceMark) move).getRow());
        System.out.println(ae.numStates + " nodes expanded");
    }
}
