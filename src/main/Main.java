package main;

import model.AbstractModel;
import model.integerprogramming.IPModel;

import java.io.IOException;

public class Main {

    static {
        com.google.ortools.Loader.loadNativeLibraries();
    }

    public static void main(String[] args){
        AbstractModel.execute(new IPModel(),1,Config.kArray[0], Config.alphaArray[0],"test");
    }
}
