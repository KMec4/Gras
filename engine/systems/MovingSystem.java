package engine.systems;

import org.joml.Vector3f;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With2;
import engine.ecs.ScheduledSystem;
import engine.components.Moveable;
import engine.components.Position;

public class MovingSystem extends ScheduledSystem<With2<Moveable,Position>>
{

    Vector3f toAdd = new Vector3f();

    @Override
    public Results<With2<Moveable, Position>> requestData(Dominion ecs)
    {
        return ecs.findEntitiesWith(Moveable.class, Position.class);
    }

    @Override
    public void execute(With2<Moveable, Position> result, double deltaTime)
    {
        result.comp1().direction.mul(result.comp1().velocity * (float) deltaTime, toAdd);
        result.comp2().position.add( toAdd );
    }

}
