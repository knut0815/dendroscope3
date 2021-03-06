/*
 *   ProcWindow.java Copyright (C) 2020 Daniel H. Huson
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

/*
 * Copyright (C) This is third party code.
 */
package dendroscope.hybrid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProcWindow extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final JPanel panel = new JPanel();
    private final Controller controller;

    final JTextField number;

    public ProcWindow(Controller controller) {
        this.controller = controller;
        panel.setLayout(new FlowLayout());

        JLabel text = new JLabel("Available Cores: ");
        panel.add(text);

        number = new JTextField();
        number.setText("" + (Runtime.getRuntime().availableProcessors() - 1));
        number.setColumns(10);
        panel.add(number);

        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        panel.add(ok);
        add(panel);

        setMinimumSize(new Dimension(100, 0));
        setAlwaysOnTop(true);

    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            Integer num = Integer.parseInt(number.getText());
            controller.setCores(num);
            setVisible(false);
        } catch (Exception e) {
            number.setText("invalid number");
        }
    }

}
