package com.milaboratory.mixcr.postanalysis.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
interface OutputTableExtractor<K> {
    OutputTable getTable(CharacteristicGroupResult<K> result);

    static <K> OutputTableExtractor<K> summary() {
        return result -> {
            List<OutputTableCell> cells = new ArrayList<>();
            for (CharacteristicGroupResultCell<K> cell : result.cells)
                cells.add(new OutputTableCell(cell.datasetId, cell.key, cell.value));

            return new OutputTable(result.group.name,
                    new ArrayList<>(result.datasetIds),
                    new ArrayList<>(result.keys), cells);
        };
    }

    static <K> OutputTableExtractor<K> summary(Function<CharacteristicGroupResultCell<K>, Map<Object, Object>> columnsFunction) {
        return result -> {
            List<OutputTableCell> cells = new ArrayList<>();
            List<Object> columns = null;
            for (CharacteristicGroupResultCell<K> cell : result.cells) {
                Map<Object, Object> split = columnsFunction.apply(cell);
                if (columns == null)
                    columns = new ArrayList<>(split.keySet());
                for (Map.Entry<Object, Object> e : split.entrySet()) {
                    cells.add(new OutputTableCell(cell.datasetId, e.getKey(), e.getValue()));
                }
            }

            return new OutputTable(result.group.name,
                    new ArrayList<>(result.datasetIds),
                    columns, cells);
        };
    }
}
