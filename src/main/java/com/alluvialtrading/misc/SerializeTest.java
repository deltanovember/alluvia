package com.alluvialtrading.misc;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class Pojo implements Serializable {
    String blah = "11111111111111111111aaaaaaaaaaaaaaaaaaaa" + Math.random();
}

public class SerializeTest {
    public static void main(String[] args) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        Pojo pojo = new Pojo();
        long start = 0;
        long end1 = 0;
        try {
            out = new ObjectOutputStream(bos);
            start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                out.writeObject(new Pojo());
            }

            end1 = System.currentTimeMillis();
            out.writeObject(new Pojo());

        } catch (Exception ex) {

        }
        long end2 = System.currentTimeMillis();
        System.out.println(start + " " + end1);
        System.out.println(end1 + " " + end2);

    }
}
