/**
 * 
 */
package viewer;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import model.Command;
import model.CommandSequence;

/**
 * Un spécialisation de TableModel pour pouvoir utiliser une JTable
 * pour afficher et modifier une 'CommandSequence'.
 * 
 * @author Alain.Dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class CommandSequenceTableModel
extends AbstractTableModel
implements TableModelListener {

	CommandSequence _data;
		
	/**
	 * Crée à partir d'une CommandSequence.
	 */
	public CommandSequenceTableModel(CommandSequence com) {
		this._data = com;
		addTableModelListener(this);
	}

	@Override
	public int getRowCount() {
		return _data.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	/**
	 * @return null si le numero de colonne n'est pas bon.
	 */
	public String getColumnName(int col) {
		if (col == 0) {
			return "Time";
		}
		else if (col == 1) {
			return "Val";
		}
		return null;
    }
	public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
	public boolean isCellEditable(int row, int col) {
		return true;
	}
    public void setValueAt(Object value, int row, int col) {
    	System.out.println("setValue r="+row+" c="+col);
    	
    	// doit trouver la bonne Command
    	Command comChanged = null;
    	int curIndex = 0;
    	for (Command com : _data) {
    		if (curIndex == row) {
    			comChanged = com;
    			break;
    		}
    		curIndex ++;
    	}
    	// Maj la Command
    	System.out.println("r="+row+" changing : "+comChanged.toStringP());
    	double time = comChanged.time;
    	double val = comChanged.val;
    	switch (col) {
		case 0: // Change time
			time = (double) value;
			break;
		case 1: //Change val
			val = (double) value;
			break;
		default:
			break;
		}
    	_data.changeCommand(comChanged, time, val);
    	System.out.println("New CommandSeq\n"+_data.toString());
    	
        fireTableDataChanged();
    }

	@Override
	/**
	 * @return null si les index ne sont pas bons.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int curIndex = 0;
		for (Command com : _data) {
			if (curIndex == rowIndex) {
				if (columnIndex == 0) {
					return com.time;
				}
				else if (columnIndex == 1) {
					return com.val;
				}
				break;
			}
			curIndex ++;
		}
		return null;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		System.out.println("TM changed");
	}

}
