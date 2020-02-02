package Shared;

import java.io.Serializable;

public class Place implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String postalCode;
    private final String locality;

    public Place(String postalCode, String locality) {
        super();
        this.postalCode = postalCode;
        this.locality = locality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getLocality() {
        return locality;
    }
}
