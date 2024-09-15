package engine._3d;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;


public class MeshUtils
{
    public static Mesh loadMeshFromFile(File obj, Vector3f color)
    {
        try(MemoryStack stack = stackPush())
        {
            Obj modelDat;
            try
            {
                modelDat = ObjUtils.convertToRenderable(ObjReader.read(new FileInputStream(obj)));
            }
            catch (IOException e)
            {
                RuntimeException e1 = new RuntimeException("can't create 3dModel from " + obj.getAbsolutePath(), e);
                throw e1;
            }


            FloatBuffer verticles = stack.callocFloat(modelDat.getNumVertices() * 3);
            ObjData.getVertices(modelDat, verticles);
            verticles.limit(modelDat.getNumVertices() * 3);
            verticles.rewind();

            FloatBuffer normals = stack.callocFloat(modelDat.getNumVertices() * 3);
            ObjData.getNormals(modelDat, normals);
            normals.limit(modelDat.getNumVertices() * 3);
            normals.rewind();

            //FloatBuffer uv = stack.callocFloat(modelDat.getNumVertices() * 2);
            //ObjData.getTexCoords(modelDat, uv);

            Mesh mesh = new Mesh(modelDat.getNumVertices(), Material.getMaterial("DEFAULT"), ObjData.getFaceVertexIndicesArray(modelDat));

            for(Vertex v : mesh)
            {
                v.n(verticles.get());
                v.n(verticles.get());
                v.n(verticles.get());
                v.n(color.x);
                v.n(color.y);
                v.n(color.z);
                v.n(normals.get());
                v.n(normals.get());
                v.n(normals.get());
                //v.n(uv.get());
                //v.n(uv.get());
            }

            mesh.flush();
            return mesh;
        }
    }

    public static Vector3f[] calculateNormals(Vector3f[] pos, int[] indices)
    {
        Vector3f[] normals = new Vector3f[pos.length];
        for(int i = 0; i < pos.length; i++)
        {
            normals[i] = new Vector3f(0f);
        }

        for(int i = 0; i < indices.length; i += 3)
        {
            int indexA = indices[i + 0];
            int indexB = indices[i + 1];
            int indexC = indices[i + 2];

            Vector3f pointC = pos[indexC];
            Vector3f normal = new Vector3f(pos[indexA]).sub(pointC).cross(new Vector3f(pos[indexB]).sub(pointC)).normalize();

            normals[indexA].add(normal);
            normals[indexB].add(normal);
            normals[indexC].add(normal);
        }
        for(Vector3f normal : normals)
        {
            normal.normalize();
        }
        return normals;
    }
}
