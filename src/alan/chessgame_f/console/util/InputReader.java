package alan.chessgame_f.console.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTextField;


public class InputReader {
	
	public String content;
	
	private final Lock waitLock = new ReentrantLock();
	private final Condition waitCondition = waitLock.newCondition();
	
	private final JTextField textField;
	
	public InputReader(JTextField textField) {
		this.textField = textField;
		textField.addActionListener(new EnterListener());
	}
	
	public String read() {
		String content;
		waitLock.lock();
		try {
			waitCondition.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		content = this.content;
		waitLock.unlock();
		return content;
	}
	
	private void write(String content) {
		waitLock.lock();
		this.content = content;
		waitCondition.signalAll();
		waitLock.unlock();
	}
	
	private final class EnterListener implements ActionListener {

		@Override
		public synchronized void actionPerformed(ActionEvent e) {
			write(textField.getText());
			
			textField.setText(null);
		}
		
	}

}
