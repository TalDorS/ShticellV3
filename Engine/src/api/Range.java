package api;

import java.util.List;

public interface Range {
    String getName();
    List<String> getCells(); // Returns the list of cell IDs in this range
    boolean containsCell(String cell); // Checks if a specific cell is part of the range
    String getStartCell();
    String getEndCell();
}
