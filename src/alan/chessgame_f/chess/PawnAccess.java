package alan.chessgame_f.chess;

public interface PawnAccess {
	
	public void move();
	
	public void diMoveBuf();
	
	public void diMoveCancel();
	
	public boolean isDiMove();
	
	public boolean isMoved();

}
