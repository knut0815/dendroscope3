/*
 *   RerootingView.java Copyright (C) 2020 Daniel H. Huson
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

package dendroscope.hybroscale.rerooting;

import dendroscope.hybroscale.util.graph.MyPhyloTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class RerootingView extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JButton ok, close;
	private JLabel infoLabel;
	private RerootByHNumber rerooter;
	private JProgressBar progressBar;

	private Object o;
	private String[] treeStrings;
	private int cores;

	public RerootingView(JFrame frame, Object o, String[] treeStrings, int cores) {

		this.o = o;
		this.treeStrings = treeStrings;
		this.cores = cores;

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		JLabel label = new JLabel("Max Hybridization Number: ", SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(250, 30));
		gbl.setConstraints(label, gbc);
		add(label);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(250, 30));
		gbl.setConstraints(textField, gbc);
		add(textField);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(500, 50));
		progressBar.setStringPainted(true);
		gbl.setConstraints(progressBar, gbc);
		add(progressBar);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		ok = new JButton("OK");
		ok.addActionListener(this);
		ok.setPreferredSize(new Dimension(250, 30));
		gbl.setConstraints(ok, gbc);
		add(ok);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		close = new JButton("Close");
		close.addActionListener(this);
		close.setPreferredSize(new Dimension(250, 30));
		gbl.setConstraints(close, gbc);
		add(close);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		JPanel infoPanel = new JPanel();
		infoLabel = new JLabel();
		infoPanel.add(infoLabel, SwingConstants.CENTER);
		infoPanel.setPreferredSize(new Dimension(500, 40));
		gbl.setConstraints(infoPanel, gbc);
		add(infoPanel);

		setTitle("Rerooting by Hybridization Number!");
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(frame);

		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(ok)) {
			close.setText("Cancel");
			ok.setEnabled(false);
			textField.setEditable(false);
			repaint();
			Integer upperBound = parseUpperBound();
			rerooter = new RerootByHNumber(this, treeStrings, cores, upperBound);
			rerooter.start();
		} else if (e.getSource().equals(close)) {
			if (rerooter != null)
				rerooter.stopComp();
			reportResult();
			setVisible(false);
		}

	}

	private Integer parseUpperBound() {
		try {
			return Integer.parseInt(textField.getText());
		} catch (Exception e) {

		}
		return null;
	}

	public void reportResult() {
		setVisible(false);
		synchronized (o) {
			o.notify();
		}
		showResult();
	}

	public void setProgress(int p) {
		if (progressBar != null) {
			progressBar.setValue(p);
			progressBar.setString(p + "%");
		}
	}

	public void setInfo(String info) {
		infoLabel.setText(info);
	}

	private void showResult() {
		if (rerooter != null)
			JOptionPane.showMessageDialog(this, rerooter.getBestTreeSets().size()
					+ " tree set(s) with hybridization number " + rerooter.getMinHNumber() + " computed!");
	}

	public Vector<MyPhyloTree[]> getBestTreeSets() {
		if (rerooter != null)
			return rerooter.getBestTreeSets();
		return null;
	}

}
