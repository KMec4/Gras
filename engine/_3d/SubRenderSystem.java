package engine._3d;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import dev.dominion.ecs.api.Results.With1;
import dev.dominion.ecs.api.Results.With2;
import engine.components.Renderable;
import engine.vulkan.Device;
import engine.vulkan.Pipeline;

public class SubRenderSystem
{
    public final int QUEUE_SIZE = 128;
    int currentIndex = 0;
    @SuppressWarnings("unchecked")
    With2<Renderable, Mesh>[] renderQueue = new With2[128];

    public Material material;
    public Pipeline pipe;
    RenderSystem3d masterSystem;
    String tag;


    public SubRenderSystem(RenderSystem3d master, Material material, String tag)
    {
        this.material = material;
        this.tag = tag;
        masterSystem = master;
        recreatePipeline(master.renderPass);
    }

    /**
     * adds an element to the queue to be rendered later
     * @param obj
     * @return true when queue is full
     */
    public boolean addToQueue(With2<Renderable, Mesh> obj)
    {
        renderQueue[currentIndex++] = obj;
        return currentIndex >= QUEUE_SIZE;
    }

    public void compute(VkCommandBuffer cmd, long globalDescriptorSet)
    {
        try(MemoryStack stack = stackPush())
        {
            vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, pipe.getPipeline());
            vkCmdBindDescriptorSets(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, pipe.getPipelineLayout(), 0, stack.longs(globalDescriptorSet), null);
            material.bindDescriptorSets();

            ByteBuffer pushData = stack.calloc(material.shaders.pushInfo.SIZE);

            for(With2<Renderable, Mesh> obj : renderQueue)
            {
                if(obj != null)
                {
                    pushData.clear();
                    material.getPushConstantData(pushData, new With1<Renderable>(obj.comp1(), obj.entity())); // Der Buffer sollte wiederverwendet werden können, da die Methode immer die selben Felder belegt?
                    vkCmdPushConstants(cmd, pipe.getPipelineLayout(), material.shaders.pushInfo.usedInShaderStagesFlag, 0, pushData);

                    obj.comp2().submitToCommandBuffer(cmd);
                }
            }
            currentIndex = 0;
        }
    }

    public void recreatePipeline(long renderPass)
    {
        {
            Device dev = masterSystem.dev;
            try(MemoryStack stack = stackPush())
            {
                this.pipe = new Pipeline(dev.getLogical(), renderPass, material.shaders);
            }
        }  
    }

}
