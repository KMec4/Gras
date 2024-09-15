package engine.components;

import org.joml.Vector3f;

public class Rotateable
{
    public Vector3f rotation;
    public Vector3f speeds;
    public Rotateable(Vector3f rotation, Vector3f speeds) { this.rotation = rotation; this.speeds = speeds; }
    public Rotateable() { this.rotation = new Vector3f(); this.speeds = new Vector3f(); }
}