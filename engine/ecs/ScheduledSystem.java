package engine.ecs;

public abstract class ScheduledSystem<T> extends ECSSystem<T>
{


    public ScheduledSystem()
    {
        ECS.addSystem(this);
    }
}