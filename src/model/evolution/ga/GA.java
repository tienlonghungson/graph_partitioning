package model.evolution.ga;

import model.AbstractModel;
import model.evolution.Individual;
import service.Utils;

import java.awt.*;
import java.util.List;

public class GA extends AbstractModel {
    private static int NUM_GENERATION, NUM_INDIVIDUAL;
    private static void setNumGeneration(int numVer){
        final int factor = numVer/36;
        NUM_GENERATION = (factor+1)*300;
        NUM_INDIVIDUAL = (factor+1)*100;
    }

    public GA(){
        this.modelName = "GA";
    }
    @Override
    protected void solve(int k, int alpha) {
        // setup parameter
        List<List<Double>> weightedMatrix = inputInterface.getWeightedMatrix();
        GAIndividual.totalWeight = Utils.calTotalWeight(weightedMatrix);
        GAIndividual.weightedMatrix = weightedMatrix;
        GAIndividual.nParts=k;
        GAIndividual.alpha=alpha;
        GAIndividual.nGen = weightedMatrix.size();

        setNumGeneration(inputInterface.getNumVertices());
        GAPopulation population = new GAPopulation(NUM_INDIVIDUAL);

        int it=0;
        while ((it<NUM_GENERATION)&&(!isTimeUp)) {
            it++;
            population.evolutePopulation();
            population.selection();
        }

//        System.out.println(population.getPopulation().toString());
        GAIndividual best = population.getBest();
        this.bestPartitions = best.toPartitionList();
        status = best.status;
        weighted = best.getCutWeight();
    }
}
