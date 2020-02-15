/*
 *   RerootCommand.java Copyright (C) 2020 Daniel H. Huson
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
package dendroscope.commands;

import dendroscope.embed.LayoutOptimizerManager;
import dendroscope.util.RerootingUtils;
import dendroscope.window.MultiViewer;
import dendroscope.window.TreeViewer;
import jloda.phylo.PhyloTree;
import jloda.swing.commands.ICommand;
import jloda.swing.util.Alert;
import jloda.swing.util.ResourceManager;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Set;

/**
 * command Daniel Huson, 6.2010
 */
public class RerootCommand extends CommandBaseMultiViewer implements ICommand {
    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Reroot";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Reroot the tree or network using the selected node, edge or taxa";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return ResourceManager.getIcon("Reroot16.gif");
    }

    /**
     * gets the accelerator key to be used in menu
     *
     * @return accelerator key
     */
    public KeyStroke getAcceleratorKey() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK);
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

        boolean warned = false;
        for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getSelectedOrAllIterator(); it.hasNext(); ) {
            TreeViewer treeViewer = it.next();

            boolean changed = false;
            if (treeViewer.getSelectedNodeLabelsNotInternalNumbers().size() > 0) {
                PhyloTree tree = treeViewer.getPhyloTree();
                Set selectedLabels = treeViewer.getSelectedNodeLabels();
                if (tree.getSpecialEdges().size() > 0) {
                    if (!warned) {
                        warned = true;
                        new Alert(getViewer().getFrame(), "Reroot by outgroup: not implemented for networks");
                    }
                } else
                    changed = RerootingUtils.rerootByOutgroup(treeViewer, selectedLabels);
            } else {
                if (treeViewer.getNumberSelectedEdges() == 1)
                    changed = RerootingUtils.rerootByEdge(treeViewer, treeViewer.getSelectedEdges().getFirstElement());
                else if (treeViewer.getNumberSelectedNodes() == 1)
                    changed = RerootingUtils.rerootByNode(treeViewer, treeViewer.getSelectedNodes().getFirstElement());
            }
            if (changed) {
                treeViewer.getPhyloTree().getNode2GuideTreeChildren().clear();
                LayoutOptimizerManager.apply(multiViewer.getEmbedderName(), treeViewer.getPhyloTree());
                treeViewer.setDirty(true);
                treeViewer.recomputeEmbedding(true, true);
                treeViewer.resetLabelPositions(true);
                treeViewer.getScrollPane().invalidate();
                treeViewer.getScrollPane().repaint();
                getDir().getDocument().getTree(multiViewer.getTreeGrid().getNumberOfViewerInDocument(treeViewer)).syncViewer2Data(treeViewer, treeViewer.isDirty());
            }
        }

    }

    /**
     * action to be performed
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {
        execute(getSyntax());

    }

    /**
     * is this a critical command that can only be executed when no other
     * command is running?
     *
     * @return true, if critical
     */
    public boolean isCritical() {
        return true;
    }

    /**
     * get command-line usage description
     *
     * @return usage
     */
    public String getSyntax() {
        return "reroot;";
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
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    public String getUndo() {
        return null;
    }
}


