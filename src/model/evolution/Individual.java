package model.evolution;

import java.util.LinkedList;
import java.util.List;

public abstract class Individual{
    /**
     * the default implementation of Individual is the array of objective store numObjective+1 elements
     * the last element is fitness value
     * for the implementation of single objective optimization, set numObjective = 0
     */
    public static int numObjective=2;
    public final int N_GEN;

    protected final double[] objectives;

    public Individual(int nGen){
        this.N_GEN = nGen;
        this.objectives = new double[numObjective +1];
    }

    public void setObjectives(int i, double value){
        assert (i<=numObjective);
        this.objectives[i]=value;
    }

    public double getObjective(int i){
        assert (i<=numObjective);
        return this.objectives[i];
    }

    public double getFitness(){
        return getObjective(numObjective);
    }

    public void setFitness(double fitness){
        setObjectives(numObjective,fitness);
    }

    /**
     * check if this individual is dominated by other individual
     * the default implementation is all objectives tend to maximized
     *
     * @param other Individual
     * @return {@code true} if this individual is dominated, {@code false} otherwise
     */
    public boolean isDominated(Individual other){
        boolean isStrict=false;
        for (int i=0;i<numObjective;++i){
            if (this.objectives[i]>other.objectives[i]){
                return false;
            } else if (this.objectives[i]<other.objectives[i]){
                isStrict = true;
            }
        }
        return isStrict;
    }

    public abstract List<List<Integer>> toPartitionList();
}
