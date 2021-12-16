package model.evolution;

import model.evolution.nsgaii.NSGA2Individual;

import java.util.ArrayList;
import java.util.List;

public class Population {
    protected final int POP_SIZE;
    protected List<Individual> population;

    public Population(int popSize){
        POP_SIZE = popSize;
        population = new ArrayList<>(popSize<<1);
    }

    public List<Individual> getPopulation(){
        return population;
    }
    public void setPopulation(List<Individual> population) {
        this.population = population;
    }
}
