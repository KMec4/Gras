package engine.physics;

import org.joml.Vector3f;
import org.joml.Vector2f;

import engine._3d.Material;
import engine._3d.Mesh;

public class CollisionBox
{
    public Vector3f start;
    public Vector3f end;
    
    public CollisionBox(Vector3f start, Vector3f end)
    {
        this.start = start;
        this.end = end;
    }

    public static CollisionBox claculateCollisionBox(Mesh mesh, int posOffsetBytes)
    {
        float negX = 0;
        float posX = 0;
        float negY = 0;
        float posY = 0;
        float negZ = 0;
        float posZ = 0;

        for(int i = 0; i < mesh.getVertexCount(); i++)
        {
            Vector3f pos = new Vector3f( mesh.getMaterial().shaders.vertexBlueprint.size * i + posOffsetBytes, mesh.getBuffer().getBuffer() );
            if(pos.x < 0 && pos.x < negX)
            {
                negX = pos.x;
            }
            else if(pos.x > 0 && pos.x > posX)
            {
                posX = pos.x;
            }
            
            if(pos.y < 0 && pos.y < negY)
            {
                negY = pos.y;
            }
            else if(pos.y > 0 && pos.y > posY)
            {
                posY = pos.y;
            }

            if(pos.z < 0 && pos.z < negZ)
            {
                negZ = pos.z;
            }
            else if(pos.z > 0 && pos.z > posZ)
            {
                posZ = pos.z;
            }
        }
        return new CollisionBox(new Vector3f(negX, negY, negZ), new Vector3f(posX, posY, posZ));
    }

    public static boolean collision(CollisionBox c1, CollisionBox c2)
    {
        return
            c1.start.x <= c2.end.x   &&
            c1.end.x   >= c2.start.x &&
            c1.start.y <= c2.end.y   &&
            c1.end.y   >= c2.start.y &&
            c1.start.z <= c2.end.z   &&
            c1.end.z   >= c2.start.z ;
    }

    public static Mesh generateCollisionBox(CollisionBox c)
    {
        Mesh mesh = new Mesh(9, Material.getMaterial("SEMI-TRANSPARENT"), new int[]
        {
            0, 4, 6,
            3, 2, 6,
            7, 6, 4,
            5, 1, 3,
            1, 0, 2,
            5, 4, 0,

            0, 6, 2,
            3, 6, 7,
            7, 4, 5,
            5, 3, 7,
            1, 2, 3,
            5, 0, 1
        } );

        mesh.next().n( new Vector3f( c.start.x, c.start.y, c.end.z  )).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(1f, 0f));
        mesh.next().n( new Vector3f( c.start.x, c.end.y  , c.end.z  )).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(0f, 0f));
        mesh.next().n( new Vector3f( c.start.x, c.start.y, c.start.z)).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(1f, 1f));
        mesh.next().n( new Vector3f( c.start.x, c.end.y  , c.start.z)).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(1f, 0f));
        mesh.next().n( new Vector3f( c.end.x  , c.start.y, c.end.z  )).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(0f, 0f));
        mesh.next().n( new Vector3f( c.end.x  , c.end.y  , c.end.z  )).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(0f, 1f));
        mesh.next().n( new Vector3f( c.end.x  , c.start.y, c.start.z)).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(0f, 1f));
        mesh.next().n( new Vector3f( c.end.x  , c.end.y  , c.start.z)).n(new Vector3f()).n(new Vector3f()).n(new Vector2f(1f, 1f));

        mesh.flush();

        return mesh;
    }
}