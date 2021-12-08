package model.constraintprogramming;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.sat.*;
import model.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class CPModel extends AbstractModel {

    public CPModel(){
        this.modelName = "CPSolver";
    }

    @Override
    protected void solve(int k, int alpha) {
        List<List<Double>> weightedMatrix = inputInterface.getWeightedMatrix();
        int numVertices = inputInterface.getNumVertices();
        long[] weightedMatrixFlatten = new long[numVertices*numVertices];

        CpModel cpModel = new CpModel();

        IntVar[][] x= new IntVar[numVertices][numVertices];
        IntVar[] xFlatten = new IntVar[numVertices*numVertices];
        for (int i=0;i<numVertices;++i){
            for (int j = 0; j < numVertices; j++) {
                x[i][j] = cpModel.newBoolVar("x["+i+"]["+j+"]");
                xFlatten[i*numVertices+j]=x[i][j];
                weightedMatrixFlatten[i*numVertices+j] = Math.round(weightedMatrix.get(i).get(j));
            }
        }

        IntVar[][] y= new IntVar[numVertices][k];
        IntVar[][] yTranspose = new IntVar[k][numVertices];
        for (int i=0;i<numVertices;++i){
            for (int j=0;j<k;++j){
                y[i][j] = cpModel.newBoolVar("y["+i+"]["+j+"]");
                yTranspose[j][i]= y[i][j];
            }
        }

        // constraint (1): each vertex belongs to exactly one partition
        for (int i=0;i<numVertices;++i){
            cpModel.addEquality(LinearExpr.sum(y[i]),1);
        }

        // constraint (2): boundary of volume of each partition
        for (int i = 0; i < k-1; ++i) {
            for (int j=i+1;j<k;++j){
                cpModel.addGreaterOrEqualWithOffset(LinearExpr.sum(yTranspose[i]),LinearExpr.sum(yTranspose[j]),alpha);
                cpModel.addGreaterOrEqualWithOffset(LinearExpr.sum(yTranspose[j]),LinearExpr.sum(yTranspose[i]),alpha);
            }
        }

        // constraint (3): the consistency of x(i,j)
        for (int i=0;i<numVertices-1;i++){
            for (int j=i+1;j<numVertices;j++){
                for(int l=0;l<k;l++){
                    cpModel.addLessOrEqual(LinearExpr.
                            scalProd(new IntVar[]{x[i][j],y[j][l],y[i][l]},new int[]{1,1,-1}),1);
                    cpModel.addLessOrEqual(LinearExpr.
                            scalProd(new IntVar[]{x[i][j],y[i][l],y[j][l]},new int[]{1,1,-1}),1);
                }
            }
        }

        cpModel.maximize(LinearExpr.scalProd(xFlatten,weightedMatrixFlatten));
        CpSolver solver = new CpSolver();
        CpSolverStatus resultStatus = solver.solve(cpModel);

        switch (resultStatus){
            case OPTIMAL -> this.status = "OPTIMAL";
            case FEASIBLE -> this.status = "FEASIBLE";
            case INFEASIBLE -> this.status = "INFEASIBLE";
            default -> this.status="NOT_SOLVED";
        }

        // calculate real objective value (since the coefficients is rounded during solving
        double cutWeight = 0;
        for (int i=0;i<numVertices-1;++i){
            for (int j=i+1;j<numVertices;++j){
                cutWeight+= (solver.value(x[i][j])==1)?0:weightedMatrix.get(i).get(j);
            }
        }
        this.weighted = cutWeight;

        this.bestPartitions = new ArrayList<>(k);
        for (int i=0;i<k;++i){
            List<Integer> part = new ArrayList<>();
            for (int j=0;j<numVertices;++j){
                if (solver.value(y[j][i])==1){
                    part.add(j);
                }
            }
            this.bestPartitions.add(part);
        }
    }
}
