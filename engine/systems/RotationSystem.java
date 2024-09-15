package engine.systems;

import org.joml.Vector3f;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With1;
import engine.components.Rotateable;
import engine.ecs.ScheduledSystem;

public class RotationSystem extends ScheduledSystem<With1<Rotateable>>
{

    Vector3f toAdd = new Vector3f();

    @Override
    public Results<With1<Rotateable>> requestData(Dominion ecs)
    {
        return ecs.findEntitiesWith(Rotateable.class);
    }

    @Override
    public void execute(With1<Rotateable> result, double deltaTime)
    {
        result.comp().speeds.mul((float) deltaTime, toAdd);
        result.comp().rotation.add(toAdd);
    }
}

