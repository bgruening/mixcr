/*
 * Copyright (c) 2014-2022, MiLaboratories Inc. All Rights Reserved
 *
 * Before downloading or accessing the software, please read carefully the
 * License Agreement available at:
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 *
 * By downloading or accessing the software, you accept and agree to be bound
 * by the terms of the License Agreement. If you do not want to agree to the terms
 * of the Licensing Agreement, you must not download or access the software.
 */
package com.milaboratory.mixcr.basictypes;

import cc.redberry.primitives.Filter;
import com.milaboratory.mixcr.assembler.CloneAssemblerParameters;
import com.milaboratory.mixcr.basictypes.tag.TagCount;
import com.milaboratory.mixcr.basictypes.tag.TagCountAggregator;
import com.milaboratory.mixcr.basictypes.tag.TagsInfo;
import com.milaboratory.mixcr.vdjaligners.VDJCAlignerParameters;
import io.repseq.core.GeneFeature;
import io.repseq.core.GeneType;
import io.repseq.core.VDJCGene;
import io.repseq.core.VDJCGeneId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by poslavsky on 10/07/14.
 */
public final class CloneSet implements Iterable<Clone>, MiXCRFileInfo, HasFeatureToAlign {
    String versionInfo;
    final MiXCRHeader header;
    final MiXCRFooter footer;
    final VDJCSProperties.CloneOrdering ordering;
    final List<VDJCGene> usedGenes;
    final List<Clone> clones;
    final double totalCount;
    private final TagCount totalTagCounts;
    /** Total number of unique tag combinations on each level */
    private final int[] tagDiversity;

    public CloneSet(List<Clone> clones, Collection<VDJCGene> usedGenes,
                    MiXCRHeader header, MiXCRFooter footer,
                    VDJCSProperties.CloneOrdering ordering) {
        ArrayList<Clone> list = new ArrayList<>(clones);
        list.sort(ordering.comparator());
        this.header = header;
        this.footer = footer;
        this.ordering = ordering;
        this.usedGenes = Collections.unmodifiableList(new ArrayList<>(usedGenes));
        this.clones = Collections.unmodifiableList(list);

        long totalCount = 0;
        int[] tagDiversity = new int[header.getTagsInfo().size() + 1];
        TagCountAggregator tagCountAggregator = new TagCountAggregator();
        for (Clone clone : clones) {
            totalCount += clone.count;
            clone.setParentCloneSet(this);
            if (clone.tagCount.depth() != header.getTagsInfo().size())
                throw new IllegalArgumentException("Conflict in tags info and clone tag counter.");
            tagCountAggregator.add(clone.tagCount);
            for (int d = 0; d < tagDiversity.length; d++)
                tagDiversity[d] += clone.getTagCount().getTagDiversity(d);
        }
        this.totalTagCounts = clones.size() == 0 ? null : tagCountAggregator.createAndDestroy();
        this.tagDiversity = tagDiversity;
        this.totalCount = totalCount;
    }

    /** To be used in tests only */
    public CloneSet(List<Clone> clones) {
        this.clones = Collections.unmodifiableList(new ArrayList<>(clones));
        long totalCount = 0;
        HashMap<VDJCGeneId, VDJCGene> genes = new HashMap<>();
        EnumMap<GeneType, GeneFeature> alignedFeatures = new EnumMap<>(GeneType.class);
        TagCountAggregator tagCountAggregator = new TagCountAggregator();
        for (Clone clone : clones) {
            totalCount += clone.count;
            tagCountAggregator.add(clone.tagCount);
            clone.setParentCloneSet(this);
            for (GeneType geneType : GeneType.values())
                for (VDJCHit hit : clone.getHits(geneType)) {
                    VDJCGene gene = hit.getGene();
                    genes.put(gene.getId(), gene);
                    GeneFeature alignedFeature = hit.getAlignedFeature();
                    GeneFeature f = alignedFeatures.put(geneType, alignedFeature);
                    if (f != null && !f.equals(alignedFeature))
                        throw new IllegalArgumentException("Different aligned feature for clones.");
                }
        }
        this.totalTagCounts = clones.size() == 0 ? null : tagCountAggregator.createAndDestroy();
        this.header = null;
        this.footer = null;
        this.tagDiversity = null;
        this.ordering = new VDJCSProperties.CloneOrdering();
        this.usedGenes = Collections.unmodifiableList(new ArrayList<>(genes.values()));
        this.totalCount = totalCount;
    }

    public List<Clone> getClones() {
        return clones;
    }

    public Clone get(int i) {
        return clones.get(i);
    }

    public int size() {
        return clones.size();
    }

    public boolean isHeaderAvailable() {
        return header != null;
    }

    @Override
    public MiXCRHeader getHeader() {
        return Objects.requireNonNull(header);
    }

    public boolean isFooterAvailable() {
        return footer != null;
    }

    @Override
    public MiXCRFooter getFooter() {
        return Objects.requireNonNull(footer);
    }

    public CloneSet withHeader(MiXCRHeader header) {
        return new CloneSet(clones, usedGenes, header, footer, ordering);
    }

    public CloneSet withFooter(MiXCRFooter footer) {
        return new CloneSet(clones, usedGenes, header, footer, ordering);
    }

    public GeneFeature[] getAssemblingFeatures() {
        return header.getAssemblerParameters().getAssemblingFeatures();
    }

    public CloneAssemblerParameters getAssemblerParameters() {
        return header.getAssemblerParameters();
    }

    public VDJCAlignerParameters getAlignmentParameters() {
        return header.getAlignerParameters();
    }

    public TagsInfo getTagsInfo() {
        return header.getTagsInfo();
    }

    public VDJCSProperties.CloneOrdering getOrdering() {
        return ordering;
    }

    public List<VDJCGene> getUsedGenes() {
        return usedGenes;
    }

    @Override
    public GeneFeature getFeatureToAlign(GeneType geneType) {
        return header.getAlignerParameters().getFeatureToAlign(geneType);
    }

    public double getTotalCount() {
        return totalCount;
    }

    public TagCount getTotalTagCounts() {
        return totalTagCounts;
    }

    public int getTagDiversity(int level) {
        return tagDiversity[level];
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    @Override
    public Iterator<Clone> iterator() {
        return clones.iterator();
    }

    /**
     * WARNING: current object (in) will be destroyed
     */
    public static CloneSet reorder(CloneSet in, VDJCSProperties.CloneOrdering newOrdering) {
        ArrayList<Clone> newClones = new ArrayList<>(in.clones);
        newClones.sort(newOrdering.comparator());
        for (Clone nc : newClones)
            nc.parent = null;
        return new CloneSet(newClones, in.usedGenes, in.header, in.footer, newOrdering);
    }

    /**
     * WARNING: current object (in) will be destroyed
     */
    public static CloneSet transform(CloneSet in, Filter<Clone> filter) {
        List<Clone> newClones = new ArrayList<>(in.size());
        for (int i = 0; i < in.size(); ++i) {
            Clone c = in.get(i);
            if (filter.accept(c)) {
                c.parent = null;
                newClones.add(c);
            }
        }
        return new CloneSet(newClones, in.usedGenes, in.header, in.footer, in.ordering);
    }

    /**
     * WARNING: current object (in) will be destroyed
     */
    public static <T> Map<T, CloneSet> split(CloneSet in, Function<Clone, T> splitter) {
        Map<T, List<Clone>> clonesMap = new HashMap<>();
        for (int i = 0; i < in.size(); ++i) {
            Clone c = in.get(i);
            T key = splitter.apply(c);
            List<Clone> cloneList = clonesMap.computeIfAbsent(key, __ -> new ArrayList<>());
            c.parent = null;
            cloneList.add(c);
        }

        return clonesMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new CloneSet(e.getValue(), in.usedGenes, in.header, in.footer, in.ordering)
                ));
    }
}
