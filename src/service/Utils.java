package service;

import java.util.List;

public class Utils {
    /**
     * calculate total weight of the graph
     * @param weightedMatrix weight of edges
     * @return total weight of the graph
     */
    public static double calTotalWeight(List<List<Double>> weightedMatrix){
        double totalWeight=0;
        int numVer = weightedMatrix.size();
        for (int i=0;i<numVer-1;++i){
            for (int j=i+1;j<numVer;++j){
                totalWeight+=weightedMatrix.get(i).get(j);
            }
        }
        return totalWeight;
    }

    /**
     * calculate the inCost of each vertex
     * @param weightedMatrix weight of edges
     * @param partitions list of partitions
     * @return the inCost array with extra information storing at 2 last elements
     * 2 last elements respectively are totalWeight and cutWeight
     */
    public static double[] initInCostWeight(List<List<Double>> weightedMatrix, List<List<Integer>> partitions){
        double[] inCost = new double[2];
        double partCost;
        double totalWeight = calTotalWeight(weightedMatrix);
        inCost[0] = inCost[1]= totalWeight;

        for (List<Integer> partition : partitions) {
            partCost = 0;
            for (int start : partition) {
                for (int des : partition) {
                    partCost += weightedMatrix.get(start).get(des);
                }
            }
            partCost /= 2;
            inCost[1] -= partCost;
        }
        return inCost;
    }


    /**
     * update inCostWeight is called after a vertex changed its partition
     * @param weightedMatrix weight of edges
     * @param inCost inCost array
     * @param vertex idx of vertex that changed its partition
     * @param oldPart old partition list
     * @param nextPart new partition list
     */
    public static void updateInCostWeight(List<List<Double>> weightedMatrix,double[] inCost, int vertex,
                                          List<Integer> oldPart, List<Integer> nextPart){
        final int LAST_IDX = inCost.length-1;
        for (int verInOldPart: oldPart){
            // update cutWeight
            inCost[LAST_IDX]+= weightedMatrix.get(verInOldPart).get(vertex);
        }

        for (int verInNewPart: nextPart){
           // update cutWeight
            inCost[LAST_IDX]-= weightedMatrix.get(verInNewPart).get(vertex);
        }

    }

    /**
     * calculate violation between partitions
     * if the different between 2 partitions exceeds alpha, the violation = different - alpha
     * @param partitions list of partitions
     * @param nParts number of partitions
     * @param alpha boundary of different between partitions
     * @return sum of (different - alpha) (if different >alpha)
     */
    public static int calViolationBetweenPartitions(List<List<Integer>> partitions, int nParts, int alpha){
        int numViolation=0;
        int diffIJ;
        for (int i=0;i<nParts-1;++i){
            for (int j=i+1;j<nParts;++j){
                diffIJ=partitions.get(i).size()-partitions.get(j).size();
                numViolation += Math.max(Math.abs(diffIJ)-alpha,0);
            }
        }
        return numViolation;
    }

    /**
     * calculate the violation of the whole partitions
     * if the different between 2 partitions exceeds alpha, the number of violation increase 1
     * @param partitions partitions list
     * @param nPart number of partitions
     * @param alpha boundary of different between partitions
     * @return number of violation
     */
    public static int calViolationOfWholePartitions(List<List<Integer>> partitions, int nPart, int alpha){
        int numViolation=0;
        for (int i=0;i<nPart-1;++i){
            for (int j=i+1;j<nPart;++j){
                numViolation+=(Math.abs(partitions.get(i).size()-partitions.get(j).size())>alpha)?1:0;
            }
        }
        return numViolation;
    }

}
