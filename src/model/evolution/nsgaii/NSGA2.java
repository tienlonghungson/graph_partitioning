package model.evolution.nsgaii;

import model.AbstractModel;
import model.evolution.Individual;
import service.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NSGA2 extends AbstractModel {

    private static final int POP_SIZE=20;
    private static final float MUTATION_RATE=0.1f;
    private static final int[] ITERATION= new int[]{100,200,300};
    private static int NUM_GENERATION;
    private static int kWay = 2;
    private final Random rd = new Random();

    public NSGA2(){
        this.modelName = "NSGA2";
    }

    private static void setNumGeneration(int numVer){
        if (numVer<=35) {
            NUM_GENERATION=ITERATION[0];
        } else if (numVer<=70){
            NUM_GENERATION=ITERATION[1];
        } else {
            NUM_GENERATION=ITERATION[2];
        }
    }

    @Override
    protected void solve(int k, int alpha) {
        // setup parameter
        List<List<Double>> weightedMatrix = inputInterface.getWeightedMatrix();
        NSGA2Individual.weightedMatrix = weightedMatrix;
        NSGA2Individual.nParts=k;
        NSGA2Individual.alpha=alpha;
        NSGA2Individual.SN_GEN = weightedMatrix.size(); setNumGeneration(NSGA2Individual.SN_GEN);
        Individual.numObjective=2;
        
        NSGA2Population population = initPopulation(POP_SIZE);
        population.firstEval();

        int iter=0;
        while(iter<NUM_GENERATION && !isTimeUp){
            population.select();
            crossOverPopulation(population);
            population.eval();
            iter++;
        }

        NSGA2Individual individual = (NSGA2Individual) population.fronts.get(0).get(0);
        this.bestPartitions=individual.toPartitionList();
        this.weighted=individual.getObjective(0);
        this.weighted=(this.weighted!=0)?this.weighted:Double.POSITIVE_INFINITY;
        // if 2nd objective of this individual == uLoadBal, then the violation is 0 => FEASIBLE
        this.status = (individual.getObjective(1)==NSGA2Individual.uLoadBal)?"FEASIBLE":"INFEASIBLE";

    }

    public NSGA2Population initPopulation(int popSize) {
        return new NSGA2Population(popSize);
    }

//    public void selectPopulation(NSGA2Population population){
//        population.select();
//    }

    public void crossOverPopulation(NSGA2Population population){
        int iter = POP_SIZE>>1;
        List<Individual> pop = population.getPopulation();
        float mutateDecision;
        for (int i=0;i<iter;++i){
            NSGA2Individual parent1 = tournamentSelection(selectRandomKTour(pop));
            NSGA2Individual parent2 = tournamentSelection(selectRandomKTour(pop));
            NSGA2Individual children1, children2;
//            twoPointCrossover(parent1,parent2,children1,children2);
            Pair<NSGA2Individual,NSGA2Individual> pair = parent1.twoPointCrossover(parent2);
            children1 = pair.first(); children2 = pair.second();

            mutateDecision=rd.nextFloat();
            if (mutateDecision<MUTATION_RATE/2){
//                children1.setGene(rd.nextInt(children1.getChromosomeLength()),rd.nextInt(children1.getMaxValue()));
                children1.reversedMutation();
            } else if (mutateDecision<MUTATION_RATE){
//                children2.setGene(rd.nextInt(children2.getChromosomeLength()),rd.nextInt(children2.getMaxValue()));
                children2.swapHalfMutation();
            }
            pop.add(children1); pop.add(children2);
        }
    }

    private NSGA2Individual[] selectRandomKTour(List<Individual> nsga2IndividualList){
        NSGA2Individual[] nsga2Individuals = new NSGA2Individual[kWay];
        ArrayList<Integer> idxes = new ArrayList<>(POP_SIZE);
        for (int i = 0; i < POP_SIZE; i++) {
            idxes.add(i);
        }
        Collections.shuffle(idxes);
        for (int i = 0; i < kWay; i++) {
            nsga2Individuals[i]=(NSGA2Individual) nsga2IndividualList.get(idxes.get(i));
        }
        return nsga2Individuals;
    }

    private NSGA2Individual tournamentSelection(NSGA2Individual[] kTour){
        NSGA2Individual bestInd = kTour[0];
        for (int i=1;i<kTour.length;++i){
            if (kTour[i].isCrowding(bestInd)){
                bestInd = kTour[i];
            }
        }
        return bestInd;
    }

}
