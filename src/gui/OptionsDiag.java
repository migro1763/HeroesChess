package gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class OptionsDiag extends JFrame implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	final JDialog dialog;
	private JOptionPane optionPane;

	public OptionsDiag(String question, String[] choices, int def, int x, int y) {
		
		super(); // initialize JFrame (this)	
		optionPane = new JOptionPane(question, JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, choices, choices[def]);        
		
		dialog = new JDialog(this, "Click a button", true);
		dialog.setContentPane(optionPane);
		dialog.setLocation(x - dialog.getWidth(), y - dialog.getHeight());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {}
		});
		optionPane.addPropertyChangeListener(this);
		dialog.pack();
		dialog.setVisible(true);
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
}
