package engine.physics;

import org.joml.Matrix3f;
import org.joml.Vector3f;

public class GJK
{
    public static interface GJKComponent
    {
        public Vector3f center();
        public Vector3f findFurthestPoint(Vector3f dir);
        public void transform(Matrix3f transformMatrix, Vector3f position);
    }

    public static class SphereComponent implements GJKComponent
    {
        float radius;
        Matrix3f transform;
        Vector3f position;

        public SphereComponent(float radius)
        {
            this.radius = radius;
            transform(new Matrix3f().identity(), new Vector3f());
        }

        @Override
        public Vector3f center()
        {
            return new Vector3f(position);
        }

        @Override
        public Vector3f findFurthestPoint(Vector3f dir)
        {
            return transform.transform( new Vector3f(dir).mul(radius) ).add(position);
        }

        @Override
        public void transform(Matrix3f transformMatrix, Vector3f position)
        {
            transform = transformMatrix;
            this.position = position;
        }
        
    }

    public static class SimpleVertexComponent implements GJKComponent
    {
        Vector3f center;
        Vector3f transformedCenter;
        Vector3f[] shape;
        Vector3f[] transformedShape;
        
        public SimpleVertexComponent(Vector3f[] vertexData, Vector3f center)
        {
            shape = vertexData;
            this.center = center;
            transformedShape = new Vector3f[shape.length];
            transform(new Matrix3f().identity(), new Vector3f());
        }

        @Override
        public void transform(Matrix3f transformMatrix, Vector3f position)
        {
            for(int i = 0; i < shape.length; i++)
            {
                transformedShape[i] = transformMatrix.transform(new Vector3f(shape[i])).add(position);
            }
            transformedCenter = transformMatrix.transform(new Vector3f(center)).add(position);
        }

        @Override
        public Vector3f findFurthestPoint(Vector3f dir)
        {
            int index = 0;
            float maxDot = transformedShape[0].dot(dir);
            for(int i = 1; i < transformedShape.length; i++)
            {
                float dot = transformedShape[i].dot(dir);
                if(dot > maxDot)
                {
                    index = i;
                }
            }
            return new Vector3f(transformedShape[index]);
        }

        @Override
        public Vector3f center()
        {
            return transformedCenter;
        }
        
    }

    public static boolean intersect(GJKComponent convexShape1, GJKComponent convexShape2)
    {
        Vector3f center1 = convexShape1.center(); //calculateCenter(convexShape1);
        Vector3f center2 = convexShape2.center(); //calculateCenter(convexShape1);
        if(center1.equals(center2, 0))
        {
            return true; // because else the algorythm fails in an infinit loop
        }
        Vector3f direction = new Vector3f(center2).sub(center1).normalize();

        Vector3f[] simplex = new Vector3f[4]; //D, C, B, A
        simplex[3] = findSupportPoint(convexShape1, convexShape2, direction);
        direction = new Vector3f(0f).sub(simplex[3]);

        for(;;)
        {
            Vector3f support = findSupportPoint(convexShape1, convexShape2, direction);
            if(support.dot(direction) < 0)
                return false;
            appendToSimplex(simplex, support);

            Vector3f ao = new Vector3f().sub(simplex[3]);
            switch(countPoints(simplex))
            {
                case 2: //LINE
                    Vector3f ab = new Vector3f(simplex[2]).sub(simplex[3]);
                    if(ab.dot(ao) > 0)
                    {
                        direction = new Vector3f(ab).cross(ao).cross(ab);
                    }
                    else
                    {
                        simplex[2] = null;
                        direction = ao;
                    }
                    break;
                case 3: //TRIANGLE
                    direction = processTriangle(simplex);
                    break;
                case 4: //TETRAHEDRON
                    Vector3f abc = new Vector3f(simplex[2]).sub(simplex[3]).cross(new Vector3f(simplex[1]).sub(simplex[3]));
                    Vector3f acd = new Vector3f(simplex[1]).sub(simplex[3]).cross(new Vector3f(simplex[0]).sub(simplex[3]));
                    Vector3f adb = new Vector3f(simplex[0]).sub(simplex[3]).cross(new Vector3f(simplex[2]).sub(simplex[3]));
                    if( abc.dot(ao) > 0 )
                    {
                        simplex[0] = null;
                        direction = processTriangle(simplex);
                    }
                    else if( acd.dot(ao) > 0 )
                    {
                        simplex[2] = simplex[1];
                        simplex[1] = simplex[0];
                        simplex[0] = null;
                        direction = processTriangle(simplex);
                    }
                    else if( adb.dot(ao) > 0 )
                    {
                        simplex[1] = simplex[2];
                        simplex[2] = simplex[0];
                        simplex[0] = null;
                        direction = processTriangle(simplex);
                    }
                    else
                    {
                        return true;
                    }
                    break;
                default:
                    return false; // Something went wrong
            }
            
        }
    }

    private static Vector3f processTriangle(Vector3f[] simplex)
    {
        Vector3f ac = new Vector3f(simplex[1]).sub(simplex[3]);
        Vector3f abc = new Vector3f(simplex[2]).sub(simplex[3]).cross(ac);
        Vector3f ao = new Vector3f().sub(simplex[3]);

        if(new Vector3f(abc).cross(ac).dot(ao) > 0)
        {
            if(ac.dot(ao) > 0)
            {
                simplex[2] = simplex[3];
                simplex[3] = null;
                return new Vector3f(ac).cross(ao).cross(ac);
            }
            else
            {
                Vector3f ab = new Vector3f(simplex[2]).sub(simplex[3]);
                if(ab.dot(ao) > 0)
                {
                    simplex[1] = null;
                    return new Vector3f(ab).cross(ao).cross(ab);
                }
                else
                {
                    simplex[2] = null;
                    simplex[1] = null;
                    return ao;
                }
            }
        }
        else
        {
            Vector3f ab = new Vector3f(simplex[2]).sub(simplex[3]);
            if(new Vector3f(ab).cross(abc).dot(ao) > 0)
            {
                if(ab.dot(ao) > 0)
                {
                    simplex[1] = null;
                    return new Vector3f(ab).cross(ao).cross(ab);
                }
                else
                {
                    simplex[2] = null;
                    simplex[1] = null;
                    return ao;
                }
            }
            else
            {
                if(abc.dot(ao) > 0)
                {
                    return abc;
                }
                else
                {
                    Vector3f tmp = simplex[2];
                    simplex[2] = simplex[1];
                    simplex[1] = tmp;
                    return abc.mul(-1f);
                }
            }
        }
    }

    private static int countPoints(Vector3f[] simplex)
    {
        int count = 0;
        for(Vector3f v : simplex)
        {
            if(v != null)
                count++;
        }
        return count;
    }

    /**
     * Adds @param toAdd to the last index of @param simplex and shifts it one back.
     */
    private static void appendToSimplex(Vector3f[] simplex, Vector3f toAdd)
    {
        for(int i = 1; i < simplex.length; i++)
        {
            simplex[i - 1] = simplex[i];
        }
        simplex[simplex.length - 1] = toAdd;
    }

    private static Vector3f findSupportPoint(GJKComponent shape1, GJKComponent shape2, Vector3f direction)
    {
        return shape1.findFurthestPoint(direction).sub(shape2.findFurthestPoint( new Vector3f(direction).mul(-1f)));
    }

}
