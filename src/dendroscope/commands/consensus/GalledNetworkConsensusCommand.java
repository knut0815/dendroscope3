/**
 * GalledNetworkConsensusCommand.java 
 * Copyright (C) 2015 Daniel H. Huson
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
package dendroscope.commands.consensus;

import dendroscope.consensus.ComputeNetworkConsensus;
import dendroscope.window.MultiViewer;
import dendroscope.window.TreeViewer;
import jloda.gui.commands.CommandBase;
import jloda.gui.commands.ICommand;
import jloda.util.Alert;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.parse.NexusStreamParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class GalledNetworkConsensusCommand extends CommandBase implements ICommand {
    public String getSyntax() {
        return null;
    }

    public void actionPerformed(ActionEvent ev) {
        String result;

        if (((MultiViewer) getViewer()).getTreeGrid().getNumberSelectedOrAllViewers() <= 2)
            result = "0";
        else {
            double threshold = ProgramProperties.get("ConsensusThreshold", 20d);
            result = JOptionPane.showInputDialog(getViewer().getFrame(), "Enter percent threshold for network consensus construction", "" + threshold);
        }
        if (result != null) {
            float threshold = Basic.parseFloat(result);
            if (threshold >= 0) {
                ProgramProperties.put("ConsensusThreshold", threshold);
                execute("compute consensus method=" + ComputeNetworkConsensus.GALLED_NETWORK + " threshold='" + threshold + "';");
            } else
                new Alert(getViewer().getFrame(), "Number >=0 expected, got: '" + result + "'");
        }

    }

    public KeyStroke getAcceleratorKey() {
        return null;
    }

    public String getDescription() {
        return "Compute a consensus galled-network for the given trees";
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getName() {
        return "Galled Network Consensus...";
    }

    public String getUndo() {
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
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        MultiViewer multiViewer = (MultiViewer) getViewer();
        int count = 0;
        for (Iterator<TreeViewer> it = multiViewer.getTreeGrid().getSelectedOrAllIterator(); it.hasNext(); ) {
            TreeViewer treeViewer = it.next();
            if (treeViewer.getPhyloTree().getSpecialEdges().size() > 0)
                return false;
            count++;
        }
        return count > 1;
    }
}