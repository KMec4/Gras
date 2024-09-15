package engine.ecs;

import java.lang.System.Logger.Level;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;

public abstract class ECSSystem<T> implements Runnable
{
    long start = System.nanoTime();
    boolean isEnabled = false;
    private int disabledCounter = 0;

    public abstract Results<T> requestData(Dominion ecs);
    public abstract void execute(T result, double deltaTime);
    public void onStart() {}
    public void onEnd() {}

    public void start() { isEnabled = true; }
    public void stop() { isEnabled = false; }

    @Override
    public void run()
    {
        if(isEnabled)
        {
            onStart();
            long now = System.nanoTime();
            float dt = ( now - start ) / 1000000000f;
            start = now;

            requestData(ECS.ecs).stream().forEach( result -> 
            {
                execute(result, dt);
            });
            onEnd();
        }
        else
        {
            disabledCounter = disabledCounter++ % 40;
            if(disabledCounter == 0)
            {
                System.getLogger("Global").log(Level.WARNING, "System is not enabled: " + getClass());
            }
        }
    }

    public ECSSystem()
    {
        ECS.addSystem(this);
    }
}
