package engine._3d;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex
{
    private int offset;
    private int index;
    Mesh m;

    public Vertex(Mesh mesh, int offset)
    {
        this.offset = offset;
        m = mesh;
    }

    public Vertex n(int i)
    {
        m.intbuffer.put((offset+index) / 4, i);
        index += 4;
        return this;
    }
    public Vertex n(float f)
    {
        m.floatbuffer.put((offset+index) / 4, f);
        index += 4;
        return this;
    }
    public Vertex n(Vector3f vec3)
    {
        vec3.get(offset + index, m.buffer);
        index += 12;
        return this;
    }
    public Vertex n(Vector2f vec2)
    {
        vec2.get(offset + index, m.buffer);
        index += 8;
        return this;
    }
}