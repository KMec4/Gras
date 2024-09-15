package engine.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.joml.Vector3f;

import engine._3d.Mesh;
import engine._3d.MeshUtils;
import engine._3d.Vertex;
import engine.components.Renderable;
import engine.ecs.GameObject;
import engine.world.MaterialPicker.BiomMap;
import lib.noise_generator.PerlinNoise;

public class Chunk extends GameObject
{
    int chunkSizeX = 100; //Chunk Size in verticles
    int chunkSizeY = 100;

    int maxDisplayedLOD = 10;

    float[][] heights;

    public void setLOD(int levelOfDetail)
    {

    }

    private Chunk(float[][] values)
    {
        heights = values;
    }

    public static Chunk loadChunk(File f)
    {
        return null;
    }

    public void saveChunk(File f)
    {
        saveChunk(this, f);
    }


    // static

    public static Chunk generateChunk(int sizeX, int sizeY, float heightScaler, double seed, int offsetX, int offsetY)
    {
        float[][] heights = new float[sizeX][sizeY];
        PerlinNoise noiseGen = new PerlinNoise(seed);
        Vector3f[] positions = new Vector3f[sizeX * sizeY];
        for(int x = 0; x < sizeX; x++)
        {
            for(int y = 0; y < sizeY; y++)
            {
                heights[x][y] = (float) noiseGen.noise(x + offsetX, y + offsetY) * heightScaler;
                positions[(y * sizeX) + x] = new Vector3f(x, heights[x][y], y);
            }
        }
        
        int[] indices = new int[sizeX * sizeY * 6];
        for(int x = 0; x < sizeX - 1; x++)
        {
            for(int y = 0; y < sizeY - 1; y++)
            {
                    indices[((y*sizeX) + x) * 6 + 0] = x + 1 + ((y + 1) * sizeX);
                    indices[((y*sizeX) + x) * 6 + 1] = x + 1 + ((y + 0) * sizeX);
                    indices[((y*sizeX) + x) * 6 + 2] = x + 0 + ((y + 0) * sizeX);

                    indices[((y*sizeX) + x) * 6 + 3] = x + 0 + ((y + 1) * sizeX);
                    indices[((y*sizeX) + x) * 6 + 4] = x + 1 + ((y + 1) * sizeX);
                    indices[((y*sizeX) + x) * 6 + 5] = x + 0 + ((y + 0) * sizeX);
            }
        }

        Vector3f[] normals = MeshUtils.calculateNormals(positions, indices);

        Mesh parent = new Mesh(positions.length);
        int i = 0;
        Vector3f color = new Vector3f(1f);
        for(Vertex v : parent)
        {
            v.n(positions[i]);
            v.n(color);
            v.n(normals[i]);
            i++;
        }
        parent.setIndices(indices);
        parent.flush();
        //parent.setMaterial(Material.getMaterial("GRAS"));
        /*
        new GameObject(
            new Renderable(parent)
        );*/

        for(Mesh m : MaterialPicker.splitMesh(new BiomMap(), parent, positions, indices))
        {
            new GameObject(
                new Renderable(m)
            );
        }

        return new Chunk(heights);
    }

    public static void saveChunk(Chunk c, File f)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(f);
            String output = "Chunk\n" + c.heights.length + " " + c.heights[0].length + "\n";

            for(int y = 0; y < c.heights[0].length; y++)
            {
                for(int x = 0; x < c.heights.length; x++)
                {
                    output += c.heights[x][y] + " ";
                }
                output += "\n";
            }
            fos.write(output.getBytes());
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
