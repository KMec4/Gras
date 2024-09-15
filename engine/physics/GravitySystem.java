package engine.physics;

import org.joml.Vector3f;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With2;
import engine.components.Moveable;
import engine.ecs.ScheduledSystem;

public class GravitySystem extends ScheduledSystem<With2<Physics, Moveable>>
{

    @Override
    public Results<With2<Physics, Moveable>> requestData(Dominion ecs)
    {
        return ecs.findEntitiesWith(Physics.class, Moveable.class);
    }

    @Override
    public void execute(With2<Physics, Moveable> result, double deltaTime)
    {
        float gravityAcceleration = (float) deltaTime * 0.9f /* m/s */ ;
        Vector3f newDirection = result.comp2().direction.mul(result.comp2().velocity).add(new Vector3f(0, gravityAcceleration, 0f));
        result.comp2().velocity = newDirection.length();
        result.comp2().direction = newDirection.normalize();
    }
}
