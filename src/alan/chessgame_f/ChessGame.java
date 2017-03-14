package alan.chessgame_f;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import alan.chessgame_f.chess.PawnAccess;
import alan.chessgame_f.chess.Piece;
import alan.chessgame_f.chess.PieceFactory;
import alan.chessgame_f.chess.PieceType;
import alan.chessgame_f.io.Joystick;
import alan.chessgame_f.io.Visualizer;
import alan.chessgame_f.util.Coordinate2D;

public final class ChessGame implements Runnable {
	
	public final Joystick blackJoystick = new JoystickImpl(this, Side.BLACK);
	public final Joystick whiteJoystick = new JoystickImpl(this, Side.WHITE);
	
	private Visualizer blackVisualizer;
	private ReadWriteLock blackVisualizerRWL = new ReentrantReadWriteLock();
	private Visualizer whiteVisualizer;
	private ReadWriteLock whiteVisualizerRWL = new ReentrantReadWriteLock();
	
	private Side turn;
	private boolean gameover;
	
	private Chessboard chessboard;
	private Map<Coordinate2D, Piece> chessboardSnapshot;
	
	private boolean running;
	private final Lock runningInquireLock = new ReentrantLock();
	
	private final Lock chessWaitLock = new ReentrantLock();
	private boolean waiting;
	private final Condition chessWaitCondition = chessWaitLock.newCondition();
	
	
	private PieceType ascendCache;
	private final Lock ascendWaitLock = new ReentrantLock();
	private final Condition ascendWaitCondition = ascendWaitLock.newCondition();
	
	
	private boolean blackTranslocated;
	private boolean whiteTranslocated;
	
	private boolean translocationCache;
	private final Lock translocationInquireLock = new ReentrantLock();
	private final Condition translocationInquireCondition = translocationInquireLock.newCondition();
	
	private Coordinate2D diStepPawn;
	
	@Override
	public void run() {
		runningInquireLock.lock();
		if(running) {
			runningInquireLock.unlock();
			throw new IllegalStateException("The chess game is still running!");
		} else {
			running = true;
			runningInquireLock.unlock();
		}
		
		blackTranslocated = false;
		whiteTranslocated = false;
		
		diStepPawn = null;
		
		turn = Side.WHITE;
		gameover = false;
		
		chessboard = new Chessboard();
		
		Chessboard.standardStartup(chessboard);
		
		updateChessboard();
		
		updateTurn();
		
		for (;;) {
			
			chessboardSnapshot = chessboard.getGrid();
			if(gameover) {
				updateGameover();
				break;
			}
			chessWaitLock.lock();
			try {
				waiting = true;
				chessWaitCondition.await();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				waiting = false;
				chessWaitLock.unlock();
			}
			
			updateChessboard();
			
			if(gameover) {
				updateGameover();
				break;
			}
			
			turn = invertSide(turn);
			updateTurn();
		}
		
		runningInquireLock.lock();
		running = false;
		runningInquireLock.unlock();
	}
	
	public void setVisualizer(Visualizer visualizer, Side side) {
		if(side == Side.BLACK) {
			blackVisualizerRWL.writeLock().lock();
			blackVisualizer = visualizer;
			blackVisualizerRWL.writeLock().unlock();
		} else if(side == Side.WHITE) {
			whiteVisualizerRWL.writeLock().lock();
			whiteVisualizer = visualizer;
			whiteVisualizerRWL.writeLock().unlock();
		} else throw new NullPointerException("Unknown side: " + side.name());
	}
	
	public Map<Coordinate2D, Piece> getChessboardGrid(){
		return chessboard.getGrid();
	}
	
	public boolean isRunning(){
		return running;
	}
	
	public Side getTurn() {
		return turn;
	}
	
	// V
	private void updateChessboard() {
		blackVisualizerRWL.readLock().lock();
		if(blackVisualizer != null) blackVisualizer.onChessboardUpdate(chessboard.getGrid());
		blackVisualizerRWL.readLock().unlock();
		whiteVisualizerRWL.readLock().lock();
		if(whiteVisualizer != null) whiteVisualizer.onChessboardUpdate(chessboard.getGrid());
		whiteVisualizerRWL.readLock().unlock();
	}
	
	// V
	private void updateTurn() {
		blackVisualizerRWL.readLock().lock();
		if(blackVisualizer != null) blackVisualizer.onTurnChanges(turn);
		blackVisualizerRWL.readLock().unlock();
		whiteVisualizerRWL.readLock().lock();
		if(whiteVisualizer != null) whiteVisualizer.onTurnChanges(turn);
		whiteVisualizerRWL.readLock().unlock();
	}
	
	// V
	private void updateGameover() {
		blackVisualizerRWL.readLock().lock();
		if(blackVisualizer != null) blackVisualizer.onGameover(turn);
		blackVisualizerRWL.readLock().unlock();
		whiteVisualizerRWL.readLock().lock();
		if(whiteVisualizer != null) whiteVisualizer.onGameover(turn);
		whiteVisualizerRWL.readLock().unlock();
	}
	
	// V
	private void notifyInvalidMove(Coordinate2D coord, Coordinate2D dest, Piece piece, Side side) {
		if(side == Side.BLACK) {
			blackVisualizerRWL.readLock().lock();
			blackVisualizer.onInvalidMove(coord, dest, piece);
			blackVisualizerRWL.readLock().unlock();
		} else if(side == Side.WHITE) {
			whiteVisualizerRWL.readLock().lock();
			whiteVisualizer.onInvalidMove(coord, dest, piece);
			whiteVisualizerRWL.readLock().unlock();
		} else throw new NullPointerException("Unknown side: " + side.name());
	}
	
	private static Side invertSide(Side side) {
		if(side == Side.BLACK) {
			return Side.WHITE;
		} else if(side == Side.WHITE) {
			return Side.BLACK;
		} else throw new NullPointerException("Unknown side: " + side.name());
	}
	
	// J
	private void move(Coordinate2D src, Coordinate2D dest, Side side) {
		chessWaitLock.lock();
		if(!waiting) {
			chessWaitLock.unlock();
			return;
		}
		if(side != turn) {
			chessWaitLock.unlock();
			return;
		}
		Piece srcPiece = chessboardSnapshot.get(src);
		Piece destPiece = chessboardSnapshot.get(dest);
		if(srcPiece == null) {
			notifyInvalidMove(src, dest, srcPiece, side);
			chessWaitLock.unlock();
			return;
		}
		if(srcPiece.side != turn) {
			notifyInvalidMove(src, dest, srcPiece, side);
			chessWaitLock.unlock();
			return;
		}
		
		boolean isMoveValid = srcPiece.isMoveValid(chessboardSnapshot, src, dest);
		if(!isMoveValid) {
			notifyInvalidMove(src, dest, srcPiece, side);
			chessWaitLock.unlock();
			return;
		}
		
		
		// En-passant
		if(srcPiece.getType() == PieceType.PAWN) {
			((PawnAccess) srcPiece).move();
			int dx = dest.x - src.x;
			int dy = dest.y - src.y;
			if(Math.abs(dx) == 1 && Math.abs(dy) == 1 && diStepPawn != null) { //En-passant
				Coordinate2D passant = new Coordinate2D(src.x + dx, src.y);
				Piece passantPiece = chessboardSnapshot.get(passant);
				if(passantPiece != null) {
					if(passantPiece.getType() == PieceType.PAWN) {
						if(passantPiece.side != turn) chessboard.passant(passant);
					} else {
						((PawnAccess) chessboardSnapshot.get(diStepPawn)).diMoveCancel();
					}
				} else {
					((PawnAccess) chessboardSnapshot.get(diStepPawn)).diMoveCancel();
				}
				diStepPawn = null;
			} else { 
				if(diStepPawn != null){
					((PawnAccess) chessboardSnapshot.get(diStepPawn)).diMoveCancel();
				}
				if(Math.abs(dy) > 1) { //Double jump, record en-passant
					((PawnAccess) srcPiece).diMoveBuf();
					diStepPawn = dest;
				} else {
					diStepPawn = null;
				}
			}
			
		} else if(diStepPawn != null) {
			((PawnAccess) chessboardSnapshot.get(diStepPawn)).diMoveCancel();
			diStepPawn = null;
		}
		
		//Translocation
		boolean isTranslocated;
		if(side == Side.BLACK) {
			isTranslocated = blackTranslocated;
		} else if(side == Side.WHITE) {
			isTranslocated = whiteTranslocated;
		} else throw new NullPointerException("Unknown side: " + side.name());
		if(!isTranslocated) {
			if(srcPiece.getType() == PieceType.ROOK) {
				int dx = dest.x - src.x;
				int dy = dest.y - src.y;
				Coordinate2D next;
				if(Math.abs(dx) > 1) {
					next = new Coordinate2D(dest.x + (dx>0?1:-1), dest.y);
				} else if (Math.abs(dy) > 1){
					next = new Coordinate2D(dest.x, dest.y + (dy>0?1:-1));
				} else {
					next = null;
				}
				if(next != null) {
					Piece nextPiece = chessboardSnapshot.get(next);
					if(nextPiece != null) {
						if(nextPiece.getType() == PieceType.KING && nextPiece.side == side) {
							if(inquireTranslocation(next)) {
								chessboard.move(next, new Coordinate2D(dest.x*2-next.x, dest.y*2-next.y));
								if(side == Side.BLACK) {
									blackTranslocated = true;
								} else if(side == Side.WHITE) {
									whiteTranslocated = true;
								} else throw new NullPointerException("Unknown side: " + side.name());
							}
						}
					}
				}
			}
		}
		
		//Ascend
		if(srcPiece.getType() == PieceType.PAWN) {
			if(side == Side.WHITE) {
				if(dest.y == 7) {
					PieceType type = acquireChooseAscend(src);
					chessboard.ascend(src, PieceFactory.createPiece(side, type));
				}
			} else if(side == Side.BLACK) {
				if(dest.y == 0) {
					PieceType type = acquireChooseAscend(src);
					chessboard.ascend(src, PieceFactory.createPiece(side, type));
				}
			} else throw new NullPointerException("Unknown side: " + side.name());
		}
		
		//Normal move
		chessboard.move(src, dest);
		if(destPiece != null) {
			if(destPiece.getType() == PieceType.KING) {
				gameover = true;
			}
		}
		chessWaitCondition.signalAll();
		chessWaitLock.unlock();
		
		/*
		chessboardSnapshotRWL.readLock().lock();
		boolean isMoveValid = srcPiece.isMoveValid(chessboardSnapshot, src, dest);
		chessboardSnapshotRWL.readLock().unlock();
		if(!isMoveValid) {
			notifyInvalidMove(src, dest, srcPiece, side);
			return;
		}*/
		
		/*chessWaitCondition.signalAll();
		chessWaitLock.unlock();*/
	}
	
	private boolean inquireTranslocation(Coordinate2D coord) {
		boolean translocate;
		if(turn == Side.BLACK) {
			blackVisualizerRWL.readLock().lock();
			if(blackVisualizer == null) {
				blackVisualizerRWL.readLock().unlock();
				return false;
			} else {
				blackVisualizer.onCanTranslocation(coord);
				blackVisualizerRWL.readLock().unlock();
				translocationInquireLock.lock();
				try {
					translocationInquireCondition.await();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				translocate = translocationCache;
				translocationInquireLock.unlock();
			}
		} else if (turn == Side.WHITE) {
			whiteVisualizerRWL.readLock().lock();
			if(whiteVisualizer == null) {
				whiteVisualizerRWL.readLock().unlock();
				return false;
			} else {
				whiteVisualizer.onCanTranslocation(coord);
				whiteVisualizerRWL.readLock().unlock();
				translocationInquireLock.lock();
				try {
					translocationInquireCondition.await();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				translocate = translocationCache;
				translocationInquireLock.unlock();
			}
		} else throw new NullPointerException("Unknown side: " + turn.name());
		return translocate;
	}
	
	private void translocate(boolean translocate, Side side) {
		if(side != turn) return;
		translocationInquireLock.lock();
		translocationCache = translocate;
		translocationInquireCondition.signalAll();
		translocationInquireLock.unlock();
	}
	
	// J
	private void quit(Side side) {
		chessWaitLock.lock();
		gameover = true;
		turn = invertSide(side);
		chessWaitCondition.signalAll();
		chessWaitLock.unlock();
	}
	
	private PieceType acquireChooseAscend(Coordinate2D coord) {
		PieceType pieceType;
		if(turn == Side.BLACK) {
			blackVisualizerRWL.readLock().lock();
			if(blackVisualizer == null) {
				blackVisualizerRWL.readLock().unlock();
				return null;
			} else {
				blackVisualizer.onAscend(coord);
				blackVisualizerRWL.readLock().unlock();
				ascendWaitLock.lock();
				try {
					ascendWaitCondition.await();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				pieceType = ascendCache;
				ascendWaitLock.unlock();
			}
		} else if (turn == Side.WHITE) {
			whiteVisualizerRWL.readLock().lock();
			if(whiteVisualizer == null) {
				whiteVisualizerRWL.readLock().unlock();
				return null;
			} else {
				whiteVisualizer.onAscend(coord);
				whiteVisualizerRWL.readLock().unlock();
				ascendWaitLock.lock();
				try {
					ascendWaitCondition.await();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				pieceType = ascendCache;
				ascendWaitLock.unlock();
			}
		} else throw new NullPointerException("Unknown side: " + turn.name());
		return pieceType;
	}
	
	private void chooseAscend(PieceType type, Side side) {
		if(side != turn) return;
		ascendWaitLock.lock();
		ascendCache = type;
		ascendWaitCondition.signalAll();
		ascendWaitLock.unlock();
	}
	
	// V, J
	private void message(String message, Side side) {
		if(side == Side.BLACK) {
			whiteVisualizer.onMessage(message);
		} else if(side == Side.WHITE) {
			blackVisualizer.onMessage(message);
		} else throw new NullPointerException("Unknown side: " + side.name());
	}
	
	private final static class JoystickImpl implements Joystick {
		
		private final ChessGame chessGame;
		
		private final Side side;
		
		public JoystickImpl(ChessGame chessGame, Side side) {
			this.chessGame = chessGame;
			this.side = side;
		}

		@Override
		public void move(Coordinate2D src, Coordinate2D dest) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					chessGame.move(src, dest, side);
				}
			}).start();
			
		}

		@Override
		public void quit() {
			chessGame.quit(side);
		}

		@Override
		public void message(String message) {
			chessGame.message(message, side);
		}

		@Override
		public void ascendTo(PieceType pieceType) {
			chessGame.chooseAscend(pieceType, side);
		}

		@Override
		public void decideTranslocation(boolean translocate) {
			chessGame.translocate(translocate, side);
		}
		
	}

}
