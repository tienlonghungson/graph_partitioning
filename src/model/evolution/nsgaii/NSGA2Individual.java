package model.evolution.nsgaii;

import model.evolution.Individual;
import service.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class NSGA2Individual extends Individual {
    protected static int nParts, alpha;
    protected static List<List<Double>> weightedMatrix;
    protected static int SN_GEN;
    protected static double uCoupling;
    protected static int uLoadBal;

    protected int[] genes;

    /**
     * numDominated : the number of individual dominating this individual
     * dominatedList : the list of individual dominated by this individual
     * rank : front index
     */
    private int numDominated=0;
    private final List<NSGA2Individual> dominatedList = new ArrayList<>();
    private int rank;

    public NSGA2Individual() {
        super(SN_GEN);

        genes= new int[SN_GEN];
        Random rd = new Random();
        for (int i=0;i<N_GEN;++i){
            genes[i] = rd.nextInt(nParts);
        }
    }

    public int getGene(int idx){
        assert (idx<N_GEN);
        return genes[idx];
    }

    public void setGene(int idx, int gene){
        assert (idx<N_GEN);
        this.genes[idx]= gene;
    }

    protected int getNumDominated() {
        return numDominated;
    }
    protected void updateNumDominated(int add){
        numDominated+=add;
    }
    protected List<NSGA2Individual> getDominatedList() {
        return dominatedList;
    }
    protected int getRank() {
        return rank;
    }
    protected void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public List<List<Integer>> toPartitionList() {
        List<List<Integer>> partitions = new ArrayList<>(nParts);
        for (int i=0;i<nParts;++i){
            partitions.add(new ArrayList<>());
        }
        for (int i=0;i<SN_GEN;++i){
            partitions.get(genes[i]).add(i);
        }
        return partitions;
    }

    public Pair<Double,Integer> getCutWeightAndMaxViolation(){
        List<List<Integer>> parts = this.toPartitionList();

        double cutWeight=0;
        int maxViolation = 0;
        for (int i=0;i<nParts-1;++i){
            for (int ver: parts.get(i)){
                for (int j=i+1;j<nParts;++j){
                    for (int endVer: parts.get(j)){
                        cutWeight+=weightedMatrix.get(ver).get(endVer);
                    }
                }
            }
            for (int j=i+1;j<nParts;++j){
                maxViolation= Math.max(maxViolation,
                        Math.abs(
                                Math.abs((parts.get(i).size()-parts.get(j).size()))
                                        -alpha));
            }
        }
        return new Pair<>(cutWeight,maxViolation);
    }

    /**
     * objective[0] stores (U_coupling - cutWeight)
     * objective[1] stores (U_balance - violation)
     */
    public void setObjectives(){
        Pair<Double,Integer> pair = getCutWeightAndMaxViolation();
        objectives[0]=Math.max(uCoupling-pair.first(),0);
        objectives[1]=Math.max(uLoadBal-pair.second(),0);
    }

    /**
     * crowding comparison
     * @param other compared individual
     * @return {$code true} if this individual > compared individual
     */
    protected boolean isCrowding(NSGA2Individual other){
        return (this.getRank()<other.getRank()) ||
                (this.getRank()==other.getRank() && this.getFitness()>other.getFitness());
    }

    protected Pair<NSGA2Individual,NSGA2Individual> twoPointCrossover(NSGA2Individual other){
        int genesLength = SN_GEN;
        Random rd = new Random();
        int firstPoint = rd.nextInt((genesLength-2))+1;
        int secondPoint = rd.nextInt((genesLength-firstPoint-1))+firstPoint+1;

        NSGA2Individual children1= new NSGA2Individual();
        NSGA2Individual children2= new NSGA2Individual();

        for (int i = 0; i < firstPoint; i++) {
            children1.setGene(i,this.getGene(i));
            children2.setGene(i, other.getGene(i));
        }
        for (int i = firstPoint; i < secondPoint; i++) {
            children1.setGene(i,other.getGene(i));
            children2.setGene(i,this.getGene(i));
        }
        for (int i = secondPoint; i < genesLength; i++) {
            children1.setGene(i, this.getGene(i));
            children2.setGene(i, other.getGene(i));
        }

        return new Pair<>(children1,children2);
    }

    protected Pair<NSGA2Individual,NSGA2Individual> onePointCrossover(NSGA2Individual parent2){
        int geneLength = SN_GEN;
        Random rd = new Random();
        int point = rd.nextInt((geneLength-1))+1;

        NSGA2Individual children1 = new NSGA2Individual();
        NSGA2Individual children2 = new NSGA2Individual();

        for (int i = 0; i < point; i++) {
            children1.setGene(i,this.getGene(i));
            children2.setGene(i, parent2.getGene(i));
        }
        for (int i = point; i < geneLength; i++) {
            children1.setGene(i,parent2.getGene(i));
            children2.setGene(i,this.getGene(i));
        }

        return new Pair<>(children1,children2);
    }

    protected void reversedMutation(){
        final int LEN = SN_GEN;
        for (int i=0;i<LEN;++i){
            genes[i]=genes[i]^genes[LEN-1-i];
            genes[LEN-1-i]=genes[i]^genes[LEN-1-i];
            genes[i]=genes[i]^genes[LEN-1-i];
        }
    }

    protected void swapHalfMutation(){
        final int LEN = SN_GEN;
        final int DIS = (LEN-1)>>1;
        for (int i=0;i<DIS;++i){
            genes[i]=genes[i]^genes[i+DIS];
            genes[LEN-1-i]=genes[i]^genes[i+DIS];
            genes[i]=genes[i]^genes[i+DIS];
        }
    }
}
