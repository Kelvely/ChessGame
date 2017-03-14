package alan.chessgame_f.chess;

import java.util.HashMap;
import java.util.Map;

import alan.chessgame_f.Side;

public final class PieceFactory {
	
	private final static Map<Side, Map<PieceType, Piece>> pieceStorage = new HashMap<>();
	
	/**
	 * Because first step of pawn can move 2 steps, it can't use static storage.
	 * @see createPiece(Side, PieceType)
	 */
	static {
		Map<PieceType, Piece> blackPieces = new HashMap<>();
		blackPieces.put(PieceType.KING, new King(Side.BLACK));
		blackPieces.put(PieceType.QUEEN, new Queen(Side.BLACK));
		blackPieces.put(PieceType.KNIGHT, new Knight(Side.BLACK));
		blackPieces.put(PieceType.ROOK, new Rook(Side.BLACK));
		blackPieces.put(PieceType.PAWN, new Pawn(Side.BLACK));
		blackPieces.put(PieceType.BISHOP, new Bishop(Side.BLACK));
		pieceStorage.put(Side.BLACK, blackPieces);
		
		Map<PieceType, Piece> whitePieces = new HashMap<>();
		whitePieces.put(PieceType.KING, new King(Side.WHITE));
		whitePieces.put(PieceType.QUEEN, new Queen(Side.WHITE));
		whitePieces.put(PieceType.KNIGHT, new Knight(Side.WHITE));
		whitePieces.put(PieceType.ROOK, new Rook(Side.WHITE));
		whitePieces.put(PieceType.PAWN, new Pawn(Side.WHITE));
		whitePieces.put(PieceType.BISHOP, new Bishop(Side.WHITE));
		pieceStorage.put(Side.WHITE, whitePieces);
	}
	
	public static Piece createPiece(Side side, PieceType type) {
		// Pawn has their status properties, thus need to be separately noted.
		if(type == PieceType.PAWN) return new Pawn(side);
		return pieceStorage.get(side).get(type);
	}
	
	private PieceFactory() {}

}
