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
        int numVer = weightedMatrix.size();
        double[] inCost = new double[numVer+2];
        double[] partCost = new double[partitions.size()];
        double totalWeight = calTotalWeight(weightedMatrix);
        inCost[numVer] = inCost[numVer+1]= totalWeight;

        for (int i=0;i< partitions.size();++i){
            for (int start: partitions.get(i)){
                for (int des: partitions.get(i)){
                    inCost[start]+= weightedMatrix.get(start).get(des);
                }
                partCost[i]+=inCost[start];
            }
            partCost[i]/=2;
            inCost[numVer+1] -= partCost[i];
        }
        return inCost;
    }

    /**
     * update inCostWeight is called after a vertex changed its partition
     * @param weightedMatrix weight of edges
     * @param inCost inCost array
     * @param vertex the vertex that changed its partition
     * @param oldPart index of its old partition
     * @param newPart index of its new partition
     * @param partitions list of partitions ( required to be updated first)
     */
    public static void updateInCostWeight(List<List<Double>> weightedMatrix,double[] inCost, int vertex,
                                              int oldPart, int newPart, List<List<Integer>> partitions){
        int len = inCost.length;
        for (int verInOldPart: partitions.get(oldPart)){
            inCost[verInOldPart]-= weightedMatrix.get(verInOldPart).get(vertex);

            // update cutWeight
            inCost[len-1]+= weightedMatrix.get(verInOldPart).get(vertex);
        }

        inCost[vertex]=0;
        for (int verInNewPart: partitions.get(newPart)){
            inCost[verInNewPart] +=  weightedMatrix.get(verInNewPart).get(vertex);
            inCost[vertex]+= weightedMatrix.get(verInNewPart).get(vertex);

            // update cutWeight
            inCost[len-1]-= weightedMatrix.get(verInNewPart).get(vertex);
        }
    }

    /**
     * update inCostWeight is called after a vertex changed its partition
     * @param weightedMatrix weight of edges
     * @param inCost inCost array
     * @param vertex the vertex that changed its partition
     * @param oldPart old partition list
     * @param nextPart new partition list
     */
    public static void updateInCostWeight(List<List<Double>> weightedMatrix,double[] inCost, int vertex,
                                          List<Integer> oldPart, List<Integer> nextPart){
        int len = inCost.length;
        for (int verInOldPart: oldPart){
            inCost[verInOldPart]-= weightedMatrix.get(verInOldPart).get(vertex);

            // update cutWeight
            inCost[len-1]+= weightedMatrix.get(verInOldPart).get(vertex);
        }

        inCost[vertex]=0;
        for (int verInNewPart: nextPart){
            inCost[verInNewPart] +=  weightedMatrix.get(verInNewPart).get(vertex);
            inCost[vertex]+= weightedMatrix.get(verInNewPart).get(vertex);

            // update cutWeight
            inCost[len-1]-= weightedMatrix.get(verInNewPart).get(vertex);
        }
    }


    /**
     * calculate the totalWeight and cutWeight
     * @param weightedMatrix weight of edges
     * @param partitions list of partitions
     * @param vertices array of vertices object
     * @return the cost array with information storing at 2 elements
     * 2 elements respectively are totalWeight and cutWeight
     */
    public static double[] initInCostWeight(List<List<Double>> weightedMatrix,
                                            List<List<Integer>> partitions, Vertex[] vertices){
        int numVer = vertices.length;
        double[] cost = new double[2]; // cost[0] is totalWeight, cost[1] is cutWeight

        double tmpWeight;
        for(int i=0;i<numVer-1;++i){
            for (int j=i+1;j<numVer;++j){
                tmpWeight = weightedMatrix.get(i).get(j);

                if (vertices[i].partIdx==vertices[j].partIdx){ // if i and j are on the same partition
                    vertices[i].inCost+=tmpWeight;
                    vertices[j].inCost+=tmpWeight;
                } else {
                    cost[1]+=tmpWeight; // i and j aren't on the same partition, we update the cutWeight
                }
                cost[0]+=tmpWeight;
            }
        }

        return cost;
    }


}
