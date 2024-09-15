package engine._3d;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With1;
import dev.dominion.ecs.api.Results.With2;
import engine.components.Renderable;
import engine.ecs.ECSSystem;
import engine.vulkan.Descriptors.DescriptorAllocator;
import engine.vulkan.Descriptors.DescriptorSetLayout;
import engine.vulkan.Buffer;
import engine.vulkan.Descriptors;
import engine.vulkan.Buffer.BufferProperties;
import engine.vulkan.Device;
import engine.vulkan.Viewport;

public class RenderSystem3d extends ECSSystem<With1<Renderable>>
{

    Device dev;
    long renderPass;
    private VkCommandBuffer cmd;
    private Viewport viewport;
    boolean isRunning = false;
    ArrayList<SubRenderSystem> systems = new ArrayList<SubRenderSystem>(16);
    
    DescriptorAllocator globalDescriptorAllocator;
    DescriptorSetLayout globalDescriptorLayout;

    Buffer[] globalUniformBuffers;
    long[] uniformBufferDescriptors;

    int frameIndex = 0;
    int MAX_FRAMES_IN_FLIGHT = 2;

    class GlobalUniformBuffer
    {
        Buffer backend;
        final int SIZE = 96;

        public GlobalUniformBuffer()
        {
            backend = Buffer.callocBuffer(dev, new BufferProperties(true, SIZE, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, 0) );
        }
    }


    public RenderSystem3d(Device d, long renderPass)
    {
        dev = d;
        this.renderPass = renderPass;
        this.globalDescriptorAllocator = new DescriptorAllocator(dev);
        globalDescriptorLayout = new DescriptorSetLayout(dev);
        globalDescriptorLayout.nextBinding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_ALL_GRAPHICS);
        globalDescriptorLayout.build();

        globalUniformBuffers = new Buffer[MAX_FRAMES_IN_FLIGHT];
        uniformBufferDescriptors = new long[MAX_FRAMES_IN_FLIGHT];

        for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            globalUniformBuffers[i] = Buffer.callocBuffer(dev, new BufferProperties(true, 96, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, 0));
            uniformBufferDescriptors[i] = Descriptors.Utils.createDescriptor(new Buffer[] {globalUniformBuffers[i]} , new int[] {0}, globalDescriptorLayout, globalDescriptorAllocator);
        }

        try
        {
            addMaterial(
                new Material( "engine/_3d/shaders/vert.spirv", "engine/_3d/shaders/frag.spirv", "DEFAULT" )
                {
                    @Override
                    public void getPushConstantData(ByteBuffer dest, With1<Renderable> obj)
                    {
                        Renderable renderable = obj.comp();
                        float c3 = org.joml.Math.cos( renderable.rotation.z);
                        float s3 = org.joml.Math.sin( renderable.rotation.z);
                        float c2 = org.joml.Math.cos( renderable.rotation.x);
                        float s2 = org.joml.Math.sin( renderable.rotation.x);
                        float c1 = org.joml.Math.cos( renderable.rotation.y);
                        float s1 = org.joml.Math.sin( renderable.rotation.y);
                        Vector3f invScale = new Vector3f(1.0f).div(renderable.scale);
                        new Matrix4f(
                            renderable.scale.x * (c1 * c3 + s1 * s2 * s3),
                            renderable.scale.x * (c2 * s3),
                            renderable.scale.x * (c1 * s2 * s3 - c3 * s1),
                            0.0f,
                            renderable.scale.y * (c3 * s1 * s2 - c1 * s3),
                            renderable.scale.y * (c2 * c3),
                            renderable.scale.y * (c1 * c3 * s2 + s1 * s3),
                            0.0f,
                            renderable.scale.z * (c2 * s1),
                            renderable.scale.z * (-s2),
                            renderable.scale.z * (c1 * c2),
                            0.0f,
                            renderable.translation.x,
                            renderable.translation.y,
                            renderable.translation.z,
                            1.0f ).get(0, dest);
                        new Matrix3f(
                            invScale.x * (c1 * c3 + s1 * s2 * s3),
                            invScale.x * (c2 * s3),
                            invScale.x * (c1 * s2 * s3 - c3 * s1),

                            invScale.y * (c3 * s1 * s2 - c1 * s3),
                            invScale.y * (c2 * c3),
                            invScale.y * (c1 * c3 * s2 + s1 * s3),
                            
                            invScale.z * (c2 * s1),
                            invScale.z * (-s2),
                            invScale.z * (c1 * c2) ).get(64, dest);
                    }
                }
            );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateBuffer(int i)
    {
        Matrix4f matrix = new Matrix4f();
        viewport.getProjectionMatrix().mul(viewport.getViewMatrix(), matrix);
        matrix.get( globalUniformBuffers[i].getBuffer().rewind() );
        globalUniformBuffers[i].flush();
    }


    // ECS SYSTEM________________________________________________________________________________

    @Override
    public void onStart()
    {
        frameIndex = (frameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
        isRunning = true;
        updateBuffer(frameIndex);
    }

    @Override
    public Results<With1<Renderable>> requestData(Dominion ecs)
    {
        return ecs.findEntitiesWith(Renderable.class);
    }

    @Override
    public void execute(With1<Renderable> result, double deltaTime)
    {
        for(Mesh m : result.comp().model)
        {
            if(systems.get(m.getMaterial().MaterialID).addToQueue(new With2<Renderable, Mesh> (result.comp(), m, result.entity())))
            {
                systems.get(m.getMaterial().MaterialID).compute(cmd, uniformBufferDescriptors[frameIndex]); // TODO split cam and view matrix
            }
        }
        
    }

    @Override
    public void onEnd()
    {
        isRunning = false;
        for(SubRenderSystem sys : systems)
        {
            sys.compute(cmd, uniformBufferDescriptors[frameIndex]);
        }
    }

    // FRAME RENDERER COMPATIBILITY PART____________________________________________________________

    public void setFrameProperties(VkCommandBuffer c, Viewport v)
    {
        while(isRunning)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Wait 10ms");
                e.printStackTrace();
            }
        }
        cmd = c;
        viewport = v;
    }


    public void update(long renderPass)
    {
        this.renderPass = renderPass;
        for(SubRenderSystem system : systems)
        {
            system.recreatePipeline(renderPass);
        }
    }

    public void addMaterial(Material mat) throws Exception
    {
        if(Material.materials.values().contains(mat))
        {
            if(systems.size() < mat.MaterialID)
                throw new Exception("The Material is already added");
        }
        else
        {
            Material.materials.put(mat.tag, mat);
        }
        try(MemoryStack stack = stackPush())
        {
            if(mat.shaders.descriptorSetLayouts != null) // TODO improve my render system
            {
                DescriptorSetLayout[] newLayouts = new DescriptorSetLayout[mat.shaders.descriptorSetLayouts.length + 1];
                System.arraycopy(mat.shaders.descriptorSetLayouts, 0, newLayouts, 1, mat.shaders.descriptorSetLayouts.length);
                newLayouts[0] = globalDescriptorLayout;
                mat.shaders.descriptorSetLayouts = newLayouts;
            }
            else
            {
                mat.shaders.descriptorSetLayouts = new DescriptorSetLayout[1];
                mat.shaders.descriptorSetLayouts[0] = globalDescriptorLayout;
            }
            SubRenderSystem system = new SubRenderSystem(this, mat, mat.tag + "_RenderSystem");
            systems.add(system);
            mat.MaterialID = systems.indexOf(system);
        }
    }
}
