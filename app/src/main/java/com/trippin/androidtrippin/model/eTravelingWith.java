package com.trippin.androidtrippin.model;

/**
 * Created by EyalEngel on 24/05/15.
 */
public enum eTravelingWith
{

    ALONE("Alone"), COUPLE("Couple"), FAMILY("Family"), FRIENDS("Friends");

    private final String value;

    private eTravelingWith(String str) {
        this.value = str;
    }

    public String strValue() {
        return this.value;
    }

    public static eTravelingWith fromStringToEnum(String withStr) {
        eTravelingWith retVal = null;

        for (eTravelingWith with : eTravelingWith.values()) {
            if (with.strValue().equals(withStr)) {
                retVal = with;
                break;
            }
        }

        return retVal;
    }
}