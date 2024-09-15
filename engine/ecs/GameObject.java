package engine.ecs;

import org.magicwerk.brownies.collections.BigList;

import dev.dominion.ecs.api.Entity;

public class GameObject
{
    static int lastObjId = 0;
    static BigList<GameObject> gameObjects = new BigList<GameObject>();

    // object
    public final int id = lastObjId++;
    Entity entity;

    public GameObject(Object... components)
    {
        if(lastObjId > Integer.MAX_VALUE - 4)
        {
            RuntimeException e = new RuntimeException("Run out of GameObject indexes!!!");
            throw e;
        }
        entity = ECS.ecs.createEntity(components);
        gameObjects.add(id, this);
    }

    public static BigList<GameObject> getAllGameObjects()
    {
        return gameObjects;
    }

    public Entity getEntity()
    {
        return entity;
    }
}
