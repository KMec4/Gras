package engine._3dObjects;

import org.joml.Vector3f;

import engine._3d.Mesh;

public class Sphere extends Mesh
{
    public Sphere(float radius, Vector3f color)
    {
        super(12);

        float t = (1.0f + org.joml.Math.sqrt(5.0f)) / 2.0f;

        next().n( new Vector3f(-1f,  t,  0f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f(-1f,  t,  0f).normalize() );
        next().n( new Vector3f( 1f,  t,  0f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 1f,  t,  0f).normalize() );
        next().n( new Vector3f(-1f, -t,  0f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f(-1f, -t,  0f).normalize() );
        next().n( new Vector3f( 1f, -t,  0f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 1f, -t,  0f).normalize() );
        next().n( new Vector3f( 0f, -1f,  t).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 0f, -1f,  t).normalize() );
        next().n( new Vector3f( 0f,  1f,  t).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 0f,  1f,  t).normalize() );
        next().n( new Vector3f( 0f, -1f, -t).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 0f, -1f, -t).normalize() );
        next().n( new Vector3f( 0f,  1f, -t).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( 0f,  1f, -t).normalize() );
        next().n( new Vector3f( t,  0f, -1f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( t,  0f, -1f).normalize() );
        next().n( new Vector3f( t,  0f,  1f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f( t,  0f,  1f).normalize() );
        next().n( new Vector3f(-t,  0f, -1f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f(-t,  0f, -1f).normalize() );
        next().n( new Vector3f(-t,  0f,  1f).normalize(radius) ).n(new Vector3f(color)).n(new Vector3f(-t,  0f,  1f).normalize() );

        flush();

        setIndices( new int[]
        {
            0, 11, 5,
            0, 5, 1,
            0, 1, 7,
            0, 7, 10,
            0, 10, 11,
            1, 5, 9,
            5, 11, 4,
            11, 10, 2,
            10, 7, 6,
            7, 1, 8,
            3, 9, 4,
            3, 4, 2,
            3, 2, 6,
            3, 6, 8,
            3, 8, 9,
            4, 9, 5,
            2, 4, 11,
            6, 2, 10,
            8, 6, 7,
            9, 8, 1
        });
    }
    
}
