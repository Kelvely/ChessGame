package alan.chessgame_f.io;

import java.util.Map;

import alan.chessgame_f.Side;
import alan.chessgame_f.chess.Piece;
import alan.chessgame_f.util.Coordinate2D;

public interface Visualizer {
	
	/**
	 * Including Null piece and Wrong move.
	 * 
	 * @param coord The piece coordinate that acquired to move.
	 * @param dest The piece coordinate that acquired to be moved to.
	 * @param piece The piece that is going to be moved.
	 */
	public void onInvalidMove(Coordinate2D coord, Coordinate2D dest, Piece piece);
	
	/**
	 * When piece is moved.
	 * 
	 * @param chessboard The chess-board after move.
	 */
	public void onChessboardUpdate(Map<Coordinate2D, Piece> chessboard);
	
	/**
	 * When it changes to one's turn.
	 */
	public void onTurnChanges(Side side);
	
	/**
	 * When the game is over, as one side wins the game or quit the game.
	 * 
	 * @param side The side that win the game.
	 */
	public void onGameover(Side side);
	
	/**
	 * When the competitor send a message.
	 * @param message The message
	 */
	public void onMessage(String message);
	
	/**
	 * When a pawn is going to ascend. The system requires to make a decision by joy-stick.
	 * 
	 * @param coord The piece(well, I meant pawn :D) that is going to ascend.
	 */
	public void onAscend(Coordinate2D coord);
	
	/**
	 * When a rook can make a translocation for the king.
	 * 
	 * @param coord Coordinate of king
	 */
	public void onCanTranslocation(Coordinate2D coord);

}
