package com.blck.MusicReleaseTracker;

public enum MonthNumbers {
    JAN("01"),
    FEB("02"),
    MAR("03"),
    APR("04"),
    MAY("05"),
    JUN("06"),
    JUL("07"),
    AUG("08"),
    SEP("09"),
    OCT("10"),
    NOV("11"),
    DEC("12");


    private final String numberCode;

    MonthNumbers(String numberCode) {
        this.numberCode = numberCode;
    }

    public String getCode() {
        return this.numberCode;
    }

}
