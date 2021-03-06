/*
 *   SiblingMemory.java Copyright (C) 2020 Daniel H. Huson
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

import jloda.graph.Node;

import java.util.*;

/**
 * @author Beckson
 */
public class SiblingMemory {

    private final Vector<String> taxaOrdering;
    private final Vector<String> taxonLabels;

    private final Hashtable<BitSet, HashSet<BitSet>> treeSetToForestSet = new Hashtable<>();
    private final Hashtable<String, Vector<String>> taxontToTaxa = new Hashtable<>();

    @SuppressWarnings("unchecked")
    public SiblingMemory(Vector<String> taxaOrdering) {
        this.taxaOrdering = taxaOrdering;
        this.taxonLabels = (Vector<String>) taxaOrdering.clone();
    }

    public SiblingMemory(HybridTree t, Vector<HybridTree> forest) {
        this.taxaOrdering = t.getTaxaOrdering();

        taxonLabels = new Vector<>();
        for (Node v : t.computeSetOfLeaves())
            taxonLabels.add(t.getLabel(v));
    }

    public BitSet getForestSet(Vector<HybridTree> forest) {
        Vector<BitSet> forestSets = new Vector<>();
        for (HybridTree t : forest) {
            BitSet b = new BitSet(taxaOrdering.size());
            for (Node v : t.computeSetOfLeaves()) {
                if (taxontToTaxa.containsKey(t.getLabel(v))) {
                    for (String s : taxontToTaxa.get(t.getLabel(v)))
                        b.set(taxaOrdering.indexOf(s));
                } else
                    b.set(taxaOrdering.indexOf(t.getLabel(v)));
            }
            forestSets.add(b);
        }
        Collections.sort(forestSets, new FirstBitComparator());
        BitSet b = new BitSet(forestSets.size() * taxaOrdering.size());
        for (BitSet f : forestSets) {
            int bitIndex = f.nextSetBit(0);
            while (bitIndex != -1) {
                b.set(bitIndex + forestSets.indexOf(f) * taxaOrdering.size());
                bitIndex = f.nextSetBit(bitIndex + 1);
            }
        }
        return b;
    }

    public BitSet getTreeSet(HybridTree t) {
        BitSet b = new BitSet(taxonLabels.size());
        for (Node v : t.computeSetOfLeaves()) {
            String label = t.getLabel(v);
            b.set(taxonLabels.indexOf(label));
        }
        return b;
    }

    public boolean contains(HybridTree t, Vector<HybridTree> forest) {

        BitSet treeSet = getTreeSet(t);
        BitSet forestSet = getForestSet(forest);

        if (treeSetToForestSet.containsKey(treeSet)) {
            for (BitSet b : treeSetToForestSet.get(treeSet)) {
                if (b.size() > forestSet.size()) {
                    if (b.equals(forestSet))
                        return true;
                } else if (forestSet.equals(b))
                    return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public void addEntry(HybridTree t, Vector<HybridTree> forest) {

        BitSet treeSet = getTreeSet(t);
        BitSet forestSet = getForestSet(forest);

        if (treeSetToForestSet.containsKey(treeSet)) {
            HashSet<BitSet> set = (HashSet<BitSet>) treeSetToForestSet.get(
                    treeSet).clone();
            set.add(forestSet);
            treeSetToForestSet.remove(treeSet);
            treeSetToForestSet.put(treeSet, set);
        } else {
            HashSet<BitSet> set = new HashSet<>();
            set.add(forestSet);
            treeSetToForestSet.put(treeSet, set);
        }

    }

    public void addTreeLabel(String label) {
        if (!taxonLabels.contains(label))
            taxonLabels.add(label);
    }

    public void addTaxon(String taxon, Vector<String> taxa) {
        Vector<String> v = new Vector<>();
        if (taxontToTaxa.containsKey(taxa.get(0))) {
            for (String s : taxontToTaxa.get(taxa.get(0)))
                v.add(s);
        } else
            v.add(taxa.get(0));
        if (taxontToTaxa.containsKey(taxa.get(1))) {
            for (String s : taxontToTaxa.get(taxa.get(1)))
                v.add(s);
        } else
            v.add(taxa.get(1));
        taxontToTaxa.put(taxon, v);
    }

    public boolean hasSameLeafSet(HybridTree t1, HybridTree t2) {
        if (t1.computeSetOfLeaves().size() == t2.computeSetOfLeaves().size()) {
            BitSet b1 = getTreeSet(t1);
            BitSet b2 = getTreeSet(t2);
            if (b1.equals(b2))
                return true;
        }
        return false;
    }

    public Hashtable<String, Vector<String>> getTaxontToTaxa() {
        return taxontToTaxa;
    }

    public boolean compareTaxa(String s1, String s2) {
        if (taxonLabels.contains(s1) && taxonLabels.contains(s2)) {

            BitSet b1 = new BitSet(taxonLabels.size());
            b1.set(taxonLabels.indexOf(s1));

            BitSet b2 = new BitSet(taxonLabels.size());
            b2.set(taxonLabels.indexOf(s2));

            if (b1.equals(b2))
                return true;
        }
        return false;
    }

}
