package com.alluvialtrading.misc;

public class Toy {
    
    public static void main(String[] args) {
        int maxMod = 0;
        for (int i=0;i<1000;i++) {
            for (int j=i;j<1000;j++) {
                if ((i * j) % 13 == 0 && i % 7 == 0) maxMod = i * j;
            }
        }
        System.out.println(maxMod);
    }
}
