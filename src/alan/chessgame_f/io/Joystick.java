package alan.chessgame_f.io;

import alan.chessgame_f.chess.PieceType;
import alan.chessgame_f.util.Coordinate2D;

public interface Joystick {
	
	/**
	 * Move from A to B. <br>
	 * The move validity is checked.
	 * 
	 * @param src The original position of piece
	 * @param dest The destination of piece
	 */
	public void move(Coordinate2D src, Coordinate2D dest);
	
	/**
	 * Quit game.
	 */
	public void quit();
	
	/**
	 * Tell a message to the player
	 * 
	 * @param message The message that acquired to say
	 */
	public void message(String message);
	
	/**
	 * To make a pawn ascend decision
	 * 
	 * @param pieceType
	 */
	public void ascendTo(PieceType pieceType);
	
	/**
	 * To make a translocation decision
	 * 
	 * @param translocate Whether player decide to translocate or not.
	 */
	public void decideTranslocation(boolean translocate);

}
