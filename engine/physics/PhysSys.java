package engine.physics;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With2;
import engine._3d.Mesh;
import engine._3d.Vertex;
import engine.components.Position;
import engine.components.Renderable;
import engine.ecs.ScheduledSystem;

public class PhysSys extends ScheduledSystem<With2< Physics, Renderable >>
{
    
    static class ComputeableCollisionBox extends CollisionBox
    {
        public ComputeableCollisionBox()
        {
            super(null, null);
        }
        public ComputeableCollisionBox(Vector3f min, Vector3f max, With2<Physics, Renderable> value)
        {
            super(min, max);
            comp = value;
        }
        With2<Physics, Renderable> comp;
    }

    ComputeableCollisionBox[] bodies;
    int bodiesIndex = 0;
    int cIndex;
    
    @Override
    public Results<With2<Physics, Renderable>> requestData(Dominion ecs)
    {
        Results<With2<Physics, Renderable>> components = ecs.findEntitiesWith(Physics.class, Renderable.class);
        int i = 0;
        for(With2<Physics, Renderable> e : components) // TODO find better solution
        {
            i++;
        }
        bodies = new ComputeableCollisionBox[i];
        bodiesIndex = 0;
        return components;
    }

    @Override
    public void execute(With2<Physics, Renderable> result, double deltaTime)
    {
        Physics target = result.comp1();
        Renderable renderable = result.comp2();
        target.showBox(result.entity());

        if(target.collisionBox == null)
        {
            return;
        }
        CollisionBox box = target.collisionBox;

        Matrix3f rotationMatrix = new Matrix3f();
        rotationMatrix.scale(renderable.scale).rotateXYZ(renderable.rotation);

        Vector3f[] boxVerticles = new Vector3f[]
        {
            new Vector3f( box.start.x, box.start.y, box.end.z  ),
            new Vector3f( box.start.x, box.end.y  , box.end.z  ),
            new Vector3f( box.start.x, box.start.y, box.start.z),
            new Vector3f( box.start.x, box.end.y  , box.start.z),
            new Vector3f( box.end.x  , box.start.y, box.end.z  ),
            new Vector3f( box.end.x  , box.end.y  , box.end.z  ),
            new Vector3f( box.end.x  , box.start.y, box.start.z),
            new Vector3f( box.end.x  , box.end.y  , box.start.z)
        };

        float negX = 0;
        float posX = 0;
        float negY = 0;
        float posY = 0;
        float negZ = 0;
        float posZ = 0;
        for(int i = 0; i < boxVerticles.length; i++) // rotating all verticels to get new bounding box and calculate it
        {
            rotationMatrix.transform(boxVerticles[i]);
            if(boxVerticles[i].x < 0 && boxVerticles[i].x < negX)
            {
                negX = boxVerticles[i].x;
            }
            else if(boxVerticles[i].x > 0 && boxVerticles[i].x > posX)
            {
                posX = boxVerticles[i].x;
            }
            
            if(boxVerticles[i].y < 0 && boxVerticles[i].y < negY)
            {
                negY = boxVerticles[i].y;
            }
            else if(boxVerticles[i].y > 0 && boxVerticles[i].y > posY)
            {
                posY = boxVerticles[i].y;
            }

            if(boxVerticles[i].z < 0 && boxVerticles[i].z < negZ)
            {
                negZ = boxVerticles[i].z;
            }
            else if(boxVerticles[i].z > 0 && boxVerticles[i].z > posZ)
            {
                posZ = boxVerticles[i].z;
            }
        }

        Vector3f min = new Vector3f(negX, negY, negZ);
        Vector3f max = new Vector3f(posX, posY, posZ);

        if(target.boxMesh != null && target.showHitBox)
        {
            updateMesh(target.boxMesh, rotationMatrix.invert(), min, max);
        }

        bodies[bodiesIndex++] = new ComputeableCollisionBox(min.add(renderable.translation), max.add(renderable.translation), result);
    }

    @Override
    public void onEnd()
    {
        for(int i = 0; i < bodiesIndex; i++)
        {
            for(int j = i + 1; j < bodiesIndex; j++)
            {
                if(CollisionBox.collision(bodies[i], bodies[j]))
                {
                    Matrix3f rotationMatrixK = new Matrix3f().scale(bodies[i].comp.comp2().scale).rotateXYZ(bodies[i].comp.comp2().rotation);
                    Matrix3f rotationMatrixL = new Matrix3f().scale(bodies[j].comp.comp2().scale).rotateXYZ(bodies[j].comp.comp2().rotation);
                    Vector3f positionK = bodies[i].comp.comp2().translation;
                    Vector3f positionL = bodies[j].comp.comp2().translation;

                    for(int l = 0; l < bodies[j].comp.comp1().convexObjects.size(); l++)
                        bodies[j].comp.comp1().convexObjects.get(l).transform(rotationMatrixL, positionL);

                    for(int k = 0; k < bodies[i].comp.comp1().convexObjects.size(); k++)
                    {
                        bodies[i].comp.comp1().convexObjects.get(k).transform(rotationMatrixK, positionK);
                        for(int l = k; l < bodies[j].comp.comp1().convexObjects.size(); l++)
                        {
                            if(GJK.intersect(bodies[i].comp.comp1().convexObjects.get(k), bodies[j].comp.comp1().convexObjects.get(l)))
                            {
                                Vector3f direction = new Vector3f(bodies[j].comp.comp1().convexObjects.get(l).center()).sub(bodies[i].comp.comp1().convexObjects.get(k).center()).normalize();
                                bodies[i].comp.entity().get(Position.class).direction.cross(direction);
                                direction.mul(-1f);
                                bodies[j].comp.entity().get(Position.class).direction.cross(direction);
                                System.out.println("Collision!" + cIndex++ + " part1:" + i + " part2:" + j);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void updateMesh(Mesh boundingBox, Matrix3f rotation, Vector3f min, Vector3f max)
    {
        Vector3f[] boxVerticles = new Vector3f[]
        {
            new Vector3f( min.x , min.y , max.z ),
            new Vector3f( min.x , max.y , max.z ),
            new Vector3f( min.x , min.y , min.z ),
            new Vector3f( min.x , max.y , min.z ),
            new Vector3f( max.x , min.y , max.z ),
            new Vector3f( max.x , max.y , max.z ),
            new Vector3f( max.x , min.y , min.z ),
            new Vector3f( max.x , max.y , min.z )
        };
        int i = 0;
        for(Vertex v : boundingBox)
        {
            if(i < 8)
            {
                v.n(rotation.transform(boxVerticles[i++]));
            }
        }
        boundingBox.flush();
    }
    
}
