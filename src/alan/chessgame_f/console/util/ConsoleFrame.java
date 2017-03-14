package alan.chessgame_f.console.util;

import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ConsoleFrame extends JFrame {
	
	private final static Set<String> FONT_FAMILIES = new HashSet<>();
	static {
		for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
			FONT_FAMILIES.add(fontName);
		}
	}
	
	public static boolean isFontFamilyExist(String name){
		return FONT_FAMILIES.contains(name);
	}
	
	private static final long serialVersionUID = 7448214484486157723L;

	public final PrintStream out;
	private final TextAreaOutputStream outStream;
	
	public final InputReader in;
	
	public final JTextArea textArea;
	
	public final JTextField textField;
	
	public ConsoleFrame() {
		this("Console");
	}
	
	public ConsoleFrame(String title) {
		super(title);
		textArea = new JTextArea();
		outStream = new TextAreaOutputStream(textArea);
		out = new PrintStream(outStream);
		textField = new JTextField();
		in = new InputReader(textField);
		init();
	}
	
	public ConsoleFrame(String title, int maxLen) {
		super(title);
		textArea = new JTextArea();
		outStream = new TextAreaOutputStream(textArea, maxLen);
		out = new PrintStream(outStream);
		textField = new JTextField();
		in = new InputReader(textField);
		init();
	}
	
	private void init(){
		setSize(640, 480);
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		textArea.setEditable(false);
		textField.setEditable(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane);
		add(textField);
		layout.putConstraint(SpringLayout.SOUTH, textField, -5, SpringLayout.SOUTH, this.getContentPane());
		layout.putConstraint(SpringLayout.WEST, textField, 5, SpringLayout.WEST, this.getContentPane());
		layout.putConstraint(SpringLayout.EAST, textField, -5, SpringLayout.EAST, this.getContentPane());
		
		layout.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.NORTH, this.getContentPane());
		layout.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, this.getContentPane());
		layout.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, this.getContentPane());
		layout.putConstraint(SpringLayout.SOUTH, scrollPane, -5, SpringLayout.NORTH, textField);
	}
	
	public void clear() {
		outStream.clear();
	}
	
	/* public static void main(String[] args) {
		ConsoleFrame frame = new ConsoleFrame();
		
		Font font;
		if(isFontFamilyExist("Monaco")) {
			font = new Font("Monaco", Font.PLAIN, 12);
		} else if(isFontFamilyExist("Consolas")) {
			font = new Font("Consolas", Font.PLAIN, 12);
		} else {
			font = new Font("Courier", Font.PLAIN, 12);
		}
		frame.textArea.setFont(font);
		frame.textField.setFont(font);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				Stop.tryStop(30);
			}
		});
		
		frame.setVisible(true);
		
		while(true) {
			String cmd = frame.in.read();
			if(cmd.equalsIgnoreCase("stop")) {
				frame.out.println("Stoped the program.");
				break;
			} else {
				frame.out.println("Console: " + cmd);
			}
		}
		
		frame.dispose();
		
	} */
	
	/*public static void main(String[] args) {
		Lock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		Thread[] threads = new Thread[10];
		for(int i=0;i<10;i++) {
			threads[i] = new Thread(new A(lock, condition));
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		lock.lock();
		for(int i=0;i<10;i++) {
			condition.signalAll();
		}
		lock.unlock();
	}
	
	private static class A implements Runnable {
		
		public final Lock lock;
		public final Condition condition;
		
		public A(Lock lock, Condition condition) {
			this.lock = lock;
			this.condition = condition;
		}

		@Override
		public void run() {
			lock.lock();
			try {
				condition.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try{
				System.out.println("A");
				Thread.sleep(100);
				System.out.println("B");
				Thread.sleep(100);
				System.out.println("C");
				Thread.sleep(100);
				System.out.println("D");
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			lock.unlock();
		}
		
	}*/

}
