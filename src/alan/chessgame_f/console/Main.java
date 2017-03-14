package alan.chessgame_f.console;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import alan.chessgame_f.ChessGame;
import alan.chessgame_f.Chessboard;
import alan.chessgame_f.Side;
import alan.chessgame_f.chess.Piece;
import alan.chessgame_f.chess.PieceType;
import alan.chessgame_f.console.util.ConsoleFrame;
import alan.chessgame_f.io.Joystick;
import alan.chessgame_f.io.Visualizer;
import alan.chessgame_f.util.Coordinate2D;

public final class Main {
	
	private static boolean stopAppealed;
	
	private static boolean switr;
	
	private final static AtomicBoolean contA = new AtomicBoolean();
	private final static AtomicBoolean contB = new AtomicBoolean();
	private final static Lock INTERROLE_LOCK = new ReentrantLock();
	private final static Condition INTERROLE_CONDITION = INTERROLE_LOCK.newCondition();
	
	public static void main(String[] args) {
		
		ChessGame chessGame = new ChessGame();
		ConsoleHandler handlerA = new ConsoleHandler(chessGame, Side.BLACK);
		ConsoleHandler handlerB = new ConsoleHandler(chessGame, Side.WHITE);
		chessGame.setVisualizer(handlerA, handlerA.side);
		chessGame.setVisualizer(handlerB, handlerB.side);
		handlerA.init();
		handlerB.init();
		for(;;) {
			chessGame.run();
			
			handlerA.notifyEndOfGame();
			handlerB.notifyEndOfGame();
			
			contA.set(false);
			contB.set(false);
			
			for(;;) {
				INTERROLE_LOCK.lock();
				try {
					INTERROLE_CONDITION.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				INTERROLE_LOCK.unlock();
				if(switr == true){
					Joystick tempJoystick = handlerA.joystick;
					handlerA.joystick = handlerB.joystick;
					handlerB.joystick = tempJoystick;
					
					Side tempSide = handlerA.side;
					handlerA.side = handlerB.side;
					handlerB.side = tempSide;
					chessGame.setVisualizer(handlerA, handlerA.side);
					chessGame.setVisualizer(handlerB, handlerB.side);
					
					handlerA.updateTitle();
					handlerB.updateTitle();
					switr = false;
				}
				if(contA.get() && contB.get()) {
					break;
				}
			}
		}
		
	}
	
	private final static class ConsoleHandler implements Visualizer {
		
		private final ChessGame chessGame;
		public Joystick joystick;
		public Side side;
		private final ConsoleFrame consoleFrame;
		private boolean initialized;
		private Map<Coordinate2D, Piece> chessboardCache;
		
		public ConsoleHandler(ChessGame chessGame, Side side) {
			this.chessGame = chessGame;
			this.side = side;
			if(side == Side.BLACK) {
				joystick = chessGame.blackJoystick;
			} else if(side == Side.WHITE) {
				joystick = chessGame.whiteJoystick;
			} else throw new NullPointerException("Unknown side: " + side.name());
			
			consoleFrame = new ConsoleFrame("Chess Game : " + side.name());
		}

		@Override
		public void onInvalidMove(Coordinate2D coord, Coordinate2D dest, Piece piece) {
			if(piece == null){
				consoleFrame.out.println("Here's no piece on "+ wrapPos(coord));
			} else {
				consoleFrame.out.println(
					"You can't move "+ piece.getType().name() +" from " + wrapPos(coord) + " to " + wrapPos(dest)
					);
			}
		}

		@Override
		public void onChessboardUpdate(Map<Coordinate2D, Piece> chessboard) {
			chessboardCache = chessboard;
			printChessboard(side);
		}

		@Override
		public void onTurnChanges(Side side) {
			if(side == this.side) {
				consoleFrame.out.println("It is your turn.");
			} else consoleFrame.out.println("Round changes to " + side.name());
		}

		@Override
		public void onGameover(Side side) {
			consoleFrame.out.println(side.name() + " side won the game! Congratulations!");
		}

		@Override
		public void onMessage(String message) {
			consoleFrame.out.println(message);
		}

		@Override
		public void onAscend(Coordinate2D coord) {
			consoleFrame.out.println("Please select a type for piece on "+ wrapPos(coord) + " to ascend.");
			consoleFrame.out.println("By using \"ascend <Type>\", you can ascend that piece");
			consoleFrame.out.println("You can change your pawn to these pieces below:");
			consoleFrame.out.println("Rook, Knight, Bishop, King");
		}
		
		@Override
		public void onCanTranslocation(Coordinate2D coord) {
			consoleFrame.out.println("You can make a translocation of king on "+ wrapPos(coord) + ".");
			consoleFrame.out.println("By using \"yes\" or \"no\" to decide if you want to translocate.");
		}
		
		public void init(){
			if(initialized) return;
			Font font;
			if(ConsoleFrame.isFontFamilyExist("Monaco")) {
				font = new Font("Monaco", Font.PLAIN, 12);
			} else if(ConsoleFrame.isFontFamilyExist("Consolas")) {
				font = new Font("Consolas", Font.PLAIN, 12);
			} else {
				font = new Font("Courier", Font.PLAIN, 12);
			}
			consoleFrame.textArea.setFont(font);
			consoleFrame.textField.setFont(font);
			
			consoleFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					consoleFrame.dispose();
					if(stopAppealed) {
						System.exit(0);
					} else { 
						stopAppealed = true;
						joystick.message("Your competitor closed the window.");
					}
				}
			});
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(;;) {
						String line = consoleFrame.in.read();
						consoleFrame.out.println(" > " + line);
						String[] ag = parse(line);
						String cmd = ag[0];
						
						if(cmd.equalsIgnoreCase("")) {
							if(chessGame.isRunning()) {
								// I'm certain that here's no code :)
							} else if(side == Side.BLACK) {
								boolean continueA = contA.getAndSet(true);
								if(continueA) {
									consoleFrame.out.println("You've already got ready.");
									continue;
								}
								consoleFrame.out.println("You got ready.");
								joystick.message("You competitor is ready.");
								INTERROLE_LOCK.lock();
								INTERROLE_CONDITION.signalAll();
								INTERROLE_LOCK.unlock();
							} else if (side == Side.WHITE) {
								boolean continueB = contB.getAndSet(true);
								if(continueB) {
									consoleFrame.out.println("You've already got ready.");
									continue;
								}
								consoleFrame.out.println("You got ready.");
								joystick.message("You competitor is ready.");
								INTERROLE_LOCK.lock();
								INTERROLE_CONDITION.signalAll();
								INTERROLE_LOCK.unlock();
							} else throw new NullPointerException("Unknown side: " + side.name());
						} else if(cmd.equalsIgnoreCase("?")) {
							sendHelp();
						} else if (cmd.equalsIgnoreCase("move")) {
							if(ag.length > 2) {
								Coordinate2D src = resolvePos(ag[1]), dest = resolvePos(ag[2]);
								if(src == null && dest == null) {
									consoleFrame.out.println("I can't understand your coordinate "+ ag[1] + " and " + ag[2] + ".");
								} else if(src == null) {
									consoleFrame.out.println("I can't understand your coordinate "+ ag[1] + ".");
								} else if(dest == null) {
									consoleFrame.out.println("I can't understand your coordinate "+ ag[2] + ".");
								}
								joystick.move(src, dest);
							} else {
								consoleFrame.out.println("Not enough argument...");
								consoleFrame.out.println("Usage: \"move <Pos1> <Pos2>\"");
							}
						} else if (cmd.equalsIgnoreCase("ascend")) {
							if(ag.length > 1) {
								PieceType type;
								if(ag[1].equalsIgnoreCase("queen")) {
									type = PieceType.QUEEN;
								} else if(ag[1].equalsIgnoreCase("rook")) {
									type = PieceType.ROOK;
								} else if(ag[1].equalsIgnoreCase("knight")) {
									type = PieceType.KNIGHT;
								} else if(ag[1].equalsIgnoreCase("bishop")) {
									type = PieceType.BISHOP;
								} else if(ag[1].equalsIgnoreCase("king")) {
									consoleFrame.out.println("Your pawn can't become a king!");
									consoleFrame.out.println("You can change your pawn to these pieces below:");
									consoleFrame.out.println("Rook, Knight, Bishop, King");
									continue;
								} else if(ag[1].equalsIgnoreCase("pawn")) {
									consoleFrame.out.println("Your pawn must become something else!");
									continue;
								} else {
									consoleFrame.out.println("I'm not sure what you have entered...");
									consoleFrame.out.println("You can change your pawn to these pieces below:");
									consoleFrame.out.println("Rook, Knight, Bishop, King");
									continue;
								}
								joystick.ascendTo(type);
							} else {
								consoleFrame.out.println("I'm not sure what kind of piece you want to ascend :(");
								consoleFrame.out.println("You can change your pawn to these pieces below:");
								consoleFrame.out.println("Rook, Knight, Bishop, King");
							}
						} else if (cmd.equalsIgnoreCase("say")) {
							if(ag.length > 1) {
								joystick.message("- " + line.substring(line.indexOf(' ')+1));
							} else {
								consoleFrame.out.println("Say something ;)");
							}
						} else if (cmd.equalsIgnoreCase("quit")) {
							joystick.quit();
						} else if (cmd.equalsIgnoreCase("exit")) {
							consoleFrame.dispose();
							if(stopAppealed) {
								System.exit(0);
							} else { 
								stopAppealed = true;
								joystick.message("Your competitor closed the window.");
							}
						} else if (cmd.equalsIgnoreCase("clear")) {
							consoleFrame.clear();
							consoleFrame.out.println("Console cleared.");
						} else if (cmd.equalsIgnoreCase("switch")){
							if(chessGame.isRunning()) {
								consoleFrame.out.println("Game is not over yet, can't switch black and white :P");
							} else {
								switr = true;
								contA.set(false);
								contB.set(false);
								consoleFrame.out.println("Side switched, ready status is cancelled.");
								joystick.message("Side switched, ready status is cancelled.");
								INTERROLE_LOCK.lock();
								INTERROLE_CONDITION.signalAll();
								INTERROLE_LOCK.unlock();
							}
						} else if (cmd.equalsIgnoreCase("no")) {
							joystick.decideTranslocation(false);
						} else if (cmd.equalsIgnoreCase("yes")) {
							joystick.decideTranslocation(true);
						} else {
							consoleFrame.out.println("Unknown command!");
							sendHelp();
						}
					}
				}
				
			}).start();;
			
			consoleFrame.setVisible(true);
		}
		
		private void sendHelp(){
			consoleFrame.out.println("commands: ");
			consoleFrame.out.println("\"Press enter\" : To confirm the game continue after gameover");
			consoleFrame.out.println("? : To get some help about commands");
			consoleFrame.out.println("move <Pos1> <Pos2>: To move a piece");
			consoleFrame.out.println("ascend <Type> : To ascend a pawn");
			consoleFrame.out.println("say <Message> : To say something to your competitor");
			consoleFrame.out.println("quit : To quit a round, and your competitor win the game");
			consoleFrame.out.println("exit : To exit game, and notify your competitor that you closed the window");
			consoleFrame.out.println("clear : To make your console clear");
			consoleFrame.out.println("switch : Switch black and white side");
		}
		
		public void updateTitle() {
			consoleFrame.setTitle("Chess Game : " + side.name());
		}
		
		/* |Coordinate.y
		 *     ┌───┬───┬───┬───┬───┬───┬───┬───┐
		 * 7 8 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 6 7 │   │   │WBp│   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 5 6 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 4 5 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 3 4 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 2 3 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 1 2 │   │   │   │   │   │   │   │   │
		 *     ├───┼───┼───┼───┼───┼───┼───┼───┤
		 * 0 1 │   │   │   │   │   │   │   │   │
		 *     └───┴───┴───┴───┴───┴───┴───┴───┘
		 *       A   B   C   D   E   F   G   H   
		 *       0   1   2   3   4   5   6   7  -Coordinate.x
		 */
		
		private void printChessboard(Side side) {
			if(side == Side.WHITE) {
				printWhiteChessboard();
			} else if (side == Side.BLACK) {
				printBlackChessborad();
			} else throw new NullPointerException("Unknown side: " + side.name());
		}
		
		private void printWhiteChessboard() {
			Piece[][] pieces = Chessboard.toArray(chessboardCache);
			String[][] outputs = new String[8][8];
			for(int i=0;i<8;i++) {
				for(int j=0;j<8;j++) {
					outputs[7-j][i] = wrapPiece(pieces[i][j]);
				}
			}
			int i=0;
			consoleFrame.out.println("   ┌───┬───┬───┬───┬───┬───┬───┬───┐");
			for(;i<7;i++) {
				consoleFrame.out.print(' ');
				consoleFrame.out.print(8-i);
				consoleFrame.out.print(" |");
				for(int j=0;j<8;j++) {
					consoleFrame.out.print(outputs[i][j]);
					consoleFrame.out.print('|');
				}
				consoleFrame.out.println();
				consoleFrame.out.println("   ├───┼───┼───┼───┼───┼───┼───┼───┤");
			}
			
			consoleFrame.out.print(' ');
			consoleFrame.out.print(8-i);
			consoleFrame.out.print(" |");
			for(int j=0;j<8;j++) {
				consoleFrame.out.print(outputs[i][j]);
				consoleFrame.out.print('|');
			}
			consoleFrame.out.println();
			consoleFrame.out.println("   └───┴───┴───┴───┴───┴───┴───┴───┘");
			consoleFrame.out.println("     A   B   C   D   E   F   G   H");
		}
		
		private void printBlackChessborad(){
			Piece[][] pieces = Chessboard.toArray(chessboardCache);
			String[][] outputs = new String[8][8];
			for(int i=0;i<8;i++) {
				for(int j=0;j<8;j++) {
					outputs[j][7-i] = wrapPiece(pieces[i][j]);
				}
			}
			int i=0;
			consoleFrame.out.println("   ┌───┬───┬───┬───┬───┬───┬───┬───┐");
			for(;i<7;i++) {
				consoleFrame.out.print(' ');
				consoleFrame.out.print(i+1);
				consoleFrame.out.print(" |");
				for(int j=0;j<8;j++) {
					consoleFrame.out.print(outputs[i][j]);
					consoleFrame.out.print('|');
				}
				consoleFrame.out.println();
				consoleFrame.out.println("   ├───┼───┼───┼───┼───┼───┼───┼───┤");
			}
			
			consoleFrame.out.print(' ');
			consoleFrame.out.print(i+1);
			consoleFrame.out.print(" |");
			for(int j=0;j<8;j++) {
				consoleFrame.out.print(outputs[i][j]);
				consoleFrame.out.print('|');
			}
			consoleFrame.out.println();
			consoleFrame.out.println("   └───┴───┴───┴───┴───┴───┴───┴───┘");
			consoleFrame.out.println("     H   G   F   E   D   C   B   A");
		}

		public void notifyEndOfGame() {
			consoleFrame.out.println("Press enter to be ready to replay.");
		}
		
	}
	
	
	
	private final static String[] EMPTY_STRING_ARRAY = new String[0];
	
	private static String[] parse(String line) {
		List<String> li = new LinkedList<>();
		int p = 0;
		for(;;){
			int l = line.indexOf(' ', p);
			if(l > 0){
				li.add(line.substring(p, l));
				p = l+1;
			}else {
				li.add(line.substring(p));
				break;
			}
		}
		return li.toArray(EMPTY_STRING_ARRAY);
		
	}
	
	private static Coordinate2D resolvePos(String pos) {
		if(pos.length() != 2) return null;
		char x = pos.charAt(0);
		char y = pos.charAt(1);
		if((x < 'I' && x >= 'A') || (x < 'i' && x >= 'a')) {
			if(y < '9' && y >= '1') {
				return new Coordinate2D(x<='Z' ? x-0x41 : x-0x61, y-0x31);
			} else return null;
		} else if ((y < 'I' && y >= 'A') || (y < 'i' && y >= 'a')) {
			if(x < '9' && x >= '1') {
				return new Coordinate2D(y<='Z' ? y-0x41 : y-0x61, x-0x31);
			} else return null;
		} else return null;
	}
	
	private static String wrapPos(Coordinate2D pos) {
		if(pos.x >=0 && pos.x < 8 && pos.y >= 0 && pos.y < 8) {
			return new String(new char[]{(char) (pos.x + 0x41), (char) (pos.y + 0x31)});
		} else return null;
	}
	
	/*
	public static void main(String[] args) {
		Coordinate2D f = resolvePos("A3");
		System.out.println("("+f.x+','+f.y+')');
		System.out.println(wrapPos(f));
	} */

	private static String wrapPiece(Piece piece) {
		if(piece == null) return "   ";
		StringBuilder stringBuilder = new StringBuilder();
		Side side = piece.side;
		PieceType type = piece.getType();
		if(side == Side.BLACK) {
			stringBuilder.append('B');
		} else if (side == Side.WHITE) {
			stringBuilder.append('W');
		} else throw new NullPointerException("Unknown side: " + side.name());
		
		if(type == PieceType.KING) {
			stringBuilder.append("Kg");
		} else if(type == PieceType.QUEEN) {
			stringBuilder.append("Qu");
		} else if(type == PieceType.KNIGHT) {
			stringBuilder.append("Kt");
		} else if(type == PieceType.ROOK) {
			stringBuilder.append("Rk");
		} else if(type == PieceType.PAWN) {
			stringBuilder.append("Pn");
		} else if(type == PieceType.BISHOP) {
			stringBuilder.append("Bp");
		} else throw new NullPointerException("Unknown piece type: " + type.name());
		
		 return stringBuilder.toString();
	}

}
