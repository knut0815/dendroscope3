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

package dendroscope.commands.mul;

import dendroscope.commands.CommandBaseMultiViewer;
import dendroscope.core.Director;
import dendroscope.core.Document;
import dendroscope.core.TreeData;
import dendroscope.multnet.MultiLabeledTreeProcessor;
import dendroscope.window.MultiViewer;
import dendroscope.window.TreeViewer;
import jloda.gui.commands.ICommand;
import jloda.gui.director.IDirector;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * command Daniel Huson, 6.2010
 */
public class MUL2NetworkCommand extends CommandBaseMultiViewer implements ICommand {
    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Network for Multi-Labeled Tree...";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Compute a network from a multi-labeled tree";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return ResourceManager.getIcon("Empty16.gif");
    }

    /**
     * gets the accelerator key to be used in menu
     *
     * @return accelerator key
     */
    public KeyStroke getAcceleratorKey() {
        return null;
    }

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws java.io.IOException
     */
    @Override
    public void apply(NexusStreamParser np) throws Exception {

        np.matchIgnoreCase("compute mult2net method=");
        String method = np.getWordMatchesIgnoringCase("holm cluster levelk contracted");
        np.matchIgnoreCase(";");

        List<TreeData> list = new LinkedList<>();

        for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getSelectedOrAllIterator(); it.hasNext(); ) {
            TreeViewer treeViewer = it.next();
            if (treeViewer.getPhyloTree().getNumberOfNodes() > 0) {
                TreeData treeData = getDir().getDocument().getTree(multiViewer.getTreeGrid().getNumberOfViewerInDocument(treeViewer));
                TreeData result = MultiLabeledTreeProcessor.apply(treeData, method);
                result.setName(treeData.getName());
                list.add(result);
            }
        }

        Director newDir = Director.newProject(1, 1);

        MultiViewer newMultiViewer = (MultiViewer) newDir.getViewerByClass(MultiViewer.class);
        Document newDoc = newDir.getDocument();
        newDoc.setTitle(Basic.getFileBaseName(getDir().getDocument().getTitle()) + "-networks");
        BitSet which = new BitSet();
        TreeData[] trees = list.toArray(new TreeData[list.size()]);
        for (int i = 0; i < trees.length; i++) {
            newDoc.appendTree(trees[i].getName(), trees[i], i);
            // System.err.println("tree[" + i + "] in doc: " + newDoc.getTree(i).toBracketString());
            which.set(i);
        }

        newMultiViewer.loadTrees(which);
        newMultiViewer.chooseGridSize();

        newMultiViewer.updateView(IDirector.ALL);
        newMultiViewer.recomputeEmbedding();
        newMultiViewer.setMustRecomputeEmbedding(true);
        newMultiViewer.setMustRecomputeCoordinates(true);
        newMultiViewer.updateView(IDirector.ALL);
        newMultiViewer.getFrame().toFront();
    }

    /**
     * action to be performed
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {
        String[] choices = new String[]{"HOLM [Build exact network using Huber, Oxelman, Lott and Moulton (2006)]", "Cluster [Build cluster network from all clusters]", "LevelK [Build level-k network from all clusters]"};
        Object reply = JOptionPane.showInputDialog(getViewer().getFrame(), "Choose method to convert multi-labeled tree to network:", "Choose Method", JOptionPane.QUESTION_MESSAGE,
                ProgramProperties.getProgramIcon(), choices, choices[1]);
        if (reply != null)
            execute("compute mult2net method=" + reply.toString() + ";");


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
    @Override
    public String getSyntax() {
        return "compute mult2net method={HOLM|cluster|levelk};";
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        MultiViewer multiViewer = (MultiViewer) getViewer();
        for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getSelectedOrAllIterator(); it.hasNext(); ) {
            TreeViewer treeViewer = it.next();
            if (treeViewer.getPhyloTree().getSpecialEdges().size() > 0)
                return false;
        }
        return multiViewer.getTreeGrid().getSelectedOrAllIterator().hasNext();
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


