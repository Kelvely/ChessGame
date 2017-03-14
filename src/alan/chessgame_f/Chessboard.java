package alan.chessgame_f;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import alan.chessgame_f.chess.Piece;
import alan.chessgame_f.chess.PieceFactory;
import alan.chessgame_f.chess.PieceType;
import alan.chessgame_f.util.Coordinate2D;

public final class Chessboard {
	
	private final Map<Coordinate2D, Piece> grid = new HashMap<>();
	private final ReadWriteLock gridRWL = new ReentrantReadWriteLock();
	
	/**
	 * The grid is cloned, don't worry :P
	 * @return The cloned grid
	 */
	public Map<Coordinate2D, Piece> getGrid(){
		Map<Coordinate2D, Piece> grid = new HashMap<>();
		gridRWL.readLock().lock();
		for (Entry<Coordinate2D, Piece> entry : Chessboard.this.grid.entrySet()) {
			grid.put(entry.getKey(), entry.getValue());
		}
		gridRWL.readLock().unlock();
		return grid;
	}
	
	public void move(Coordinate2D src, Coordinate2D dest) {
		gridRWL.writeLock().lock();
		Piece piece = grid.remove(src);
		grid.put(dest, piece);
		gridRWL.writeLock().unlock();
	}
	
	/**
	 * Pronounce as "switch"
	 */
	public void switr(Coordinate2D coordA, Coordinate2D coordB){
		gridRWL.writeLock().lock();
		Piece pieceA = grid.get(coordA);
		Piece pieceB = grid.get(coordA);
		grid.put(coordA, pieceB);
		grid.put(coordB, pieceA);
		gridRWL.writeLock().unlock();
	}
	
	public void ascend(Coordinate2D coord, Piece piece) {
		gridRWL.writeLock().lock();
		grid.put(coord, piece);
		gridRWL.writeLock().unlock();
	}
	
	public void passant(Coordinate2D coord) {
		gridRWL.writeLock().lock();
		grid.remove(coord);
		gridRWL.writeLock().unlock();
	}
	
	public static Piece[][] toArray(Map<Coordinate2D, Piece> grid){
		Piece[][] pieces = new Piece[8][8];
		for (Entry<Coordinate2D, Piece> entry : grid.entrySet()) {
			Coordinate2D coord = entry.getKey();
			if(coord.x < 8 && coord.x >= 0 && coord.y < 8 && coord.y >= 0) {
				pieces[coord.x][coord.y] = entry.getValue();
			}
		}
		return pieces;
	}
	
	public static void standardStartup(Chessboard chessboard){
		Map<Coordinate2D, Piece> grid = chessboard.grid;
		chessboard.gridRWL.writeLock().lock();
		//grid.put(new Coordinate2D(2, 2), PieceFactory.createPiece(Side.BLACK, PieceType.BISHOP)); // Example
		
		// White Pawns
		for(int i=0;i<8;i++) {
			grid.put(new Coordinate2D(i, 1), PieceFactory.createPiece(Side.WHITE, PieceType.PAWN));
		}
		
		// White Rooks
		grid.put(new Coordinate2D(0, 0), PieceFactory.createPiece(Side.WHITE, PieceType.ROOK));
		grid.put(new Coordinate2D(7, 0), PieceFactory.createPiece(Side.WHITE, PieceType.ROOK));
		
		// White Knights
		grid.put(new Coordinate2D(1, 0), PieceFactory.createPiece(Side.WHITE, PieceType.KNIGHT));
		grid.put(new Coordinate2D(6, 0), PieceFactory.createPiece(Side.WHITE, PieceType.KNIGHT));
		
		// White Bishops
		grid.put(new Coordinate2D(2, 0), PieceFactory.createPiece(Side.WHITE, PieceType.BISHOP));
		grid.put(new Coordinate2D(5, 0), PieceFactory.createPiece(Side.WHITE, PieceType.BISHOP));
		
		// White King and queen
		grid.put(new Coordinate2D(3, 0), PieceFactory.createPiece(Side.WHITE, PieceType.QUEEN));
		grid.put(new Coordinate2D(4, 0), PieceFactory.createPiece(Side.WHITE, PieceType.KING));
		
		// Black Pawns
		for(int i=0;i<8;i++) {
			grid.put(new Coordinate2D(i, 6), PieceFactory.createPiece(Side.BLACK, PieceType.PAWN));
		}
		
		// Black Rooks
		grid.put(new Coordinate2D(0, 7), PieceFactory.createPiece(Side.BLACK, PieceType.ROOK));
		grid.put(new Coordinate2D(7, 7), PieceFactory.createPiece(Side.BLACK, PieceType.ROOK));
		
		// Black Knights
		grid.put(new Coordinate2D(1, 7), PieceFactory.createPiece(Side.BLACK, PieceType.KNIGHT));
		grid.put(new Coordinate2D(6, 7), PieceFactory.createPiece(Side.BLACK, PieceType.KNIGHT));
		
		// Black Bishops
		grid.put(new Coordinate2D(2, 7), PieceFactory.createPiece(Side.BLACK, PieceType.BISHOP));
		grid.put(new Coordinate2D(5, 7), PieceFactory.createPiece(Side.BLACK, PieceType.BISHOP));
		
		// Black King and queen
		grid.put(new Coordinate2D(3, 7), PieceFactory.createPiece(Side.BLACK, PieceType.QUEEN));
		grid.put(new Coordinate2D(4, 7), PieceFactory.createPiece(Side.BLACK, PieceType.KING));
		
		// TODO
		chessboard.gridRWL.writeLock().unlock();
	}

}
