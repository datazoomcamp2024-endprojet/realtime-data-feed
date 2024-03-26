package org.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBtcEvent {

    public OrderBtcEvent() {
    }

    public String side;
    public String time;
    public String size;
    public String price;
    public long trade_id;

    @Override
    public String toString() {
        return String.format("side %s time %s size %s price %s trade id", side, time, size, price, trade_id);
    }
}
