package model.tabu;

import service.Pair;
import service.Triplet;
import service.Utils;

import java.util.Collections;
import java.util.List;

public class Solution implements Cloneable{
    protected static int k, alpha, W;
    protected static List<List<Double>> weightedMatrix;
    protected List<List<Integer>> partitions;
    protected double[] inCost;
    protected int violation;
    protected int[][] violations;
    protected double obj;

    public Solution(){}

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
        this.violations=violations;
        this.obj=obj;
    }
    
    protected void updateViolation(){
        Pair<Integer,int[][]> pair= calViolation(partitions,k,alpha);
        violation = pair.first(); violations= pair.second();
        obj = calObjective(inCost[inCost.length-1], violation);
    }

    /**
     * update solution by a new move
     * @param moveInfo contains information of the move
     */
    protected void update(Triplet<Triplet<Integer,Integer,Integer>,Integer, Double> moveInfo){
        moveVertexToNewPart(moveInfo.first().first(),
                partitions.get(moveInfo.first().second()),partitions.get(moveInfo.second()));
        obj = moveInfo.third();
        Utils.updateInCostWeight(weightedMatrix,inCost,
                partitions.get(moveInfo.first().second()).get(moveInfo.first().first()),
                partitions.get(moveInfo.first().second()),
                partitions.get(moveInfo.second()));
        updateViolation();
    }

    /**
     * calculate violation array and number of violation
     * @param partitions partitions list
     * @param k number of partitions
     * @param alpha bound of difference among partitions
     * @return a Pair:
     *              Integer : number of violations
     *              int[] : violation array
     */
    protected Pair<Integer,int[][]> calViolation(List<List<Integer>> partitions, int k, int alpha){
        int numViolation=0;
        int[][] violations = new int[k-1][];
        for (int i=0;i<k-1;++i){
            violations[i]= new int[k-(i+1)];
            for (int j=i+1;j<k;++j){
                violations[i][j-(i+1)]=partitions.get(i).size()-partitions.get(j).size();
                numViolation+= (Math.abs(violations[i][j-(i+1)])<=alpha)?0:1;
            }
        }
        return new Pair<>(numViolation,violations);
    }

    protected double calObjective(double cutWeight, int violation){
        return cutWeight+W*violation;
    }

    protected Triplet<Triplet<Integer,Integer,Integer>,Integer,Double> findBestNeighbor(int[] tabu){
        int selectVerIdx=-1;
        int selectVerPosInPart = -1;
        int selectSrcPartition =-1;
        int selectDesPartition =-1;

        double neighBestObj = Double.POSITIVE_INFINITY;
        double tmpObj;

        int vertexIdx;
        List<Integer> auxPart;
        Pair<Integer,int[][]> auxPair;
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
                        auxPair = calViolation(partitions,k,alpha);
                        Utils.updateInCostWeight(weightedMatrix,inCost,vertexIdx,
                                partitions.get(oldPart),partitions.get(nextPart));

                        tmpObj = calObjective(inCost[inCost.length-1],auxPair.first());
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
//        int auxVer = oldPart.get(posVer);?
        Collections.swap(oldPart,posVer,oldPart.size()-1);
        oldPart.remove(oldPart.size()-1);
        nextPart.add(oldPart.remove(oldPart.size()-1));
    }

    protected void undoMoveVertexToNewPart(int posVer, List<Integer> oldPart, List<Integer> nextPart){
        oldPart.add(nextPart.remove(nextPart.size()-1));
        Collections.swap(oldPart,posVer,oldPart.size()-1);
    }


    @Override
    protected Object clone(){
        Solution cloned;
        try {
            cloned = (Solution) super.clone();
        } catch (CloneNotSupportedException cloneNotSupportedException){
            cloned = new Solution(List.copyOf(this.partitions));
        }
        Collections.copy(cloned.partitions,this.partitions);
        cloned.inCost = this.inCost.clone();
        cloned.violations= new int[this.violations.length][];
        for (int i=0;i<this.violations.length;++i){
            cloned.violations[i] = this.violations[i].clone();
        }
        return cloned;
    }
}
