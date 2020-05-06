package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.Task;
import jobshop.encodings.ResourceOrder;
import java.util.HashSet;
import java.util.Arrays;

public class LRPTSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        ResourceOrder sol = new ResourceOrder(instance);

        HashSet<Task> readyTasks = new HashSet<Task>();

        int[] remainingDuration = new int[instance.numJobs];

        int[] currentTaskPerJob = new int[instance.numJobs];
        Arrays.fill(currentTaskPerJob, 0);

        // On initialise les tâches réalisables avec la première tâche de chaque job
        for(int job=0; job<instance.numJobs; job++) {
            Task tmp = new Task(job,0);
            readyTasks.add(tmp);
        }

        // Boucle tant qu'il y a des tâches réalisables
        while(readyTasks.size() > 0 && deadline - System.currentTimeMillis() > 1) {

            // On choisit une tâche et on la place sur la ressource associée
            Arrays.fill(remainingDuration, 0);
            for(int job=0; job<instance.numJobs; job++) {
                if(currentTaskPerJob[job] != -1) {
                    for(int task=currentTaskPerJob[job]; task<instance.numTasks; task++) {
                        remainingDuration[job] += instance.duration(job,task);
                    }
                }
            }
            Task LRPT = null;
            for(Task task : readyTasks) {
                if (LRPT == null) {
                    LRPT = task;
                } else if(remainingDuration[task.job] > remainingDuration[LRPT.job]) {
                    LRPT = task;
                }
            }

            sol.addTask(instance.machine(LRPT), LRPT.job, LRPT.task);

            // On met à jour l'ensemble des tâches réalisables
            readyTasks.remove(LRPT);
            if(LRPT.task < instance.numTasks-1) {
                Task newTask = new Task(LRPT.job, LRPT.task + 1);
                readyTasks.add(newTask);
                currentTaskPerJob[LRPT.job] += 1;
            } else {
                currentTaskPerJob[LRPT.job] = -1;
            }

        }

        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }

}
