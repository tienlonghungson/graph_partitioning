package main;

import model.AbstractModel;
import model.constraintprogramming.CPModel;
import model.evolution.nsgaii.NSGA2;
import model.integerprogramming.IPModel;
import model.tabu.TabuModel;
import service.Pair;

import java.io.IOException;

public class Main {

    static {
        com.google.ortools.Loader.loadNativeLibraries();
    }

    public static void main(String[] args){
        // args[0]:nRun, args[1]:data_size
        int nRun = Integer.parseInt(args[0]);
        Pair<int[],int[]> problemParameter = Config.getParameter(args[1]);
        AbstractModel.execute(new TabuModel(),nRun,problemParameter.first(), problemParameter.second(),args[1]);
        AbstractModel.execute(new IPModel(),1,problemParameter.first(), problemParameter.second(),args[1]);
        AbstractModel.execute(new CPModel(),1,problemParameter.first(), problemParameter.second(),args[1]);
//        AbstractModel.execute(new NSGA2(),1,Config.kArray[0], Config.alphaArray[0],"test");
    }
}
