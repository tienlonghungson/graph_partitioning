package main;

import service.Pair;

public class Config {
    public static int[][] kArray = new int[][] {{2,3,4},{4,5,6},{6,7,8}};
//    public static int[][] kArray = new int[][] {{2},{4,5,6},{6,7,8}};
    public static int[][] alphaArray = new int[][] {{1,2},{5,6},{10,11}};

    public static Pair<int[],int[]> getParameter(String dataSize){
        return switch (dataSize) {
            case "medium" -> new Pair<>(kArray[1],alphaArray[1]);
            case "large","huge" -> new Pair<>(kArray[2],alphaArray[2]);
            default -> new Pair<>(kArray[0],alphaArray[0]);
        };
    }

}
