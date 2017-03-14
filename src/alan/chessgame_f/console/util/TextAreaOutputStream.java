package alan.chessgame_f.console.util;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class TextAreaOutputStream extends OutputStream {

	// *************************************************************************************************
	// INSTANCE MEMBERS
	// *************************************************************************************************
	
	//private byte[]                          oneByte;                                                    // array for write(int val);
	private Appender                        appender;                                                   // most recent action
	
	public TextAreaOutputStream(JTextArea txtara) {
		this(txtara,1000);
	}
	
	public TextAreaOutputStream(JTextArea txtara, int maxlin) {
		if(maxlin<1) { throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value="+maxlin+")"); }
		//oneByte=new byte[1];
		appender=new Appender(txtara,maxlin);
	}
	
	/** Clear the current console text area. */
	public void clear() {
		synchronized (this) {
			if(appender!=null) { appender.clear(); }
		}
	}
	
	public void close() {
		appender = null;
	}

	public void flush() {
	}
	
	public void write(int val) {
		write(new byte[]{(byte) val}, 0, 1);
	}
	
	public void write(byte[] ba) {
		write(ba,0,ba.length);
	}
	
	public void write(byte[] ba,int str,int len) {
		synchronized (this) {
			if(appender!=null) { appender.append(bytesToString(ba,str,len)); }
		}
	}

	static private String bytesToString(byte[] ba, int str, int len) {
		try { return new String(ba,str,len,"UTF-8"); } catch(UnsupportedEncodingException thr) { return new String(ba,str,len); } // all JVMs are required to support UTF-8
	}
	
	// *************************************************************************************************
	// STATIC MEMBERS
	// *************************************************************************************************

	final static class Appender implements Runnable
	{
		private final JTextArea             textArea;
		private final int                   maxLines;                                                   // maximum lines allowed in text area
		private final LinkedList<Integer>   lengths;                                                    // length of lines within text area
		private final List<String>          values;                                                     // values waiting to be appended

		private int                         curLength;                                                  // length of current line
		private boolean                     clear;
		private boolean                     queue;

		Appender(JTextArea txtara, int maxlin) {
			textArea =txtara;
			maxLines =maxlin;
			lengths  =new LinkedList<Integer>();
			values   =new ArrayList<String>();
			
			curLength=0;
			clear    =false;
			queue    =true;
		}
		
		void append(String val) {
			synchronized (this) {
				values.add(val);
				if(queue) { queue=false; EventQueue.invokeLater(this); }
			}
		}
		
		void clear() {
			synchronized (this) {
				clear=true;
				curLength=0;
				lengths.clear();
				values.clear();
				if(queue) { queue=false; EventQueue.invokeLater(this); }
			}
		}

		// MUST BE THE ONLY METHOD THAT TOUCHES textArea!
		public void run() {
			synchronized (this) {
				if(clear) { textArea.setText(""); }
				for(String val: values) {
					curLength+=val.length();
					if(val.endsWith(EOL1) || val.endsWith(EOL2)) {
						if(lengths.size()>=maxLines) { textArea.replaceRange("",0,lengths.removeFirst()); }
						lengths.addLast(curLength);
						curLength=0;
					}
					textArea.append(val);
				}
				values.clear();
				clear =false;
				queue =true;
			}
		}
		
		static private final String         EOL1="\n";
		static private final String         EOL2=System.getProperty("line.separator",EOL1);
	}

}