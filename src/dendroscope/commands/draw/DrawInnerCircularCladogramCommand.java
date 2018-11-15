/**
 * DrawInnerCircularCladogramCommand.java 
 * Copyright (C) 2018 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package dendroscope.commands.draw;

import dendroscope.window.TreeViewer;
import jloda.gui.commands.ICheckBoxCommand;
import jloda.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

public class DrawInnerCircularCladogramCommand extends DrawCommand implements ICheckBoxCommand {
    /**
     * get command-line syntax
     *
     * @return usage
     */
    public String getSyntax() {
        return "set drawer=" + TreeViewer.INNERCIRCULAR_CLADOGRAM + ";";
    }

    /**
     * this is currently selected?
     *
     * @return selected
     */
    public boolean isSelected() {
        for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getSelectedOrAllIterator(); it.hasNext(); ) {
            TreeViewer viewer = it.next();
            if (viewer.getDrawerKind().equals(TreeViewer.INNERCIRCULAR_CLADOGRAM))
                return true;
        }
        return false;
    }

    /**
     * get the name to be used as a menu label
     *
     * @return name
     */

    public String getName() {
        return "Draw Inner Circular Cladogram";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */

    public String getDescription() {
        return "Draw tree or network as inner circular cladogram";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */

    public ImageIcon getIcon() {
        return ResourceManager.getIcon("InnerCircularCladogram16.gif");
    }

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */

    public KeyStroke getAcceleratorKey() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    }

    /**
     * action to be performed
     *
     * @param ev
     */

    public void actionPerformed(ActionEvent ev) {
        execute("set drawer=" + TreeViewer.INNERCIRCULAR_CLADOGRAM + ";");
    }
}
