package engine.vulkan;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.components.Position;
import engine.components.Rotateable;
import engine.ecs.GameObject;
import engine.components.Moveable;

public class Viewport extends GameObject
{
    Matrix4f projectionMatrix = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f,
                                             0.0f, 1.0f, 0.0f, 0.0f,
                                             0.0f, 0.0f, 1.0f, 0.0f,
                                             0.0f, 0.0f,0.0f, 1.0f );

    Matrix4f viewMatrix       = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f,
                                             0.0f, 1.0f, 0.0f, 0.0f,
                                             0.0f, 0.0f, 1.0f, 0.0f,
                                             0.0f, 0.0f,0.0f, 1.0f );

    Vector3f pos = new Vector3f(0.0f, .0f, 0.0f);
    Vector3f dir = new Vector3f(0.0f, .0f, 1.0f);
    Vector3f up  = new Vector3f(.0f,  -1.f, .0f);

    Vector3f rotate = new Vector3f(.0f, .0f, .0f);

    float speed = .00f;


    public void setOrthographicProjection( float left, float right, float top, float bottom, float near, float far)
    {
        projectionMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        projectionMatrix.m00( 2.f / (right - left)              );
        projectionMatrix.m11( 2.f / (bottom - top)              );
        projectionMatrix.m22( 1.f / (far - near)                );
        projectionMatrix.m30( -(right + left) / (right - left)  );
        projectionMatrix.m31( -(bottom + top) / (bottom - top)  );
        projectionMatrix.m32( -near / (far - near)              );
    }

    public void setPerspectiveProjection(float fovy, float aspect, float near, float far)
    {
        projectionMatrix.setPerspective(fovy, aspect, near, far);
    }

    public void setViewDirection()
    {
        viewMatrix.identity()
                .rotateX(rotate.x)
                .rotateY(rotate.y)
                .translate(pos.x, pos.y, pos.z);

                
        dir.set( viewMatrix.positiveZ(dir).negate() );
        dir.normalize();
      }
      
    public void setViewTarget(Vector3f target) //TODO target - pos != 0
    {
        Vector3f w = new Vector3f();
        target.sub(pos, w);
        w.normalize();

        Vector3f u = new Vector3f();
        w.cross(up, u);
        u.normalize();

        Vector3f v = new Vector3f();
        w.cross(u, v);


        viewMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );

        viewMatrix.m00( u.x );
        viewMatrix.m10( u.y );
        viewMatrix.m20( u.z );
        viewMatrix.m01( v.x );
        viewMatrix.m11( v.y );
        viewMatrix.m21( v.z );
        viewMatrix.m02( w.x );
        viewMatrix.m12( w.y );
        viewMatrix.m22( w.z );
        viewMatrix.m30( u.dot(pos) );
        viewMatrix.m31( v.dot(pos) );
        viewMatrix.m32( w.dot(pos) );
    }
      
    //GETTERS

    public Matrix4f getProjectionMatrix()
    {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }

    //SETTERS

    public void setSpeedM_S(float speed)
    {
        getEntity().get(Moveable.class).velocity = -speed;
    }

    public void setRotation(Vector2f rot)
    {
        //rotate.x = rot.x;
        //rotate.y = rot.y;
    }
    
    public Viewport()
    {
        super(
            new Position(),
            new Moveable(),
            new Rotateable()
        );

        getEntity().get(Position.class).direction = dir;
        getEntity().get(Moveable.class).direction = dir;
        getEntity().get(Position.class).position = pos;
        getEntity().get(Rotateable.class).rotation = rotate;
    }
}
