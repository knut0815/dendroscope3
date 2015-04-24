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

import dendroscope.core.Document;
import dendroscope.io.IOFormat;
import dendroscope.io.IOManager;
import dendroscope.io.Newick;
import dendroscope.io.nexml.Nexml;
import dendroscope.main.DendroscopeProperties;
import dendroscope.window.TreeViewer;
import jloda.gui.commands.ICommand;
import jloda.util.Alert;
import jloda.util.ResourceManager;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * The save as command
 * Daniel Huson, 7.2010
 */
public class SaveCommand extends CommandBaseMultiViewer implements ICommand {
    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Save";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Save the current document in NeXML format";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Save16.gif");
    }

    /**
     * gets the accelerator key  to be used in menu
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
        try {
            np.matchIgnoreCase("save");
            String formatName = "nexml";
            if (np.peekMatchIgnoreCase("format")) {
                np.matchIgnoreCase("format=");
                formatName = np.getWordMatchesIgnoringCase("dendro newick nexus nexml newick-no-weights");
            }

            np.matchIgnoreCase("file=");
            String fileName = np.getWordFileNamePunctuation();
            np.matchIgnoreCase(";");
            File file = new File(fileName);

            Document doc = getDir().getDocument();

            doc.notifyTasks("Saving file", fileName);

            multiViewer.getTreeGrid().syncCurrentViewers2Document(getDir().getDocument(), true);

            IOFormat format = IOManager.createIOFormatForName(formatName);
            if (format instanceof Nexml) {
                ((Nexml) format).setConnectors(doc.getConnectors());
            }
            if (format instanceof Newick && formatName.equalsIgnoreCase("Newick-no-weights")) {
                ((Newick) format).setSaveEdgeWeights(false);
            }
            format.write(file, doc.getTrees());
            if (format instanceof Nexml) {
                doc.setFile(file);
                doc.setDocumentIsDirty(false);
                for (int i = 0; i < doc.getNumberOfTrees(); i++) {
                    doc.getTree(i).setDirty(false);
                }
                boolean changed = false;
                for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getIterator(); it.hasNext(); ) {
                    TreeViewer treeViewer = it.next();
                    if (treeViewer.isDirty()) {
                        treeViewer.setDirty(false);
                        multiViewer.getTreeGrid().updateName(treeViewer);
                        changed = true;
                    }
                }
                if (changed && multiViewer.getTreeGrid().getConnectors().size() > 0)
                    multiViewer.getTreeGrid().repaint(); // need to force redrawing of connectors
            }
            DendroscopeProperties.addRecentFile(file);
        } catch (IOException ex) {
            new Alert(getViewer().getFrame(), "Save failed: " + ex.getMessage());
            throw ex;
        }

    }

    /**
     * is this a critical command that can only be executed when no other command is running?
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
        return "save format=<name> file=<name>;";
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        Document doc = getDir().getDocument();
        return doc.getNumberOfTrees() > 0 && doc.getFile() != null && (new Nexml()).getFileFilter().accept(doc.getFile());
    }

    /**
     * action to be performed
     *
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Document doc = getDir().getDocument();
        File file = doc.getFile();
        execute("save format=" + Nexml.NAME + " file='" + file.getPath() + "';");
    }
}