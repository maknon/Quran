package classes;

// Check this: http://home.arcor.de/andi.warnke/SourceCodes/Java/RETE-DB_06/utils/Convert.java.F_0000.html
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

class UnicodeConvertor extends JFrame implements ClipboardOwner
{
	UnicodeConvertor()
	{
		super("Unicode Convertor");
		
		final JTextArea unicode = new JTextArea(8, 50);
		final JTextArea hex = new JTextArea(8, 50);
		
		unicode.setLineWrap(true);
		unicode.setWrapStyleWord(true);
		
		hex.setLineWrap(true);
		hex.setWrapStyleWord(true);
		
		JButton reset = new JButton("Reset");
		JButton cpu = new JButton(" Copy ");
		JButton cph = new JButton(" Copy ");
		JButton cth = new JButton("Convert");
		JButton ctu = new JButton("Convert");
		
		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				unicode.setText("");
				hex.setText("");
			}
		});
		
		cpu.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){setClipboardContents(unicode.getText());}});
		cph.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){setClipboardContents(hex.getText());}});
		
		ctu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				StringBuilder sb = new StringBuilder();
				String s = hex.getText();
				for(int i=0; i<s.length(); i++)
				{
					if(s.charAt(i)=='\\')
					{
						sb.append((char)Integer.parseInt(s.substring(i+2, i+6), 16));
						i=i+5;
					}
					else
						sb.append(s.charAt(i));
				}
				unicode.setText(sb.toString());
			}
		});
		
		cth.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				/*
				//  open up standard input
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line = null;
				try{line = br.readLine();}
				catch(IOException ioe){}
				
				StringBuilder sb = new StringBuilder();
				int base = Integer.parseInt(line);
				String s[] = unicode.getText().split("\n");
				for(int i=0; i<s.length; i++)
				{
					sb.append(Integer.parseInt(s[i])+base);
					sb.append("\n");
				}
				*/
				
				StringBuilder sb = new StringBuilder();
				String s = unicode.getText();
				for(int i=0; i<s.length(); i++)
				{
					char c = s.charAt(i);
					if (c <= 0x7E)
						sb.append(c);
					else
						sb.append(String.format("\\u%04X", (int)c));
				}
				
				hex.setText(sb.toString());
			}
		});
		
		JPanel unicodePanel = new JPanel(new BorderLayout());
		JPanel hexPanel = new JPanel(new BorderLayout());
		unicodePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Unicode", 0, 0, null, Color.red));
		hexPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Hex", 0, 0, null, Color.red));
		
		unicodePanel.add(cpu, BorderLayout.EAST);
		unicodePanel.add(cth, BorderLayout.WEST);
		unicodePanel.add(new JScrollPane(unicode), BorderLayout.NORTH);
		hexPanel.add(cph, BorderLayout.EAST);
		hexPanel.add(ctu, BorderLayout.WEST);
		hexPanel.add(new JScrollPane(hex), BorderLayout.NORTH);
		
		add(unicodePanel, BorderLayout.NORTH);
		add(hexPanel, BorderLayout.CENTER);
		add(reset, BorderLayout.SOUTH);
		
		unicode.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	void setClipboardContents(String aString)
	{
		StringSelection stringSelection = new StringSelection( aString );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}
	
	public void lostOwnership( Clipboard aClipboard, Transferable aContents){}
	public static void main(String args[])
	{
		UIManager.put("TextArea.font", new javax.swing.plaf.FontUIResource( new Font("Tahoma", Font.PLAIN, 12) ));
		new UnicodeConvertor();
	}
}