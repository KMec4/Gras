package engine._3d;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

import engine.Vulkan;
import engine.vulkan.Buffer;
import engine.vulkan.Buffer.BufferProperties;

public class SharedMesh extends Mesh
{
    Mesh parentMesh;

    public SharedMesh(Mesh parent)
    {
        super();
        super.buffer = parent.buffer;
        super.floatbuffer = parent.floatbuffer;
        super.intbuffer = parent.intbuffer;
        super.position = 0;

        super.vertexCount = parent.vertexCount;
        super.vertexData = parent.vertexData;

        super.indexData = parent.indexData;
        super.indicesCount = parent.indicesCount;

        super.material = parent.material;

        parentMesh = parent;
    }

    @Override
    public void setIndices(int[] indices)
    {
        if(indicesCount > 0 && indexData != parentMesh.indexData)
        {
            indexData.free();
            indicesCount = 0;
        }
        if(indices == null)
        {
            return;
        }
        indexData = Buffer.callocBuffer(Vulkan.getRenderDevice(), new BufferProperties(true, indices.length * 4, VK_BUFFER_USAGE_INDEX_BUFFER_BIT, 0));
        indexData.getBuffer().asIntBuffer().put(indices);
        indexData.flush();
        indicesCount = indices.length;
    }

    public void resetIndicesToParent()
    {
        if(indicesCount > 0 && indexData != parentMesh.indexData)
        {
            indexData.free();
        }
        indexData = parentMesh.indexData;
        indicesCount = parentMesh.indicesCount;
    }

    public void resetMaterialToParent()
    {
        material = parentMesh.material;
    }
}
