package com.alluvialtrading.fix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OrderSide implements Serializable {
    static private Map<String, OrderSide> known = new HashMap<String, OrderSide>();
    static public final OrderSide BUY = new OrderSide("Buy");
    static public final OrderSide SELL = new OrderSide("Sell");
    static public final OrderSide SHORT_SELL = new OrderSide("Short Sell");
    static public final OrderSide SHORT_SELL_EXEMPT =
        new OrderSide("Short Sell Exempt");
    static public final OrderSide CROSS = new OrderSide("Cross");
    static public final OrderSide CROSS_SHORT = new OrderSide("Cross Short");
    static public final OrderSide CROSS_SHORT_EXEMPT =
        new OrderSide("Cross Short Exempt");

    static private OrderSide[] array =
        { BUY, SELL, SHORT_SELL, SHORT_SELL_EXEMPT,
          CROSS, CROSS_SHORT, CROSS_SHORT_EXEMPT };

    private String name;

    private OrderSide(String name) {
        this.name = name;
        synchronized(OrderSide.class) {
            known.put(name, this);
        }
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    static public Object[] toArray() {
        return array;
    }

    public static OrderSide parse(String type)
    throws IllegalArgumentException {
        OrderSide result = known.get(type);
        if(result == null) {
            throw new IllegalArgumentException
            ("OrderSide:  " + type + " is unknown.");
        }
        return result;
    }
}
