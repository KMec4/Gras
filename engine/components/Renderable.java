package engine.components;

import org.joml.Vector3f;

import engine._3d.Mesh;

public class Renderable
    {
    public Vector3f translation;
    public Vector3f scale;
    public Vector3f rotation;

    public Vector3f color;
    public Mesh[]  model;

    public Renderable(Mesh model)
    {
        this.model = new Mesh[1];
        this.model[0] = model;
        translation = new Vector3f();
        scale = new Vector3f(1f);
        rotation = new Vector3f((float) Math.PI, 0f, 0f);
        color = new Vector3f(1f);
    }
}
