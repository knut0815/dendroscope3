/**
 * Copyright 2015, Daniel Huson
 *
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package dendroscope.commands;

import dendroscope.main.Dendroscope;
import dendroscope.window.MultiViewer;
import dendroscope.window.TreeViewer;
import jloda.export.TransferableGraphic;
import jloda.gui.commands.ICommand;
import jloda.util.Alert;
import jloda.util.ResourceManager;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * copy command
 * Daniel Huson, 7.2010
 */
public class CopyImageCommand extends CommandBaseMultiViewer implements ICommand {
    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Copy Image";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Copy the current data as an image";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Copy16.gif");
    }

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */
    public KeyStroke getAcceleratorKey() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK);
    }

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws java.io.IOException
     */
    @Override
    public void apply(NexusStreamParser np) throws Exception {
        np.matchIgnoreCase(getSyntax());

        if (!Dendroscope.getApplication().go(null, false, false)) {
            new Alert(getViewer().getFrame(), "Unavailable: please register program to unlock");
            return;
        }

        if (multiViewer.getTreeGrid().getNumberSelectedOrAllViewers() == 1) {
            TreeViewer treeViewer = multiViewer.getTreeGrid().getSelectedOrAllIterator().next();
            TransferableGraphic tg = new TransferableGraphic(treeViewer, treeViewer.getScrollPane());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tg, tg);
        } else // show the whole window
        {
            TransferableGraphic tg = new TransferableGraphic(multiViewer.getTreeGrid(), null);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tg, tg);
        }
    }

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    public boolean isCritical() {
        return false;
    }

    /**
     * get command-line usage description
     *
     * @return usage
     */
    @Override
    public String getSyntax() {
        return "copy what=image;";
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        return ((MultiViewer) getViewer()).getDir().getDocument().getNumberOfTrees() > 0;
    }

    /**
     * action to be performed
     *
     * @param ev
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        execute(getSyntax());
    }
}