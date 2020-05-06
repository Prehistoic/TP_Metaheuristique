package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.*;

import javax.print.DocFlavor;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            /*long deadline = 100;

            ResourceOrder ro = new ResourceOrder(instance);
            ro.addTask(0,0,0);
            ro.addTask(0, 1,1);
            ro.addTask(1,1,0);
            ro.addTask(1,0,1);
            ro.addTask(2, 0,2);
            ro.addTask(2,1,2);

            ResourceOrder ro2 = new ResourceOrder(ro.toSchedule());

            System.out.println("ResourceOrder1:\n" + ro);
            System.out.println("ResourceOrder2:\n" + ro2);*/

            /*long deadline = 1000;

            SPTSolver solver = new SPTSolver();
            Result result = solver.solve(instance,deadline);

            ResourceOrder res = new ResourceOrder(result.schedule);
            System.out.println("ENCODING:\n" + res);
            System.out.println("MAKESPAN:" + res.toSchedule().makespan());

            DescentSolver solver2 = new DescentSolver();
            Result result2 = solver2.solve(instance,deadline);

            ResourceOrder res2 = new ResourceOrder(result2.schedule);

            System.out.println("\nENCODING:\n" + res2);
            System.out.println("MAKESPAN:" + res2.toSchedule().makespan());*/

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}