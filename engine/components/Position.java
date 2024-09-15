package engine.components;

import org.joml.Vector3f;

public class Position
{
    public Vector3f position;
    public Vector3f direction;

    public Position(Vector3f position, Vector3f direction)
    {
        this.position = position;
        this.direction = direction;
    }
    public Position()
    {
        position = new Vector3f();
        direction = new Vector3f(0f, 0f, 1f);
    }
}
