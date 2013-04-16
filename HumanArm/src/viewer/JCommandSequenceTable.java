/**
 * 
 */
package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import model.CommandSequence;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class JCommandSequenceTable extends JPanel implements ActionListener {

	/** Ensemble de CommandSequence */
	CommandSequence[] _comSeq;
	
	/** CheckBoxes pour les différentes CommandSequence */
	JCheckBox[] _comBox;
	/** ComboBox pour les choix de ComSeq à détailler */
	JComboBox<String> _comChoice;
	/** Table pour les détails */
	JTable _comTable;
	
	/**
	 * 
	 */
	public JCommandSequenceTable( CommandSequence[] comSeq) {
		super();
		_comSeq = comSeq;
		buildGUI();
	}

	private void buildGUI() {
		// Set Layout -> Vertical
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		JLabel visLabel = new JLabel("Consignes visibles");
		add(visLabel);
		
		// Check Boxes
		_comBox = new JCheckBox[_comSeq.length];
		for (int i = 0; i < _comSeq.length; i++) {
			_comBox[i] = new JCheckBox(_comSeq[i].getName(),true);
			add(_comBox[i]);
		}
		
		JSeparator sep = new JSeparator();
		add(sep);
		
		// Une JCombBox pour choisir celui qui est dans la table
		JLabel choiceLabel = new JLabel("Consigne détaillée");
		add(choiceLabel);
		_comChoice = new JComboBox<>();
		for (int i = 0; i < _comSeq.length; i++) {
			_comChoice.addItem(_comSeq[i].getName());
		}
		_comChoice.setSelectedIndex(0);
		_comChoice.addActionListener(this);
		add(_comChoice);
		
		// Une Table pour la consigne choisie
		CommandSequenceTableModel _comTableModel = new CommandSequenceTableModel(
				_comSeq[_comChoice.getSelectedIndex()]);
		_comTable = new JTable(_comTableModel);
		_comTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_comTable.setRowSelectionAllowed(false);
		_comTable.setColumnSelectionAllowed(false);
		_comTable.setCellSelectionEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(_comTable);
		_comTable.setFillsViewportHeight(true);
		add(scrollPane);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        int indexComSeq = _comChoice.getSelectedIndex();
       
        CommandSequenceTableModel _comTableModel = new CommandSequenceTableModel(
				_comSeq[_comChoice.getSelectedIndex()]);
        _comTable.setModel(_comTableModel);
        //_comTableModel.fireTableDataChanged();
		
	}

	
	
}
