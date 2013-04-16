/**
 * 
 */
package viewer;

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
public class JCommandSequenceTable extends JPanel {

	/** Ensemble de CommandSequence */
	CommandSequence[] _comSeq;
	
	/** CheckBoxes pour les différentes CommandSequence */
	JCheckBox[] _comBox;
	
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
		JComboBox<String> _comChoice = new JComboBox<>();
		for (int i = 0; i < _comSeq.length; i++) {
			_comChoice.addItem(_comSeq[i].getName());
		}
		_comChoice.setSelectedIndex(0);
		add(_comChoice);
		
		// Une Table pour la consigne choisie
		CommandSequenceTableModel _comTableModel = new CommandSequenceTableModel(
				_comSeq[_comChoice.getSelectedIndex()]);
		JTable _comTable = new JTable(_comTableModel);
		_comTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_comTable.setRowSelectionAllowed(false);
		_comTable.setColumnSelectionAllowed(false);
		_comTable.setCellSelectionEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(_comTable);
		_comTable.setFillsViewportHeight(true);
		add(scrollPane);
	}

	
	
}
