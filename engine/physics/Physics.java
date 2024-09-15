package engine.physics;

import java.util.ArrayList;

import org.joml.Vector3f;

import dev.dominion.ecs.api.Entity;
import engine._3d.Mesh;
import engine.components.Renderable;
import engine.ecs.GameObject;
import engine.physics.GJK.GJKComponent;

public class Physics
{
    public boolean showHitBox = true;

    public CollisionBox collisionBox;
    public Vector3f centerOfMass;

    Vector3f velocity;
    Mesh boxMesh;
    ArrayList<GJKComponent> convexObjects;
    float mass = 1.0f; // in gram
    
    public Physics()
    {
        convexObjects = new ArrayList<>();
    }

    public void showBox(Entity gameObject)
    {
        if(collisionBox == null)
        {
            return;
        }
        if(gameObject.has(Renderable.class))
        {
            Renderable renderable = gameObject.get(Renderable.class);
            if( boxMesh == null )
            {
                boxMesh = CollisionBox.generateCollisionBox(collisionBox);
            }
            else
            {
                for(Mesh m : renderable.model)
                {
                    if(m == boxMesh)
                    {
                        return;
                    }
                }
            }
            Mesh[] tmp = new Mesh[renderable.model.length + 1];
            System.arraycopy(renderable.model, 0, tmp, 0, renderable.model.length);
            tmp[renderable.model.length] = boxMesh;
            renderable.model = tmp;
        }
    }


    public void showBox(GameObject gameObject)
    {
        showBox(gameObject.getEntity());
    }


    public void setMeshForPhysics(int posOffsetBytes, Mesh mesh)
    {
        collisionBox = CollisionBox.claculateCollisionBox(mesh, posOffsetBytes);
        centerOfMass = centerOfMass(mesh, posOffsetBytes);
        Vector3f[] vertexData = new Vector3f[mesh.getVertexCount()];
        for(int i = 0; i < vertexData.length; i++)
        {
            vertexData[i] = new Vector3f((mesh.getMaterial().shaders.vertexBlueprint.size * i + posOffsetBytes) / 4, mesh.getBuffer().getBuffer());
        }
        convexObjects.add(new GJK.SimpleVertexComponent(vertexData, centerOfMass));
    }

    public void setMeshForPhysics(int posOffsetBytes, Mesh... meshs)
    {

    }

    public void setMeshForPhysics(Mesh mesh)
    {
        setMeshForPhysics(0, mesh);
    }

    public void setMeshForPhysics(Mesh... meshs)
    {
        setMeshForPhysics(0, meshs);
    }

    public static Vector3f centerOfMass(Mesh mesh, int posOffsetBytes)
    {
        Vector3f sum = new Vector3f();
        for(int i = 0; i < mesh.getVertexCount(); i++)
        {
            sum.add(new Vector3f( mesh.getMaterial().shaders.vertexBlueprint.size * i + posOffsetBytes, mesh.getBuffer().getBuffer() ));
        }
        return sum.div(mesh.getVertexCount());
    }
}
