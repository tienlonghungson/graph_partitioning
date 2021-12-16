package main;

import model.AbstractModel;
import model.constraintprogramming.CPModel;
import model.evolution.ga.GA;
import model.evolution.nsgaii.NSGA2;
import model.integerprogramming.IPModel;
import model.tabu.TabuModel;
import service.Pair;
import service.Triplet;

import java.io.IOException;
import java.util.SplittableRandom;

public class Main {

    static {
        com.google.ortools.Loader.loadNativeLibraries();
    }

    public static void main(String[] args){
        // args[0]: modelName, args[1]:nRun, args[2]:data_size
        Triplet<AbstractModel, Integer, String> parsedArgs = parseArgument(args);
        if (parsedArgs==null) {
            return;
        }
        Pair<int[],int[]> problemParameter = Config.getParameter(args[2]);
        AbstractModel.execute(parsedArgs.first(),parsedArgs.second(), problemParameter.first(), problemParameter.second(),args[2]);
    }

    private static Triplet<AbstractModel,Integer,String> parseArgument(String[] args){
        if (args.length<3) {
            System.out.println("Not Enough Parameter");
            return null;
        }

        int nRun;
        try {
            nRun = Integer.parseInt(args[1]);
        } catch (NumberFormatException numberFormatException){
            nRun=1;
        }

        return switch (args[0]) {
            case "CP" -> new Triplet<>(new CPModel(), 1, args[2]);
            case "TabuSearch" -> new Triplet<>(new TabuModel(), nRun, args[2]);
            case "GA" -> new Triplet<>(new GA(),nRun,args[2]);
            case "NSGA2" -> new Triplet<>(new NSGA2(), nRun, args[2]);
            default -> new Triplet<>(new IPModel(), 1, args[2]);
        };
    }
}
