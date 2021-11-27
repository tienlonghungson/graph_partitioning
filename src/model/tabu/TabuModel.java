package model.tabu;

import model.AbstractModel;
import service.Pair;
import service.Triplet;
import service.Utils;

import java.util.*;

public class TabuModel extends AbstractModel {
//    private List<List<Double>> weightedMatrix;
    private int numVer;

    private int[] tabu;
    private final int TB_MIN=2, TB_MAX=5;
    private int tbl=3;

    Solution currSol, bestSol, lastImprovedSol;

    private final int W=1000;

    public TabuModel(){
        this.modelName = "TabuSearch";
    }

    /**
     * generate a random solution
     * @return random solution
     */
    private Solution genSolution(){
        final int CAP_PART = (int)Math.floor((double) numVer/(double) k)+ alpha;
        List<List<Integer>> partitions = new ArrayList<>(k);
        for (int i=0;i<k;++i){
            partitions.add(new ArrayList<>(CAP_PART));
        }

        Random  rd = new Random();
        for (int i=0;i<numVer;++i){
            partitions.get(rd.nextInt(k)).add(i);
        }

        return new Solution(partitions);
    }

    /**
     * calculate the maximum iteration provided number of vertices
     * @return maximum iteration
     */
    private int maxIteration(){
        return 500;
    }
    @Override
    protected void solve(int k, int alpha) {
        Solution.weightedMatrix = inputInterface.getWeightedMatrix();

        numVer = inputInterface.getNumVertices();
        this.k=Solution.k=k;
        this.alpha = Solution.alpha= alpha;
        Solution.W = W;

        currSol = genSolution();
        double bestObj = Double.POSITIVE_INFINITY;
        double oldObj;

        final int MAX_ITER = maxIteration();
        int it=0;
        int stable=0, stableLimit=30;
        int restartFreq=100;

        while ((it<MAX_ITER)||(!isTimeUp)){
            it++;
            if (currSol.obj<bestObj){
                bestObj=currSol.obj;
                bestSol=(Solution)currSol.clone();
                stable=0;
            } else if (stable==stableLimit){
                currSol=(Solution) lastImprovedSol.clone();
                stable=0;
            } else {
                stable++;
                if (it%restartFreq==0){
                    currSol = genSolution();
                    Arrays.fill(tabu,0);
                }
            }

            oldObj=currSol.obj;
            Triplet<Triplet<Integer,Integer,Integer>,Integer,Double> moveToNext = currSol.findBestNeighbor(tabu);
            currSol.update(moveToNext);

            for (int i=0;i<tabu.length;++i){
                if (tabu[i]>0){
                    tabu[i]--;
                }
            }

            tabu[moveToNext.first().third()]= tbl;
            if (currSol.obj<oldObj){
                if (tbl>TB_MIN){
                    tbl--;
                }

                lastImprovedSol = (Solution) currSol.clone();
                stable=0;
            } else {
                if (tbl<TB_MAX){
                    tbl++;
                }
            }
        }

        // return result
        this.bestPartitions = bestSol.partitions;
        this.weighted = bestSol.obj;
    }
}
