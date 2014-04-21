package gui;

//import java.awt.BorderLayout;
//import java.awt.Container;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JTextField;

public class OptionsDiag extends JFrame implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	final JDialog dialog;
	private JOptionPane optionPane;
//	private JTextField textField;
//	private String inputText = "";
//	private int choice = -1;
	private String[] choices;

	public OptionsDiag(String question, String[] choices, int def, int x, int y) {		
		super(); // initialize JFrame (this)
		this.choices = choices;
		dialog = new JDialog(this);
		optionPane = new JOptionPane(question, JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, choices, choices[def]);        
		initDialog("Choose a button");
		dialog.setContentPane(optionPane);
		dialog.setLocation(x - dialog.getWidth(), y - dialog.getHeight());
		optionPane.addPropertyChangeListener(this);
		dialog.pack();
		dialog.setVisible(true);
	}
	
//	public OptionsDiag(Container dialog, String question, int x, int y) {
//		super(); // initialize JFrame (this)
//		final List<String> holder = new LinkedList<String>();
//		textField = new JTextField();
//		dialog.setLayout(new BorderLayout());
//		dialog.add(new JLabel(question), BorderLayout.NORTH);
//		dialog.add(textField, BorderLayout.SOUTH);
//		dialog.setLocation(x - dialog.getWidth(), y - dialog.getHeight());
//		textField.addActionListener(new ActionListener() {
//	        @Override
//	        public void actionPerformed(ActionEvent e) {
//	            synchronized (holder) {
//	                holder.add(textField.getText());
//	                holder.notify();
//	            }
//	        }
//	    });
//		dialog.setVisible(true);
//		synchronized (holder) {
//	        // wait for input from field
//	        while (holder.isEmpty())
//				try {
//					holder.wait();
//				} catch (InterruptedException e1) {}
//	        inputText = holder.remove(0);
//	    }
//		this.dispose();
//	}
	
	public void initDialog(String name) {
		dialog.setTitle(name);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {}
		});		
	}

	public JOptionPane getOptionPane() {
		return optionPane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (dialog.isVisible() 
         && (e.getSource() == optionPane)
         && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
            dialog.setVisible(false);
        }
    }	

	public int getChoice() {
		return Arrays.asList(choices).indexOf(optionPane.getValue());
	}
	
//	public String getInputText() {
//		return inputText;
//	}

}
