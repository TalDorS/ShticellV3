package ranges;

import api.Range;
import exceptions.engineexceptions.RangeAlreadyExistsException;
import exceptions.engineexceptions.RangeNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RangesManager {
    private final Map<String, Range> ranges; // Map to store ranges by their names

    public RangesManager() {
        this.ranges = new HashMap<>();
    }

    // Add a new range
    public void addRange(String rangeName, String firstCell, String lastCell) throws RangeAlreadyExistsException {
        if (ranges.containsKey(rangeName.toUpperCase())) {
            throw new RangeAlreadyExistsException("Range with the name '" + rangeName + "' already exists. Note that ranges are case-insensitive (e.g. rAngE equals to RANGE)");
        }

        Range range = new RangeImpl(rangeName, firstCell, lastCell);
        ranges.put(rangeName.toUpperCase(), range);
    }

    public void clearRanges() {
        ranges.clear();
    }

    // Delete a range
    public void deleteRange(String rangeName) throws RangeNotFoundException {
        if (!ranges.containsKey(rangeName)) {
            throw new RangeNotFoundException("Range with the name '" + rangeName + "' does not exist.");
        }

        ranges.remove(rangeName);
    }

    // Get all ranges
    public Map<String, Range> getAllRanges() {
        return ranges;
    }

    // Get a certain range
    public Range getRange(String rangeName) {
        return ranges.get(rangeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangesManager that = (RangesManager) o;
        return Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ranges);
    }
}
