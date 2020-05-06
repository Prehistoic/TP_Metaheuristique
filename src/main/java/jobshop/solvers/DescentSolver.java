package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import org.graalvm.compiler.lir.SwitchStrategy;

import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {

            Task tmp = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = tmp;

        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {

        // Initialisation avec une solution issue d'un algo glouton : s_init = Glouton(instance)
        EST_LRPTSolver solver = new EST_LRPTSolver();
        Result result = solver.solve(instance,deadline);
        ResourceOrder order = new ResourceOrder(result.schedule);

        // Mémoriser la meilleur solution : s* = s_init
        ResourceOrder best_order = order;

        // Exploration des voisinages successifs
        boolean got_better = true;
        while(got_better && deadline - System.currentTimeMillis() > 1) {

            ResourceOrder best_neighbor = null;
            got_better = false;

            ResourceOrder current_order = best_order.copy();
            List<Block> blocks = blocksOfCriticalPath(current_order);

            if(!blocks.isEmpty()) {

                for (Block block : blocks) {
                    List<Swap> swaps = neighbors(block);
                    for (Swap swap : swaps) {
                        ResourceOrder tmp = current_order.copy();
                        swap.applyOn(tmp);
                        if(best_neighbor == null) {
                            best_neighbor = tmp.copy();
                        } else {
                            int makespan = tmp.toSchedule().makespan();
                            if (makespan < best_neighbor.toSchedule().makespan()) {
                                best_neighbor = tmp.copy();
                            }
                        }
                    }
                }

                if(best_neighbor.toSchedule().makespan() < best_order.toSchedule().makespan()) {
                    best_order = best_neighbor.copy();
                    got_better = true;
                }

            }

        }

        return new Result(instance, best_order.toSchedule(), Result.ExitCause.Blocked);

    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {

        List<Block> blocks = new ArrayList<Block>();

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
                    blocks.add(new Block(currentMachine,start,end));
                }
                currentMachine = order.instance.machine(t);
                start = order.getTaskIndex(currentMachine,t);
                end = start;
            }
        }

        return blocks;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> neighbors = new ArrayList<>();

        if(block.firstTask + 1 == block.lastTask) {
            neighbors.add(new Swap(block.machine, block.firstTask, block.lastTask));
        } else {
            neighbors.add(new Swap(block.machine, block.firstTask, block.firstTask + 1));
            neighbors.add(new Swap(block.machine, block.lastTask - 1, block.lastTask));
        }

        return neighbors;
    }

}
