package model.tabu;

import service.Triplet;
import service.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Solution implements Cloneable{
    protected static int k, alpha, W;
    protected static List<List<Double>> weightedMatrix;
    protected List<List<Integer>> partitions;
    protected double[] inCost;
    protected int violation;
    protected double obj;

    public Solution(List<List<Integer>> partitions){
        this.partitions = partitions;
        inCost = Utils.initInCostWeight(weightedMatrix,partitions);
        updateViolation();
    }

    public Solution(List<List<Integer>> partitions, double[] inCost,
                    int violation, int[][] violations,double obj){
        this(partitions);
        this.inCost = inCost;
        this.violation=violation;
        this.obj=obj;
    }
    
    protected void updateViolation(){
        violation = calViolation(partitions,k,alpha);
        obj = calObjective(inCost[inCost.length-1], violation);
    }

    /**
     * update solution by a new move
     * @param moveInfo contains information of the move. Datatype : a Triplet includes:
     *                 Triplet includes :
     *                    Integer: position of moved vertex in partition
     *                    Integer: idx of source partition
     *                    Integer: idx of moved vertex
     *                 Integer : idx of destination partition
     *                 Double : objective value of new neighbor solution
     */
    protected void update(Triplet<Triplet<Integer,Integer,Integer>,Integer, Double> moveInfo){
        moveVertexToNewPart(moveInfo.first().first(),
                partitions.get(moveInfo.first().second()),partitions.get(moveInfo.second()));
        obj = moveInfo.third();
        Utils.updateInCostWeight(weightedMatrix,inCost,
                moveInfo.first().third(),
                partitions.get(moveInfo.first().second()),
                partitions.get(moveInfo.second()));
        updateViolation();
    }

    /**
     * calculate violation array and number of violation
     * @param partitions partitions list
     * @param k number of partitions
     * @param alpha bound of difference among partitions
     * @return number of violations
     */
    protected int calViolation(List<List<Integer>> partitions, int k, int alpha){
        return Utils.calViolationBetweenPartitions(partitions,k,alpha);
    }

    protected double calObjective(double cutWeight, int violation){
        return cutWeight+W*violation;
    }

    /**
     * find the best neighbor
     * @param tabu array of tabu
     * @return update info Triplet includes:
     *      Triplet includes :
     *          Integer: position of moved vertex in partition
     *          Integer: idx of source partition
     *          Integer: idx of moved vertex
     *       Integer : idx of destination partition
     *       Double : objective value of new neighbor solution
     */
    protected Triplet<Triplet<Integer,Integer,Integer>,Integer,Double> findBestNeighbor(int[] tabu){
        int selectVerIdx=-1;
        int selectVerPosInPart = -1;
        int selectSrcPartition =-1;
        int selectDesPartition =-1;

        double neighBestObj = Double.POSITIVE_INFINITY;
        double tmpObj;

        int vertexIdx;
        List<Integer> auxPart;
        int auxViolation;
        for (int oldPart=0;oldPart<k;++oldPart){
            auxPart = partitions.get(oldPart);
            for (int posVer=0;posVer<auxPart.size();++posVer){
                vertexIdx=auxPart.get(posVer);
                if(tabu[vertexIdx]>0){
                    continue;
                }

                for (int nextPart=0;nextPart<k;nextPart++){
                    if(nextPart!=oldPart){
                        moveVertexToNewPart(posVer,partitions.get(oldPart),partitions.get(nextPart));
                        auxViolation = calViolation(partitions,k,alpha);
                        Utils.updateInCostWeight(weightedMatrix,inCost,vertexIdx,
                                partitions.get(oldPart),partitions.get(nextPart));

                        tmpObj = calObjective(inCost[inCost.length-1],auxViolation);
                        if (tmpObj<neighBestObj){
                            neighBestObj=tmpObj;
                            selectVerIdx=vertexIdx;
                            selectVerPosInPart=posVer;
                            selectSrcPartition=oldPart;
                            selectDesPartition=nextPart;
                        }
                        undoMoveVertexToNewPart(posVer,partitions.get(oldPart),partitions.get(nextPart));
                        Utils.updateInCostWeight(weightedMatrix,inCost,vertexIdx,
                                partitions.get(nextPart),partitions.get(oldPart));

                    }
                }
            }
        }
        return new Triplet<>(new Triplet<>(selectVerPosInPart,selectSrcPartition,selectVerIdx),
                selectDesPartition,neighBestObj);
    }

    protected void moveVertexToNewPart(int posVer,List<Integer> oldPart, List<Integer> nextPart){
        Collections.swap(oldPart,posVer,oldPart.size()-1);
        nextPart.add(oldPart.remove(oldPart.size()-1));
    }

    protected void undoMoveVertexToNewPart(int posVer, List<Integer> oldPart, List<Integer> nextPart){
        oldPart.add(nextPart.remove(nextPart.size()-1));
        Collections.swap(oldPart,posVer,oldPart.size()-1);
    }

    public double getCutWeight(){
        return this.inCost[1];
    }

    public String getStatus(){
        return (violation==0)?"FEASIBLE":"INFEASIBLE";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object clone(){
        Solution cloned;
        try {
            cloned = (Solution) super.clone();
        } catch (CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            System.out.println("Error When Cloning");
            throw new InternalError(e);
        }

        cloned.partitions = new ArrayList<>(this.partitions.size());
        for (int i=0;i<partitions.size();++i){
            cloned.partitions.add((List<Integer>) ((ArrayList<Integer>) this.partitions.get(i)).clone());
        }

        cloned.inCost = this.inCost.clone();
        return cloned;
    }
}
