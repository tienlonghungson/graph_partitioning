package model.evolution.nsgaii;

import model.evolution.Individual;
import model.evolution.Population;
import service.Pair;

import java.util.ArrayList;
import java.util.List;

public class NSGA2Population extends Population {
    protected List<List<Individual>> fronts;

    public NSGA2Population(int popSize){
        super(popSize);
        for (int i=0;i<(POP_SIZE<<1);i++){
            population.add(new NSGA2Individual());
        }
    }

    protected void firstEval(){
        double uCoupling=0; int uBalance=0;
        Pair<Double,Integer> currUPair;
        NSGA2Individual ind;
        for (Individual individual: population){
            ind = (NSGA2Individual) individual;
            currUPair = ind.getCutWeightAndMaxViolation();
            uCoupling = Math.max(uCoupling,currUPair.first());
            uBalance = Math.max(uBalance, currUPair.second());
        }
        NSGA2Individual.uCoupling = uCoupling;
        NSGA2Individual.uLoadBal=uBalance;

        eval();
    }

    protected void eval(){
        NSGA2Individual ind;
        for(Individual individual: population){
            ind=(NSGA2Individual)individual;
            ind.setObjectives();
        }
    }

    protected void fastNonDominatedSorting(){
        fronts = new ArrayList<>();
        fronts.add(new ArrayList<>());
        int sortedNum = 0; // number of sorted individual

//        System.out.println(POPULATION_SIZE<<1);
        for (int i=0;i<(POP_SIZE<<1);++i){
            NSGA2Individual individual = (NSGA2Individual) getPopulation().get(i);
            individual.getDominatedList().clear(); // set to empty
            individual.updateNumDominated(-individual.getNumDominated()); // set to 0
            for (int j=0;j<(POP_SIZE<<1);++j){
                if (j!=i) {
//                    System.out.print(j);
                    NSGA2Individual other = (NSGA2Individual) getPopulation().get(j);
                    if (other.isDominated(individual)) {
                        individual.getDominatedList().add(other);
                    } else if (individual.isDominated(other)){
                        individual.updateNumDominated(1);
                    }
                }
            }
//            System.out.println();
            if (individual.getNumDominated()==0){
//                System.out.println("Added to Pareto");
                fronts.get(0).add(individual);
                individual.setRank(0);
                sortedNum++;
            }
        }

        int i=-1;
        while(!fronts.get(++i).isEmpty()&&(sortedNum<POP_SIZE)){
            ArrayList<Individual> q  = new ArrayList<>();
            for (Individual individual: fronts.get(i)) {
                for (NSGA2Individual other : ((NSGA2Individual) individual).getDominatedList()){
                    other.updateNumDominated(-1);
                    if (other.getNumDominated()==0){
                        q.add(other);
                        other.setRank(i+1);
                        sortedNum++;
                    }
                }
            }
            fronts.add(q);
        }
//        System.out.println("Stopped Searching. Sorted Num= "+sortedNum);
//        System.out.println("There are "+fronts.size()+" fronts");
//        for (List<Individual> front : fronts){
//            System.out.println("Front have "+front.size());
//        }
    }

    /**
     * assign crowding-distance to each individual of a front
     *
     * @param front list of individuals need to be assigned crowding-distance
     */
    private void crowdingDistanceAssignment(List<Individual> front){
        int lastIdx = front.size()-1;
        double fMax,fMin;
        Individual individual, preInd, postInd;
        for (Individual ind : front){
            ind.setFitness(0);
        }

        assert (!front.isEmpty());
        for (int i=0;i<Individual.numObjective;++i){
            int finalI = i;
            front.sort(((o1, o2) -> {
                if (o1.getObjective(finalI)>=o2.getObjective(finalI)){
                    return 0;
                } else {
                    return 1;
                }
            }));
            front.get(0).setFitness(Double.POSITIVE_INFINITY);
            front.get(lastIdx).setFitness(Double.POSITIVE_INFINITY);
            fMin = front.get(0).getObjective(finalI);
            fMax = front.get(lastIdx).getObjective(finalI);

            for (int j = 1; j < lastIdx; j++) {
                individual=front.get(j); preInd = front.get(j-1); postInd = front.get(j+1);
                individual.setFitness(individual.getFitness()+
                        (postInd.getObjective(finalI)-preInd.getObjective(finalI))/(fMax-fMin));
            }

        }
    }

    /**
     * assign crowding-distance to each individual of a front determined by its index
     *
     * @param idx index of the front
     */
    private void crowdingDistanceAssignment(int idx){
        crowdingDistanceAssignment(fronts.get(idx));
    }

    /**
     * select top N {@code POPULATION_SIZE} individual from this Population currently in size of 2*N
     */
    protected void select(){
        fastNonDominatedSorting();
//        System.out.println("Sorted");
        assert (fronts!=null);
        List<Individual> selectedPop = new ArrayList<>(POP_SIZE<<1);
        int currSize=0;
        for (List<Individual> front: this.fronts){
            currSize+=front.size();
            if (currSize<POP_SIZE){
                crowdingDistanceAssignment(front);
                selectedPop.addAll(front);
            } else {
                int supplementNum = POP_SIZE-currSize+front.size();
                crowdingDistanceAssignment(front);
                selectedPop.addAll(front.subList(0,supplementNum));
                break;
            }
        }
        setPopulation(selectedPop);
    }

    public void sortPopulation() {
//        assert (fronts!=null);
        if (fronts!=null) {
            fronts.get(0).sort(((o1, o2) -> {
                if (o1.getFitness() > o2.getFitness()) {
                    return 1;
                } else {
                    return 0;
                }
            }));
        }
    }

    public Individual getFittest(int offset) {
        assert (fronts!=null);
        return fronts.get(0).get(offset);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Individual individual: getPopulation()){
            stringBuilder.append(individual.toString());
        }
        return stringBuilder.toString();
    }

}
