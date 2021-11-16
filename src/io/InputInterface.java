package io;

import basic.Triplet;

import java.util.List;

public interface InputInterface {
    int getNumVertices();
    int getNumEdges();
    List<Triplet<Integer,Integer,Double>> getListEdges();
    List<List<Double>> getWeightedMatrix();
}
