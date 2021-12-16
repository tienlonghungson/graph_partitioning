package model.evolution.ga;

import model.evolution.Individual;
import service.Pair;
import service.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GAIndividual extends Individual {
    static {
        numObjective=0;
    }

    /**
     * nParts : number of partitions
     */
    protected static int nParts;
    /**
     * alpha : bound of different among partitions
     */
    protected static  int alpha;
    /**
     * nGen : number of genes
     */
    protected static int nGen;
    /**
     * totalWeight : total of weight in graph
     */
    protected static double totalWeight;
    /**
     * weighted matrix of graph
     */
    protected static List<List<Double>> weightedMatrix;

    /**
     * weight for objective (fitness)
     */
    protected static final int W=800;

    protected static final double R_MUTATE1=0.06, R_MUTATE2=0.08, R_MUTATE3=0.1;
    protected int[] genes;
    private double cutWeight;
    protected String status;
    /**
     * constructor
     * @param isAutoGen whether this individual is auto generated or not (offspring, mutated)
     */
    public GAIndividual(boolean isAutoGen){
        super(nGen);
        this.genes = new int[nGen];

        if (isAutoGen) {
            Random rd = new Random();
            for (int i=0;i<nGen;++i){
                this.genes[i]=rd.nextInt(nParts);
                // add inCost of partition containing i vertex
            }
            this.evaluate();
        }
    }

    protected int calViolation(List<List<Integer>> partitions, int nParts, int alpha){
        int vio = Utils.calViolationBetweenPartitions(partitions, nParts, alpha);
        status = vio>0?"INFEASIBLE":"FEASIBLE";
        return vio;
    }

    public void setGene(int idxGen, int idxPart){
        assert (idxGen<=N_GEN);
        this.genes[idxGen]=idxPart;
    }

    protected double getCutWeight(){
        return cutWeight;
    }

    protected void evaluate(){
        List<List<Integer>> partitions = new ArrayList<>(nParts);
        for (int i = 0; i < nParts; ++i) {
            partitions.add(new ArrayList<>());
        }

        int idxPart;
        int inCost=0;
        for (int i=0;i<N_GEN;++i){
            idxPart= this.genes[i];
            for (int vertexInPartContainI : partitions.get(idxPart)){
                inCost+= GAIndividual.weightedMatrix.get(vertexInPartContainI).get(i);
            }
            partitions.get(idxPart).add(i);
        }
        this.cutWeight = GAIndividual.totalWeight-inCost;
        setFitness(cutWeight+GAIndividual.W*calViolation(partitions, nParts, alpha));
    }

    public Pair<Individual, Individual> onePointCrossOver(GAIndividual other){
        Random rd = new Random();
        int point = rd.nextInt((N_GEN-1))+1;

        GAIndividual child1 = new GAIndividual(false);
        GAIndividual child2 = new GAIndividual(false);

        int idxPart1, idxPart2;
        for (int i=0;i<point;++i){
            idxPart1 = this.genes[i];
            idxPart2 = other.genes[i];
            child1.setGene(i,idxPart1);
            child2.setGene(i,idxPart2);
        }

        for (int i=point;i<N_GEN;++i){
            idxPart1 = other.genes[i];
            idxPart2 = this.genes[i];
            child1.setGene(i,idxPart1);
            child2.setGene(i,idxPart2);
        }
        child1.evaluate();
        child2.evaluate();
        return new Pair<>(child1,child2);
    }

    protected void mutate(double rMutate){
        if(rMutate<GAIndividual.R_MUTATE1){
            this.onePointMutate();
        } else if (rMutate<GAIndividual.R_MUTATE2){
            this.reversedMutation();
        } else if (rMutate< GAIndividual.R_MUTATE3){
            this.swapHalfMutation();
        } else {
            return;
        }
        evaluate();
    }

    private void onePointMutate(){
        Random rd = new Random();
        int mutatePoint = rd.nextInt(N_GEN);
        int mutateValue = rd.nextInt(GAIndividual.nParts);
        this.genes[mutatePoint]=mutateValue;
    }

    private void reversedMutation(){
        for (int i=0;i<N_GEN;++i){
            genes[i]=genes[i]^genes[N_GEN-1-i];
            genes[N_GEN-1-i]=genes[i]^genes[N_GEN-1-i];
            genes[i]=genes[i]^genes[N_GEN-1-i];
        }
    }

    private void swapHalfMutation(){
        final int DIS = (N_GEN-1)>>1;
        for (int i=0;i<DIS;++i){
            genes[i]=genes[i]^genes[i+DIS];
            genes[N_GEN-1-i]=genes[i]^genes[i+DIS];
            genes[i]=genes[i]^genes[i+DIS];
        }
    }

    @Override
    public boolean isDominated(Individual other) {
        return this.getFitness()>other.getFitness();
    }

    @Override
    public List<List<Integer>> toPartitionList() {
        List<List<Integer>> partitions = new ArrayList<>(nParts);
        for (int i=0;i<nParts;++i){
            partitions.add(new ArrayList<>());
        }
        for (int i=0;i<N_GEN;++i){
            partitions.get(genes[i]).add(i);
        }
        return partitions;
    }

    @Override
    public String toString() {
        return "GAIndividual{" +
                "objectives=" + Arrays.toString(objectives) +
                ", cutWeight=" + cutWeight +
                '}';
    }
}
