package model.integerprogramming;

import model.AbstractModel;

import java.util.ArrayList;
import java.util.List;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class IPModel extends AbstractModel {

    public IPModel(){
        this.modelName = "IPSolver";
    }

    @Override
    protected void solve(int k, int alpha) {
        List<List<Double>> weightedMatrix = inputInterface.getWeightedMatrix();
        int numVertices = inputInterface.getNumVertices();
        int numVar1Dim = numVertices+k;

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return;
        }

        MPVariable[][] x = new MPVariable[numVar1Dim][numVar1Dim];
        for (int i=0;i<numVar1Dim;++i){
            for (int j = 0; j < numVar1Dim; j++) {
                x[i][j] = solver.makeBoolVar("x["+i+"]["+j+"]");
            }
        }

        // constraint (1): each vertex belongs to exactly one partition
        for (int i=0;i<numVertices;++i){
            MPConstraint mpConstraint1 = solver.makeConstraint(1,1,"ver "+i+"in one partition");
            for (int j = numVertices; j <numVar1Dim ; j++) {
                mpConstraint1.setCoefficient(x[i][j],1);
            }
        }

        // constraint (2): boundary of volume of each partition
        for (int j=numVertices;j<numVar1Dim-1;j++){
            for (int l=j+1;l<numVar1Dim;l++){
                MPConstraint mpConstraint2 = solver.makeConstraint(-alpha, alpha,"diff part: "+j+"-"+l);
                for (int i=0;i<numVertices;i++){
                    mpConstraint2.setCoefficient(x[i][j],1);
                    mpConstraint2.setCoefficient(x[i][l],-1);
                }
            }
        }

        // constraint (3): the consistency of x(i,j)
        for (int i=0;i<numVertices;i++){
            for (int j=0;j<numVertices;j++){
                for(int l=numVertices;l<numVar1Dim;l++){
                    MPConstraint mpConstraint3 = solver.makeConstraint(-1,1,"consistent: "+i+"-"+j);
                    mpConstraint3.setCoefficient(x[i][j],1);
                    mpConstraint3.setCoefficient(x[i][l],-1);
                    mpConstraint3.setCoefficient(x[j][l],1);

                    mpConstraint3 = solver.makeConstraint(-1,1,"consistent: "+j+"-"+i);
                    mpConstraint3.setCoefficient(x[i][j],1);
                    mpConstraint3.setCoefficient(x[i][l],1);
                    mpConstraint3.setCoefficient(x[j][l],-1);
                }
            }
        }

        // set up objective and calculate total weight of the graph at the same time
        MPObjective objective = solver.objective(); double sumWeight=0;
        for (int i=0;i<numVertices;++i){
            for (int j=0;j<numVertices;++j){
                objective.setCoefficient(x[i][j],weightedMatrix.get(i).get(j));
                sumWeight += weightedMatrix.get(i).get(j);
            }
        }

        objective.setMaximization();
        final MPSolver.ResultStatus  resultStatus = solver.solve();

        if (resultStatus==MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE){
            this.status = (resultStatus==MPSolver.ResultStatus.OPTIMAL)?"OPTIMAL":"FEASIBLE";

            this.weighted = sumWeight- objective.value();

            this.partitionList = new ArrayList<>(k);
            int idxPart;
            for (int i=0;i<k;++i){
                List<Integer> part = new ArrayList<>();
                idxPart = i+numVertices;
                for (int j=0;j<numVertices;++j){
                    if (x[j][idxPart].solutionValue()==1){
                        part.add(j);
                    }
                }
                this.partitionList.add(part);
            }

        }
    }
}
