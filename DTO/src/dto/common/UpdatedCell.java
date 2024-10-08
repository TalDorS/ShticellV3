package dto.common;

public class UpdatedCell {
    private String cellId;
    private Object value;

    public UpdatedCell(String cellId, Object value) {
        this.cellId = cellId;
        this.value = value;
    }

    public String getCellId() {
        return cellId;
    }

    public Object getValue() {
        return value;
    }
}