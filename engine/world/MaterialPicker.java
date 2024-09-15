package engine.world;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine._3d.Material;
import engine._3d.Mesh;
import engine._3d.SharedMesh;

public class MaterialPicker
{
    public static class MaterialProfile
    {
        Material targetMaterial;
        float minSlope = 0;   //in radians
        float maxSlope = 0;   //in radians
        float minSun;
        float maxSun;
        float minWetness;
        float maxWetness;
    }

    public static class BiomProfile
    {
        MaterialProfile[] materials;
        Material replacementMaterial;
        float maxTemperature;
        float minTemperature;
        float maxHight;
        float minHight;
    }

    public static class BiomMap
    {
        BiomProfile std = new BiomProfile();
        {
            MaterialProfile mp1 = new MaterialProfile();
            mp1.targetMaterial = Material.getMaterial("GRAS");
            mp1.maxSlope = 0.2f;

            MaterialProfile mp2 = new MaterialProfile();
            mp2.targetMaterial = Material.getMaterial("NORMALS");
            mp2.minSlope = 0.2f;
            mp2.maxSlope = 10f;

            std.materials = new MaterialProfile[2];
            std.materials[0] = mp1;
            std.materials[1] = mp2;
            std.replacementMaterial = Material.getMaterial("DEFAULT");
        }

        public BiomProfile getBiomAt(Vector2f pos)
        {
            return std;
        }
    }

    public static boolean isMaterialAppropriated(MaterialProfile profile, Vector3f pos, Vector3f normal)
    {
        float slope = Math.acos(normal.y);
        return profile.minSlope < slope && profile.maxSlope > slope;
    }

    public static Mesh[] splitMesh(BiomMap bMap, Mesh parent, Vector3f[] pos, int[] indices)
    {
        ArrayList<MaterialProfile> materials = new ArrayList<MaterialProfile>();
        ArrayList<int[]> indicesOut = new ArrayList<int[]>();
        int[] indexForIndices = new int[0];

        for(int i = 0; i < indices.length; i += 3)
        {
            int indexA = indices[i + 0];
            int indexB = indices[i + 1];
            int indexC = indices[i + 2];

            Vector3f pointC = pos[indexC];
            Vector3f normal = new Vector3f(pos[indexA]).sub(pointC).cross(new Vector3f(pos[indexB]).sub(pointC)).normalize();

            Vector3f pos3D = new Vector3f(pos[indexA]).add(pos[indexB]).add(pos[indexC]).div(3f);
            Vector2f pos2D = pos3D.xy(new Vector2f());

            for(MaterialProfile profile : bMap.getBiomAt(pos2D).materials)
            {
                if(isMaterialAppropriated(profile, pos3D, normal))
                {
                    int index = materials.indexOf(profile);
                    if(index < 0)
                    {
                        index = materials.size();
                        materials.add(profile);
                        indicesOut.add(new int[indices.length]);
                        int[] tmp = new int[index + 1];
                        System.arraycopy(indexForIndices, 0, tmp, 0, indexForIndices.length);
                        indexForIndices = tmp;
                    }

                    indicesOut.get(index)[indexForIndices[index]++] = indexA;
                    indicesOut.get(index)[indexForIndices[index]++] = indexB;
                    indicesOut.get(index)[indexForIndices[index]++] = indexC;
                }
            }
        }

        ArrayList<Mesh> output = new ArrayList<Mesh>(materials.size());
        for(int i = 0; i < materials.size(); i++)
        {
            if(indexForIndices[i] == 0)
            {
                continue;
            }

            SharedMesh mesh = new SharedMesh(parent);
            mesh.setMaterial(materials.get(i).targetMaterial);
            if(indexForIndices[i] != indices.length)
            {
                int[] tmp = new int[indexForIndices[i]];
                System.arraycopy(indicesOut.get(i), 0, tmp, 0, indexForIndices[i]);
                mesh.setIndices(tmp);
            }
            else
            {
                mesh.setIndices(indicesOut.get(i));
            }
            output.add(mesh);
        }
        Mesh[] arrayOutput = new Mesh[output.size()];
        return output.toArray(arrayOutput);
    }
}
