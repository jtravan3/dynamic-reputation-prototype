package com.jtravan.model;

public enum Category {
    HCHE(0),
    HCLE(1),
    LCHE(2),
    LCLE(3),
    NOT_APPLICABLE(99);

    private final int categoryNum;

    Category(int categoryNum) {
        this.categoryNum = categoryNum;
    }

    public int getCategoryNum() {
        return this.categoryNum;
    }

    public static Category getCategoryByCategoryNum(int operationNum) {

        if(operationNum == 0) {
            return HCHE;
        } else if(operationNum == 1) {
            return HCLE;
        } else if(operationNum == 2) {
            return LCHE;
        } else if(operationNum == 3) {
            return LCLE;
        } else if(operationNum == 99) {
            return NOT_APPLICABLE;
        } else {
            return null;
        }

    }

    public static boolean isCategory1HigherThanCategory2(Category category1, Category category2) {
        return category1.getCategoryNum() < category2.getCategoryNum();
    }

    public static boolean isCategory1HigherThanOrEqualCategory2(Category category1, Category category2) {
        return category1.getCategoryNum() <= category2.getCategoryNum();
    }
}
