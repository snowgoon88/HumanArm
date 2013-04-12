/**
 * 
 */
package viewer;

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
public class CommandSequenceTableModel extends AbstractTableModel {

	CommandSequence _data;
		
	/**
	 * Crée à partir d'une CommandSequence.
	 */
	public CommandSequenceTableModel(CommandSequence com) {
		this._data = com;
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

}
