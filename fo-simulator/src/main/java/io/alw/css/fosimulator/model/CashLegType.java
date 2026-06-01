package io.alw.css.fosimulator.model;

public enum CashLegType {
    FX_SIDE1("FX_Side1"),
    FX_SIDE2("FX_Side2"),
    MM_PRINCIPAL("MM_Principal"),
    MM_MATURITY("MM_Maturity"),
    MM_INTEREST("MM_Interest"),
    PARENT_CASHFLOW("Parent_Cashflow"),
    CHILD_CASHFLOW("Child_Cashflow");

    public String name;

    CashLegType(String name) {
        this.name = name;
    }

}
