package engine.components;

import org.joml.Vector3f;

public class Moveable
{
    /*
     * in meters per secound
     */
    public float velocity = 0f;
    public Vector3f direction;
    {
        direction = new Vector3f(0f, 0f, 0f);
    }
}
