package com.trippin.androidtrippin.model;

/**
 * Created by EyalEngel on 24/05/15.
 */
public enum eTripType
{
    SIGHTSEEING("Sightseeing"), ROMANTIC("Romantic"), NATURE("Nature"),
    BUSINESS("Business"), BEACHES("Beaches/Surfing"), EXTREME("Extreme");

    private final String value;

    private eTripType(String str) {
        this.value = str;
    }

    public String strValue() {
        return this.value;
    }

    public static eTripType fromStringToEnum(String typeStr)
    {
        eTripType retVal = null;

        for (eTripType type : eTripType.values()) {
            if (type.strValue().equals(typeStr)) {
                retVal = type;
                break;
            }
        }

        return retVal;
    }
}
