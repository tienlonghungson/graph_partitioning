package model.evolution.ga;

import model.evolution.Individual;
import model.evolution.Population;
import service.Pair;

import java.util.Collections;
import java.util.Random;

public class GAPopulation extends Population {
    private Random rd = new Random();
    public GAPopulation(int popSize) {
        super(popSize);
        for (int i=0;i<popSize;++i){
            this.population.add(new GAIndividual(true));
        }
    }

    public void evolutePopulation(){
        this.crossoverPopulation();
        this.mutatePopulation();
    }

    public void selection(){
        this.sortPopulation();
        final int LAST_IDX = this.population.size()-1;
        if (LAST_IDX >= POP_SIZE) {
            this.population.subList(POP_SIZE, LAST_IDX + 1).clear();
        }
    }

    private void sortPopulation(){
        this.population.sort(((o1, o2) -> o1.isDominated(o2)?1:(o2.isDominated(o1)?-1:0)));
    }

    private void crossoverPopulation(){
        int firstPar, secondPair;
        for (int i=0;i< (POP_SIZE>>1);i++){
            firstPar= tournamentSelection();
            secondPair=tournamentSelection();
            Pair<Individual,Individual> children= ((GAIndividual)this.population.get(firstPar)).
                    onePointCrossOver((GAIndividual)this.population.get(secondPair));
            this.population.add(children.first());
            this.population.add(children.second());
        }
    }

    private void mutatePopulation(){
        for (Individual individual:this.population){
            ((GAIndividual)individual).mutate(rd.nextFloat());
        }
    }

    private int tournamentSelection(){
        int first=rd.nextInt(GAIndividual.nGen), second;
        do{
            second = rd.nextInt(GAIndividual.nGen);
        }while (second==first);
        return this.population.get(first).isDominated(this.population.get(second))?second:first;
    }

    public GAIndividual getBest(){
        return (GAIndividual) this.population.get(0);
    }
}
