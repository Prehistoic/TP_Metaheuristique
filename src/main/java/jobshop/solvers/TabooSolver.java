package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.Task;
import jobshop.encodings.ResourceOrder;

import java.util.ArrayList;
import java.util.List;


public class TabooSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        // Variables de configuration de la méthode
        int maxIter = 10000;
        int dureeTaboo = 10;

        // Initialisation avec une solution issue d'un algo glouton : s_init = Glouton(instance)
        EST_LRPTSolver solver = new EST_LRPTSolver();
        Result result = solver.solve(instance,deadline);
        ResourceOrder init = new ResourceOrder(result.schedule);

        // Mémoriser la meilleur solution : s* = s_init
        ResourceOrder best_order = init;

        // Solution courante s = sinit
        ResourceOrder current = init;

        // Solutions tabou
        int sTaboo[][] = new int[instance.numJobs*instance.numTasks][instance.numJobs*instance.numTasks];

        // Compteur itérations
        int k = 0;

        // Exploration des voisinages successifs
        while(k < maxIter && deadline - System.currentTimeMillis() > 1) {

            ResourceOrder best_neighbor = null;
            int best_neighbor_t1 = -1;
            int best_neighbor_t2 = -1;
            k++;

            List<DescentSolver.Block> blocks = blocksOfCriticalPath(current);

            if(!blocks.isEmpty()) {

                for (DescentSolver.Block block : blocks) {

                    List<DescentSolver.Swap> swaps = neighbors(block);
                    for (DescentSolver.Swap swap : swaps) {

                        Task t1 = current.tasksByMachine[swap.machine][swap.t1];
                        Task t2 = current.tasksByMachine[swap.machine][swap.t2];
                        int id_t1 = t1.job * current.instance.numTasks + t1.task;
                        int id_t2 = t2.job * current.instance.numTasks + t2.task;
                        int value_case_sTaboo = sTaboo[id_t1][id_t2];

                        ResourceOrder tmp = current.copy();
                        swap.applyOn(tmp);

                        int makespan = tmp.toSchedule().makespan();

                        if (best_neighbor == null && (value_case_sTaboo <= k || (makespan < best_order.toSchedule().makespan() && value_case_sTaboo > k))) {
                            best_neighbor = tmp.copy();
                            best_neighbor_t1 = id_t1;
                            best_neighbor_t2 = id_t2;
                        } else if (best_neighbor != null) {
                            if (makespan < best_neighbor.toSchedule().makespan() && value_case_sTaboo <= k) {
                                best_neighbor = tmp;
                                best_neighbor_t1 = id_t1;
                                best_neighbor_t2 = id_t2;
                            } else if (makespan < best_neighbor.toSchedule().makespan() && makespan < best_order.toSchedule().makespan() &&  value_case_sTaboo > k) {
                                best_neighbor = tmp;
                                best_neighbor_t1 = id_t1;
                                best_neighbor_t2 = id_t2;
                            }
                        }
                    }
                }

                if(best_neighbor != null) {

                    current = best_neighbor.copy();

                    sTaboo[best_neighbor_t2][best_neighbor_t1] = k + dureeTaboo;

                    if (best_neighbor.toSchedule().makespan() < best_order.toSchedule().makespan()) {
                        best_order = best_neighbor.copy();
                    }
                }
            }

        }

        return new Result(instance, best_order.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<DescentSolver.Block> blocksOfCriticalPath(ResourceOrder order) {

        List<DescentSolver.Block> blocks = new ArrayList<DescentSolver.Block>();

        List<Task> critical_path = order.toSchedule().criticalPath();

        int currentMachine = -1;
        int start = -1;
        int end = -1;

        for(Task t : critical_path) {
            if(currentMachine == -1) {
                currentMachine = order.instance.machine(t);
                start = order.getTaskIndex(currentMachine,t);
                end = start;
            } else if(currentMachine == order.instance.machine(t)) {
                end++;
            } else {
                if(start != end) {
                    blocks.add(new DescentSolver.Block(currentMachine,start,end));
                }
                currentMachine = order.instance.machine(t);
                start = order.getTaskIndex(currentMachine,t);
                end = start;
            }
        }

        return blocks;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<DescentSolver.Swap> neighbors(DescentSolver.Block block) {
        List<DescentSolver.Swap> neighbors = new ArrayList<>();

        if(block.firstTask + 1 == block.lastTask) {
            neighbors.add(new DescentSolver.Swap(block.machine, block.firstTask, block.lastTask));
        } else {
            neighbors.add(new DescentSolver.Swap(block.machine, block.firstTask, block.firstTask + 1));
            neighbors.add(new DescentSolver.Swap(block.machine, block.lastTask - 1, block.lastTask));
        }

        return neighbors;
    }
}