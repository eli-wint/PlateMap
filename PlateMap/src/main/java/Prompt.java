package main.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Prompt extends JDialog {
	String val;
	boolean valid;
	
	private Color highlights = new Color(28, 58, 42); 
    private Color mainBody = new Color(85, 107, 85);
    private Color textColor = new Color(220, 223, 230);
	
	public Prompt(JFrame parent) {
		super(parent, "New Save", true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300, 70);
		setLayout(new BorderLayout());
		getContentPane().setBackground(mainBody);
		
		JLabel label = new JLabel("NEW SAVE");
		label.setForeground(textColor);
		label.setBackground(highlights);
		JTextField textField = new JTextField();
		textField.addFocusListener(new FocusListener( ) {

			@Override
			public void focusGained(FocusEvent e) {
				textField.setText("");
				textField.setForeground(Color.BLACK);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});
		
		JButton button = new JButton("CREATE");
		button.setForeground(textColor);
		button.setBackground(highlights);
		button.setBorderPainted(false);
		button.addActionListener(e -> {
			if (!textField.getText().isEmpty() && textField.getText() != null) {
				val = textField.getText().replace(" ", "_");
				dispose();
			} else {
				textField.setForeground(Color.GRAY);
				textField.setText("INVALID");
			}
		});
		
		add(label, BorderLayout.WEST);
		add(textField, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);
		
		setVisible(true);
		setResizable(false);
	}
	
	public String getVal() {
		return val;
	}
}
