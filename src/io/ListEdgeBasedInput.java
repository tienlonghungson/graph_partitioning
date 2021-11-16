package io;

import basic.Triplet;

import java.io.*;
import java.util.*;

public class ListEdgeBasedInput implements InputInterface{
    private final int NUM_VERTICES;
    private final int NUM_EDGES;

    private List<Triplet<Integer,Integer,Double>> listEdges;

    public ListEdgeBasedInput(String fileName) throws IOException {
        this(new File(fileName));
    }

    public ListEdgeBasedInput(File file) throws IOException {
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringTokenizer stringTokenizer = new StringTokenizer(bfReader.readLine()," ");

        NUM_VERTICES = Integer.parseInt(stringTokenizer.nextToken());
        NUM_EDGES = Integer.parseInt(stringTokenizer.nextToken());
        listEdges = new ArrayList<>(NUM_EDGES);

        String currLine;
        while ((currLine= bfReader.readLine())!=null){
            stringTokenizer = new StringTokenizer(currLine);
            listEdges.add(new Triplet<>(
                    Integer.parseInt(stringTokenizer.nextToken()),
                    Integer.parseInt(stringTokenizer.nextToken()),
                    Double.parseDouble(stringTokenizer.nextToken()))
            );
        }
    }

    @Override
    public int getNumVertices() {
        return NUM_VERTICES;
    }

    @Override
    public int getNumEdges() {
        return NUM_EDGES;
    }

    @Override
    public List<Triplet<Integer,Integer,Double>> getListEdges() {
        return Collections.unmodifiableList(listEdges);
    }

    @Override
    public List<List<Double>> getWeightedMatrix() {
        List<List<Double>> weightedMatrix = new ArrayList<>(NUM_VERTICES+1);
        double[][] matrix = new double[NUM_VERTICES+1][NUM_VERTICES+1];
        int firstVer, secVer;
        for (Triplet<Integer,Integer,Double> row: listEdges) {
            firstVer = row.first();
            secVer = row.second();
            matrix[firstVer][secVer]=matrix[secVer][firstVer] = row.third();
        }
        for (double[] row : matrix){
            weightedMatrix.add(Arrays.stream(row).boxed().toList());
        }
        return Collections.unmodifiableList(weightedMatrix);
    }

    public static void main(String[] args){
        try {
            ListEdgeBasedInput listEdgeBasedInput = new ListEdgeBasedInput("src/io/test.txt");
            for (List<Double> row:
                 listEdgeBasedInput.getWeightedMatrix()) {
                System.out.println(row);
            }
        } catch (IOException ioException){
            System.out.println("Error when reading files");
            ioException.printStackTrace();
            System.out.println(ioException.getMessage());
        }
    }
}
