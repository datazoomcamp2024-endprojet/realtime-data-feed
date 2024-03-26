package org.example.data;

import java.math.BigDecimal;

public class VolumeBtcTimeWindow {

    public String volume = "0";
    // buy or sell
    public String side;

    public VolumeBtcTimeWindow() {
    }

    public void addVolume(OrderBtcEvent value) {
        if (!value.side.equals(this.side))
            throw new RuntimeException("side must match");

        var price = new BigDecimal(value.price);
        var size = new BigDecimal(value.size);
        volume = new BigDecimal(volume).add(price.multiply(size)).toString();
    }

}
