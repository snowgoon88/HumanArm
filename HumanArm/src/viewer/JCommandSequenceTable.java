/**
 * 
 */
package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import model.Command;
import model.Consignes;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JCommandSequenceTable extends JPanel
implements ActionListener, Observer {

	/** Ensemble de CommandSequence */
	Consignes _consignes;
	
	/** CheckBoxes pour les différentes CommandSequence */
	JCheckBox[] _comBox;
	/** ComboBox pour les choix de ComSeq à détailler */
	JComboBox<String> _comChoice;
	/** Table pour les détails */
	JTable _comTable;
	/** TableModel pour la Command Sequence */
	CommandSequenceTableModel _comTableModel = null;
	
	/**
	 * Pour afficher et modifier le détails des CommandSequence.
	 */
	public JCommandSequenceTable( Consignes consigne) {
		super();
		_consignes = consigne;
		buildGUI();
	}

	private void buildGUI() {
		// Set Layout -> Vertical
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		JLabel visLabel = new JLabel("Consignes visibles");
		add(visLabel);
		
		// Check Boxes
		_comBox = new JCheckBox[_consignes.size()];
		for (int i = 0; i < _consignes.size(); i++) {
			_comBox[i] = new JCheckBox(_consignes.get(i).getName(),true);
			add(_comBox[i]);
		}
		
		JSeparator sep = new JSeparator();
		add(sep);
		
		// Une JCombBox pour choisir celui qui est dans la table
		JLabel choiceLabel = new JLabel("Consigne détaillée");
		add(choiceLabel);
		_comChoice = new JComboBox<>();
		for (int i = 0; i < _consignes.size(); i++) {
			_comChoice.addItem(_consignes.get(i).getName());
		}
		_comChoice.setSelectedIndex(0);
		_comChoice.setActionCommand("choice");
		_comChoice.addActionListener(this);
		add(_comChoice);
		
		// Une Table pour la consigne choisie
		_comTableModel = new CommandSequenceTableModel(
				_consignes.get(_comChoice.getSelectedIndex()));
		_comTable = new JTable(_comTableModel);
		_comTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_comTable.setRowSelectionAllowed(false);
		_comTable.setColumnSelectionAllowed(false);
		_comTable.setCellSelectionEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(_comTable);
		_comTable.setFillsViewportHeight(true);
		add(scrollPane);
		
		// button below : "Insérer une ligne"
		JButton insertBtn = new JButton("Insérer Consigne");
		insertBtn.setActionCommand("insert");
		insertBtn.addActionListener(this);
		add(insertBtn);
	}

	@Override
	/**
	 * Les actions possibles :
	 *  - "insert" : ajoute une ligne dans la ComSeq qui est détaillée
	 *  - "choice" : change la ComSeq qui est détaillée
	 */
	public void actionPerformed(ActionEvent e) {
		System.out.println("ACTION: "+e.getActionCommand());
		
		switch (e.getActionCommand()) {
		case "choice": // Change le ComSeq détaillé
			if (_comChoice.getSelectedIndex() >= 0 ) {
				_comTableModel = new CommandSequenceTableModel(
						_consignes.get(_comChoice.getSelectedIndex() ));
				_comTable.setModel(_comTableModel);
			}
			break;
		case "insert": // Insère une nouvelle commande
			int row = _comTableModel.getRowCount();
			double time = (double)_comTableModel.getValueAt(row-1, 0) + 0.5;
			int indexComSeq = _comChoice.getSelectedIndex();
			_consignes.get(indexComSeq).add(new Command(time, 0));
			_comTableModel.fireTableDataChanged();
			break;
			
		default:
			break;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("JComSeqTable.update o = "+o.getClass().getName());
		System.out.println("                   arg= "+arg);
		
		if (arg == null) {
			// Tout a changé
			updateComBox();
		}
		
	}

	void updateComBox() {
		// Maj ComboBox
		for (int i = 0; i < _comBox.length; i++) {
			_comBox[i].setText(_consignes.get(i).getName());
		}
		
		_comChoice.removeAllItems();
		// Maj des choix de consignes
		for (int i = 0; i < _consignes.size(); i++) {
			_comChoice.addItem(_consignes.get(i).getName());
		}
		_comChoice.setSelectedIndex(0);
		
	}
	
	
}
