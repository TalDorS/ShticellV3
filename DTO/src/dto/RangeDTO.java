package dto;

import java.util.List;

public class RangeDTO {
    private final String name;
    private final String startCell;
    private final String endCell;
    private final List<String> cells;

    public RangeDTO(String name, String startCell, String endCell, List<String> cells) {
        this.name = name;
        this.startCell = startCell;
        this.endCell = endCell;
        this.cells = cells;
    }

    public String getName() {
        return name;
    }

    public String getStartCell() {
        return startCell;
    }

    public String getEndCell() {
        return endCell;
    }

    public List<String> getCells() {
        return cells;
    }

}
