package engine._3d;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import engine.Vulkan;
import engine.vulkan.Buffer;
import engine.vulkan.Buffer.BufferProperties;

public class Mesh implements Iterable<Vertex>
{
    Buffer vertexData;
    Buffer indexData;

    ByteBuffer buffer;
    FloatBuffer floatbuffer;
    IntBuffer   intbuffer;

    Material material;

    int indicesCount;
    int vertexCount;
    int position = 0;

    /**
     * creates an empty Mesh Object
     */
    Mesh()
    {
    }

    public Mesh(int verticlesCount)
    {
        this(verticlesCount, Material.getMaterial("DEFAULT"));
    }

    public Mesh(int verticlesCount, Material m)
    {
        material = m;
        vertexData = Buffer.callocBuffer(Vulkan.getRenderDevice(), new BufferProperties(false, verticlesCount * material.shaders.vertexBlueprint.size, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 0)); //TODO make device local
        vertexCount = verticlesCount;
        System.getLogger("").log(Level.INFO, "\nGenerating new Mesh\n-------------------\nMaterial      : " + material.tag + "\nVertexCount: " + verticlesCount + "\n-------------------");
        buffer = vertexData.getBuffer();
        floatbuffer = buffer.asFloatBuffer();
        intbuffer = buffer.asIntBuffer();
    }

    public Mesh(int verticlesCount, Material m, int[] indices)
    {
        material = m;
        vertexData = Buffer.callocBuffer(Vulkan.getRenderDevice(), new BufferProperties(false, verticlesCount * material.shaders.vertexBlueprint.size, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 0));//TODO make device local
        vertexCount = verticlesCount;
        setIndices(indices);
        System.getLogger("").log(Level.INFO, "\nGenerating new Mesh\n-------------------\nMaterial      : " + material.tag + "\nVerticlesCount: " + verticlesCount + "\nIndicesCount  : " + indicesCount + "\n-------------------");
        buffer = vertexData.getBuffer();
        floatbuffer = buffer.asFloatBuffer();
        intbuffer = buffer.asIntBuffer();
    }


    /**
     * you have to flush to apply changes
     * it may be more efficient if you calculate normals by yourself!
     * @param vertexPositionOffsetBytes the position Vector's offset
     * @param vertexNormalOffsetBytes the normal Vector's offset
     */
    public void recalculateNormals(int vertexPositionOffsetBytes, int vertexNormalOffsetBytes)
    {
        IntBuffer indices = indexData.getBuffer().asIntBuffer();
        int vertexSizeBytes = material.shaders.vertexBlueprint.size;
        for(int i = 0; i < vertexCount; i++)
        {
            new Vector3f(0f).get((i * vertexSizeBytes + vertexNormalOffsetBytes) / 4, floatbuffer);
        }
        for(int i = 0; i < indicesCount; i += 3)
        {
            int indexA = indices.get(i + 0) * vertexSizeBytes;
            int indexB = indices.get(i + 1) * vertexSizeBytes;
            int indexC = indices.get(i + 2) * vertexSizeBytes;

            Vector3f pointC = new Vector3f((indexC + vertexPositionOffsetBytes) / 4, floatbuffer);
            Vector3f normal = new Vector3f((indexA + vertexPositionOffsetBytes) / 4, floatbuffer).sub(pointC).cross(new Vector3f((indexB + vertexPositionOffsetBytes) / 4, floatbuffer).sub(pointC)).normalize();

            Vector3f normalToSave = new Vector3f((indexA + vertexNormalOffsetBytes) / 4, floatbuffer).add(normal);
            normalToSave.get((indexA + vertexNormalOffsetBytes) / 4, floatbuffer);
            
            normalToSave = new Vector3f((indexB + vertexNormalOffsetBytes) / 4, floatbuffer).add(normal);
            normalToSave.get((indexB + vertexNormalOffsetBytes) / 4, floatbuffer);
            
            normalToSave = new Vector3f((indexC + vertexNormalOffsetBytes) / 4, floatbuffer).add(normal);
            normalToSave.get((indexC + vertexNormalOffsetBytes) / 4, floatbuffer);
        }
        for(int i = 0; i < vertexCount; i++)
        {
            Vector3f normalToNormalize = new Vector3f((i * vertexSizeBytes + vertexNormalOffsetBytes) / 4, floatbuffer).normalize();
            normalToNormalize.get((i * vertexSizeBytes + vertexNormalOffsetBytes) / 4, floatbuffer);
        }
    }

    public Vertex get(int index)
    {
        return new Vertex(this, index * material.shaders.vertexBlueprint.size);
    }

    public int getVertexCount()
    {
        return vertexCount;
    }

    public Buffer getBuffer()
    {
        return vertexData;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material m)
    {
        material = m;
    }

    public void setIndices(int[] indices)
    {
        if(indicesCount > 0)
        {
            indexData.free();
            indicesCount = 0;
        }
        if(indices == null)
        {
            return;
        }
        indexData = Buffer.callocBuffer(Vulkan.getRenderDevice(), new BufferProperties(false, indices.length * 4, VK_BUFFER_USAGE_INDEX_BUFFER_BIT, 0));
        indexData.getBuffer().asIntBuffer().put(indices);
        indexData.flush();
        indicesCount = indices.length;
    }


    public Vertex next()
    {
        return get(position++ % vertexCount);
    }
    public void setPosition(int pos)
    {
        position = pos % vertexCount;
    }

    public void flush()
    {
        vertexData.flush();
    }
    public void load()
    {
        vertexData.load();
    }

    @Override
    public Iterator<Vertex> iterator()
    {
        return new Iterator<Vertex>()
        {
            int index = 0;
            Mesh mesh;
            private Iterator<Vertex> setMesh(Mesh mesh)
            {
                this.mesh = mesh;
                return this;
            }
            @Override
            public boolean hasNext()
            {
                return index < vertexCount;
            }

            @Override
            public Vertex next()
            {
                return new Vertex(mesh, index++ * material.shaders.vertexBlueprint.size);
            }
        }.setMesh(this); // small hack (;
    }

    public void submitToCommandBuffer(VkCommandBuffer cmd) //TODO allow to seperate bind and draw call
    {
        try(MemoryStack stack = stackPush())
        {
            vkCmdBindVertexBuffers(cmd, 0, stack.longs(vertexData.getBufferPointer()), stack.longs(0));
            if(indicesCount > 0)
            {
                vkCmdBindIndexBuffer(cmd, indexData.getBufferPointer(), 0, VK_INDEX_TYPE_UINT32);
                vkCmdDrawIndexed(cmd, indicesCount, 1, 0, 0, 0);
            }
            else
            {
                vkCmdDraw             (cmd, vertexCount, 1, 0, 0);
            }
        }
    }
}
