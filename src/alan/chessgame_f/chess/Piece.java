package alan.chessgame_f.chess;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import alan.chessgame_f.Side;
import alan.chessgame_f.util.Bound;
import alan.chessgame_f.util.Coordinate2D;

public abstract class Piece {
	
	public final Side side;
	
	public Piece(Side side) {
		this.side = side;
	}
	
	public abstract Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB);
	
	public abstract boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest);
	
	public abstract PieceType getType();
	
	static int getMovability(Coordinate2D coord, Map<Coordinate2D, Piece> chessboard, Side side) {
		Piece piece = chessboard.get(coord);
		if(piece != null) {
			if(piece.side == side) {
				return 2;
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}
	
	static void getValidMovesByVec(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Bound bound, Side side, Set<Coordinate2D> coords,
			int dx, int dy){
		for(int x=coord.x, y=coord.y; ; x+=dx,y+=dy){
			Coordinate2D dest = new Coordinate2D(x, y);
			if(!bound.isIn(dest)) return;
			int movability = getMovability(dest, chessboard, side);
			if(movability < 2) {
				coords.add(dest);
				if(movability > 0) {
					break;
				}
			} else {
				break;
			}
		}
	}

}

final class King extends Piece {
	
	private final static Coordinate2D[] RELATIVE_KING_MOVES = {
			new Coordinate2D(0, 1),
			new Coordinate2D(1, 1),
			new Coordinate2D(1, 0),
			new Coordinate2D(1, -1),
			new Coordinate2D(0, -1),
			new Coordinate2D(-1, -1),
			new Coordinate2D(-1, 0),
			new Coordinate2D(-1, 1)
	};

	public King(Side side) {
		super(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		Bound bound = new Bound(boundA, boundB);
		Set<Coordinate2D> coords = new HashSet<>();
		
		for (Coordinate2D relativeDest : RELATIVE_KING_MOVES) {
			Coordinate2D absoluteDest = new Coordinate2D(coord.x + relativeDest.x, coord.y + relativeDest.y);
			if(!bound.isIn(absoluteDest)) continue;
			int movability = getMovability(absoluteDest, chessboard, side);
			if(movability < 2) {
				coords.add(absoluteDest);
			}
		}
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		int dx = dest.x - coord.x;
		int dy = dest.y - coord.y;
		if(Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dx) + Math.abs(dy) > 0) {
			int movability = getMovability(dest, chessboard, side);
			if(movability < 2) return true;
			else return false;
		} else return false;
	}

	@Override
	public PieceType getType() {
		return PieceType.KING;
	}
	
}

final class Queen extends Piece {
	
	private final Rook asRook;
	private final Bishop asBishop;

	public Queen(Side side) {
		super(side);
		asRook = new Rook(side);
		asBishop = new Bishop(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		Set<Coordinate2D> coords = new HashSet<>();
		coords.addAll(asRook.getValidMoves(chessboard, coord, boundA, boundB));
		coords.addAll(asBishop.getValidMoves(chessboard, coord, boundA, boundB));
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		return asRook.isMoveValid(chessboard, coord, dest) || asRook.isMoveValid(chessboard, coord, dest);
	}

	@Override
	public PieceType getType() {
		return PieceType.QUEEN;
	}
	
}

final class Knight extends Piece {
	
	private final static Coordinate2D[] RELATIVE_KNIGHT_MOVES = {
			new Coordinate2D(1, 2),
			new Coordinate2D(2, 1),
			new Coordinate2D(2, -1),
			new Coordinate2D(1, -2),
			new Coordinate2D(-1, -2),
			new Coordinate2D(-2, -1),
			new Coordinate2D(-2, 1),
			new Coordinate2D(-1, 2)
	};

	public Knight(Side side) {
		super(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		Bound bound = new Bound(boundA, boundB);
		Set<Coordinate2D> coords = new HashSet<>();
		
		for (Coordinate2D relativeDest : RELATIVE_KNIGHT_MOVES) {
			Coordinate2D absoluteDest = new Coordinate2D(coord.x + relativeDest.x, coord.y + relativeDest.y);
			if(!bound.isIn(absoluteDest)) continue;
			int movability = getMovability(absoluteDest, chessboard, side);
			if(movability < 2) {
				coords.add(absoluteDest);
			}
		}
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		int dx = dest.x - coord.x;
		int dy = dest.y - coord.y;
		if(Math.abs(Math.abs(dy) - Math.abs(dx)) == 1 && Math.abs(dy) + Math.abs(dx) == 3) {
			int movability = getMovability(dest, chessboard, side);
			if(movability < 2) return true;
			else return false;
		} else return false;
	}

	@Override
	public PieceType getType() {
		return PieceType.KNIGHT;
	}
	
}

final class Rook extends Piece {

	public Rook(Side side) {
		super(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		Bound bound = new Bound(boundA, boundB);
		Set<Coordinate2D> coords = new HashSet<>();
		
		for(int i=1+coord.x; bound.isXIn(i); i++) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability < 2) {
				coords.add(next);
				if(movability > 0) {
					break;
				}
			} else {
				break;
			}
			
		}
		
		for(int i=-1+coord.x; bound.isXIn(i); i--) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability < 2) {
				coords.add(next);
				if(movability > 0) {
					break;
				}
			} else {
				break;
			}
			
		}
		
		for(int i=1+coord.y; bound.isYIn(i); i++) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability < 2) {
				coords.add(next);
				if(movability > 0) {
					break;
				}
			} else {
				break;
			}
			
		}
		
		for(int i=-1+coord.y; bound.isYIn(i); i--) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability < 2) {
				coords.add(next);
				if(movability > 0) {
					break;
				}
			} else {
				break;
			}
			
		}
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		int dx = dest.x - coord.x;
		int dy = dest.y - coord.y;
		if((Math.abs(dx) > 0) != (Math.abs(dy) > 0)) {
			if(dx > 0) {
				int i=1;
				for(;i<dx;i++) {
					int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
					if(movability != 0) return false;
				}
				int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
				if(movability > 1) return false;
			} else if (dx < 0){
				int i=-1;
				for(;i>dx;i--) {
					int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
					if(movability != 0) return false;
				}
				int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
				if(movability > 1) return false;
			} else if (dy > 0) {
				int i=1;
				for(;i<dy;i++) {
					int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
					if(movability != 0) return false;
				}
				int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
				if(movability > 1) return false;
			} else {
				int i=-1;
				for(;i>dy;i--) {
					int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
					if(movability != 0) return false;
				}
				int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
				if(movability > 1) return false;
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public PieceType getType() {
		return PieceType.ROOK;
	}
	
}

final class Pawn extends Piece implements PawnAccess {
	
	private final static Bound NORMAL_PAWN_MOVES = new Bound(new Coordinate2D(-1, -1), new Coordinate2D(1, 1));
	private final static Bound FIRST_PAWN_MOVES = new Bound(new Coordinate2D(-2, -2), new Coordinate2D(2, 2));
	private final static Coordinate2D[] KILL_PAWN_MOVES = {
			new Coordinate2D(1, 1),
			new Coordinate2D(1, -1),
			new Coordinate2D(-1, -1),
			new Coordinate2D(-1, 1)
	};
	
	private boolean moved;
	private boolean diMoved;

	public Pawn(Side side) {
		super(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		
		Bound bound = new Bound(boundA, boundB);
		Set<Coordinate2D> coords = new HashSet<>();
		
		if(moved) {
			// normal
			bound = bound.and(NORMAL_PAWN_MOVES.add(coord));
		} else {
			// first
			bound = bound.and(FIRST_PAWN_MOVES.add(coord));
		}
		for(int i=1+coord.x; bound.isXIn(i); i++) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability == 0) {
				coords.add(next);
			} else {
				break;
			}
			
		}
		
		for(int i=-1+coord.x; bound.isXIn(i); i--) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability == 0) {
				coords.add(next);
			} else {
				break;
			}
			
		}
		
		for(int i=1+coord.y; bound.isYIn(i); i++) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability == 0) {
				coords.add(next);
			} else {
				break;
			}
			
		}
		
		for(int i=-1+coord.y; bound.isYIn(i); i--) {
			
			Coordinate2D next = new Coordinate2D(i, coord.y);
			int movability = getMovability(next, chessboard, side);
			if(movability == 0) {
				coords.add(next);
			} else {
				break;
			}
			
		}
		
		// Kills and en-passants
		for (Coordinate2D killingCoord : KILL_PAWN_MOVES) {
			Coordinate2D dest = coord.add(killingCoord);
			int movability = getMovability(dest, chessboard, side);
			if(movability == 1) {
				coords.add(dest);
			} else if(movability == 0) {
				Piece nextPiece = chessboard.get(new Coordinate2D(coord.x + killingCoord.x, coord.y));
				if(nextPiece instanceof PawnAccess) {
					PawnAccess pawnAccess = (PawnAccess) nextPiece;
					if(pawnAccess.isDiMove() && nextPiece.side != side) {
						coords.add(dest);
					} 
				} 
			}
		}
		
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		
		int dx = dest.x - coord.x;
		int dy = dest.y - coord.y;
		if(moved) {
			if(!NORMAL_PAWN_MOVES.isIn(new Coordinate2D(dx, dy))) return false;
		} else {
			if(!FIRST_PAWN_MOVES.isIn(new Coordinate2D(dx, dy))) return false;
		}
		boolean normalValid = true;
		if((Math.abs(dx) > 0) != (Math.abs(dy) > 0)) {
			if(dx > 0) {
				int i=1;
				for(;i<=dx;i++) {
					int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
					if(movability != 0) {
						normalValid = false;
						break;
					}
				}
			} else if (dx < 0){
				int i=-1;
				for(;i>=dx;i--) {
					int movability = getMovability(new Coordinate2D(coord.x + i, coord.y), chessboard, side);
					if(movability != 0) {
						normalValid = false;
						break;
					}
				}
			} else if (dy > 0) {
				int i=1;
				for(;i<=dy;i++) {
					int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
					if(movability != 0) {
						normalValid = false;
						break;
					}
				}
			} else {
				int i=-1;
				for(;i>=dy;i--) {
					int movability = getMovability(new Coordinate2D(coord.x, coord.y + i), chessboard, side);
					if(movability != 0) {
						normalValid = false;
						break;
					}
				}
			}
		} else {
			normalValid = false;
		}
		
		boolean killValid = false; //
		if(Math.abs(dx) - Math.abs(dy) == 0 && Math.abs(dx) + Math.abs(dy) == 2) {
			int movability = getMovability(dest, chessboard, side);
			if(movability == 1) {
				killValid = true;
			} else if(movability == 0) {
				Piece nextPiece = chessboard.get(new Coordinate2D(coord.x + dx, coord.y));
				if(nextPiece instanceof PawnAccess) {
					PawnAccess pawnAccess = (PawnAccess) nextPiece;
					if(pawnAccess.isDiMove() && nextPiece.side != side) {
						killValid = true;
					} 
				} 
			} 
		}
		
		return normalValid || killValid;
		
	}

	@Override
	public PieceType getType() {
		return PieceType.PAWN;
	}

	@Override
	public void move() {
		moved = true;
	}

	@Override
	public void diMoveBuf() {
		diMoved = true;
	}

	@Override
	public void diMoveCancel() {
		diMoved = false;
	}

	@Override
	public boolean isDiMove() {
		return diMoved;
	}

	@Override
	public boolean isMoved() {
		return moved;
	}
	
}

final class Bishop extends Piece {

	public Bishop(Side side) {
		super(side);
	}

	@Override
	public Set<Coordinate2D> getValidMoves(
			Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D boundA, Coordinate2D boundB) {
		Bound bound = new Bound(boundA, boundB);
		Set<Coordinate2D> coords = new HashSet<>();
		getValidMovesByVec(chessboard, coord, bound, side, coords, 1, 1);
		getValidMovesByVec(chessboard, coord, bound, side, coords, 1, -1);
		getValidMovesByVec(chessboard, coord, bound, side, coords, -1, -1);
		getValidMovesByVec(chessboard, coord, bound, side, coords, -1, 1);
		return coords;
	}

	@Override
	public boolean isMoveValid(Map<Coordinate2D, Piece> chessboard, Coordinate2D coord, Coordinate2D dest) {
		int dx = dest.x - coord.x;
		int dy = dest.y - coord.y;
		if(Math.abs(dx) != Math.abs(dy)) return false;
		int i=1;
		for(;i<=dx;i++) {
			int movability = getMovability(getRelativeCoord(dx, dy, coord, i), chessboard, side);
			if(movability != 0) return false;
		}
		int movability = getMovability(getRelativeCoord(dx, dy, coord, i), chessboard, side);
		if(movability > 1) return false;
		return true;
	}

	@Override
	public PieceType getType() {
		return PieceType.BISHOP;
	}
	
	private static Coordinate2D getRelativeCoord(int dx, int dy, Coordinate2D coord, int di) {
		int x, y;
		if(dx > 0) x = di;
		else x = -di;
		if(dy > 0) y = di;
		else y = -di;
		return new Coordinate2D(coord.x + x, coord.y + y);
	}
	
}