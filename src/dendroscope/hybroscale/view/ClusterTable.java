/*
 *   ClusterTable.java Copyright (C) 2020 Daniel H. Huson
 *
 *   (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dendroscope.hybroscale.view;

import dendroscope.hybroscale.model.ClusterThread;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Hashtable;

public class ClusterTable extends JTable {

	private String detailsButtonName = "Details";
	private String abortButtonName = "Abort";

	private static final long serialVersionUID = 1L;
	private Hashtable<ClusterThread, Integer> threadToRow = new Hashtable<ClusterThread, Integer>();
	private Hashtable<Integer, ClusterThread> rowToThread = new Hashtable<Integer, ClusterThread>();
	private Hashtable<ClusterThread, JButton> threadToDetailsButton = new Hashtable<ClusterThread, JButton>();
	private Hashtable<JButton, InfoFrame> detailButtonToFrame = new Hashtable<JButton, InfoFrame>();
	private Hashtable<ClusterThread, JButton> threadToAbortButton = new Hashtable<ClusterThread, JButton>();

	private Object[] columnNames = { "Cluster ID", "Progress", "Status", "", "" };
	private Object[][] data;

	private DefaultTableModel tModel;
	private int row = 0;
	private int id = 1;

	// private int width = (int) (Toolkit.getDefaultToolkit().getScreenSize()
	// .getWidth() / 3);
	// private int height = (int) (Toolkit.getDefaultToolkit().getScreenSize()
	// .getHeight() / 6);

	private int width;

	@SuppressWarnings("serial")
	public ClusterTable(StatusBar statusBar, int width, HybridView hView) {

		tModel = new DefaultTableModel(data, columnNames);
		this.width = width;

		DefaultTableCellRenderer buttonRenderer = new DefaultTableCellRenderer() {

			public JButton getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
														 boolean hasFocus, int row, int column) {

				return (JButton) value;

			}

		};

		DefaultTableCellRenderer barRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			JProgressBar progressBar;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				int intValue = ((Integer) value).intValue();

				if (progressBar == null)
					progressBar = new JProgressBar();

				progressBar.setValue(intValue);
				progressBar.setBackground(table.getBackground());
				progressBar.setStringPainted(true);

				return progressBar;
			}
		};

		DefaultTableCellRenderer iconRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				if (value instanceof Icon) {
					setIcon((Icon) value);
					setText("");
				} else {
					setIcon(null);
					super.setValue(value);
				}
			}
		};

		setDefaultRenderer(JButton.class, buttonRenderer);
		setDefaultRenderer(Integer.class, barRenderer);
		setDefaultRenderer(String.class, iconRenderer);
		setDefaultRenderer(Icon.class, iconRenderer);

		iconRenderer.setHorizontalAlignment(SwingUtilities.CENTER);
		addMouseListener(new JTableButtonMouseListener(this, statusBar));

		setModel(tModel);

		getColumnModel().getColumn(0).setPreferredWidth(width / 6);
		getColumnModel().getColumn(1).setPreferredWidth(width / 6);
		getColumnModel().getColumn(2).setPreferredWidth(width / 3);
		getColumnModel().getColumn(3).setPreferredWidth(width / 6);
		getColumnModel().getColumn(4).setPreferredWidth(width / 6);

		setRowHeight(30);
		setEnabled(false);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 3 || columnIndex == 4) {
			return JButton.class;
		} else if (columnIndex == 1)
			return Integer.class;
		return String.class;
	}

	public void addClusterThread(ClusterThread cT) {

		String name = "Cluster " + id;
		String status = "Cluster in progress.";
		JButton details = new JButton(detailsButtonName);
		JButton abort = new JButton(abortButtonName);

		threadToRow.put(cT, row);
		rowToThread.put(row, cT);
		threadToAbortButton.put(cT, abort);
		threadToDetailsButton.put(cT, details);

		InfoFrame info = new InfoFrame("Details", "No details.");
		info.setLocationRelativeTo(this);;
		info.setTitle("Details");
		detailButtonToFrame.put(details, info);

		Object[] dataRow = { name, 0, status, details, abort };
		tModel.insertRow(row, dataRow);
		tModel.fireTableRowsInserted(0, row);

		getColumnModel().getColumn(0).setPreferredWidth(width / 6);
		getColumnModel().getColumn(1).setPreferredWidth(width / 6);
		getColumnModel().getColumn(2).setPreferredWidth(width / 3);
		getColumnModel().getColumn(3).setPreferredWidth(width / 6);
		getColumnModel().getColumn(4).setPreferredWidth(width / 6);

		id++;
		row++;
	}

	public void stopCluster(ClusterThread cT) {
		tModel.setValueAt("Canceled.", threadToRow.get(cT), 2);
		tModel.fireTableCellUpdated(threadToRow.get(cT), 2);
	}

	public void stopAllCluster() {
		for(ClusterThread cT : threadToRow.keySet())
			tModel.fireTableCellUpdated(threadToRow.get(cT), 2);
	}

	public void finishCluster(ClusterThread cT) {
		tModel.setValueAt("Done.", threadToRow.get(cT), 2);
		tModel.fireTableCellUpdated(threadToRow.get(cT), 2);
		threadToAbortButton.get(cT).setEnabled(false);
		repaint();
	}

	public void setStatus(ClusterThread cT, String info) {
		tModel.setValueAt(info, threadToRow.get(cT), 2);
		tModel.fireTableCellUpdated(threadToRow.get(cT), 2);
	}

	public void setProgress(ClusterThread cT, int i) {
		tModel.setValueAt(i, threadToRow.get(cT), 1);
		tModel.fireTableCellUpdated(threadToRow.get(cT), 1);
	}

	public synchronized void setDetails(ClusterThread thread, String details) {
		detailButtonToFrame.get(threadToDetailsButton.get(thread)).setInfo(details);
		detailButtonToFrame.get(threadToDetailsButton.get(thread)).repaint();
	}

	public ClusterThread getClusterThread(int row) {
		return rowToThread.get(row);
	}

	public JDialog getInfoFrame(JButton b) {
		return detailButtonToFrame.get(b);
	}

	public String getDetailsButtonName() {
		return detailsButtonName;
	}

	public String getAbortButtonName() {
		return abortButtonName;
	}

}
