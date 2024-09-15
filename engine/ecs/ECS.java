package engine.ecs;

import java.util.ArrayList;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Scheduler;

public class ECS
{
    //static

    public static int scheduleRate = 100;

    static Dominion ecs;
    static Scheduler scheduler;

    static ArrayList<ECSSystem<?>> systems;
    static ArrayList<Runnable> executeables;

    public static void init()
    {
        ecs = Dominion.create();
        scheduler = ecs.createScheduler();
        systems = new ArrayList<>();
        executeables = new ArrayList<>();
    }

    public static void stopAll()
    {
        for(ECSSystem<?> system : systems)
            system.stop();
    }

    public static void startAll()
    {
        for(ECSSystem<?> system : systems)
            system.start();
        
        scheduler.tickAtFixedRate(scheduleRate);
    }

    static <T> void addSystem(ScheduledSystem<T> sys)
    {
        scheduler.schedule(sys);
        systems.add(sys);
        executeables.add(sys);
    }

    static <T> void addSystem(ECSSystem<T> sys)
    {
        systems.add(sys);
    }

}
