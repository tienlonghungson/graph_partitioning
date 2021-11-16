package main;

import model.AbstractModel;
import model.integerprogramming.IPModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
//        AbstractModel.execute(new IPModel(),1,Config.kArray[0], Config.alphaArray[1],"test");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
//        File dir = new File(inputDir.toString());
        File file = new File("C:\\Users\\thanh\\IdeaProjects\\graphpartitioning\\data\\");
        System.out.println(file.getCanonicalFile());
//        for (File f : file.listFiles()) {
//            System.out.println(f.toString());
//        }

    }
}
