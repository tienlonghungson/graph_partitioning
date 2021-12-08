package main;

import model.AbstractModel;
import model.constraintprogramming.CPModel;
import model.evolution.nsgaii.NSGA2;
import model.integerprogramming.IPModel;
import model.tabu.TabuModel;

import java.io.IOException;

public class Main {

    static {
        com.google.ortools.Loader.loadNativeLibraries();
    }

    public static void main(String[] args){
//        AbstractModel.execute(new TabuModel(),1,Config.kArray[0],Config.alphaArray[0],"test" );
//        AbstractModel.execute(new IPModel(),1,Config.kArray[0], Config.alphaArray[0],"test");
//        AbstractModel.execute(new CPModel(),1,Config.kArray[0], Config.alphaArray[0],"test");
        AbstractModel.execute(new NSGA2(),1,Config.kArray[0], Config.alphaArray[0],"test");
    }
}
