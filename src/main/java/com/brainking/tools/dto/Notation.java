package com.brainking.tools.dto;

public class Notation {

    private String pgnCode;
    private int moveNumber;
    private String prefix;
    private String symbol;
    private String suffix;
    private String result;

    public String getPgnCode() {
        return pgnCode;
    }

    public void setPgnCode(String pgnCode) {
        this.pgnCode = pgnCode;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
