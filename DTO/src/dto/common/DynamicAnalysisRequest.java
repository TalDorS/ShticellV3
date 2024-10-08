package dto.common;

public class DynamicAnalysisRequest {
    private String cellId;
    private String userName;
    private String spreadsheetName;
    private double tempValue;

    public DynamicAnalysisRequest(String cellId, String userName, String spreadsheetName, double tempValue) {
        this.cellId = cellId;
        this.userName = userName;
        this.spreadsheetName = spreadsheetName;
        this.tempValue = tempValue;
    }
    // Getters and setters
    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public double getTempValue() {
        return tempValue;
    }

    public void setTempValue(double tempValue) {
        this.tempValue = tempValue;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSpreadsheetName() {
        return spreadsheetName;
    }

    public void setSpreadsheetName(String spreadsheetName) {
        this.spreadsheetName = spreadsheetName;
    }
}
