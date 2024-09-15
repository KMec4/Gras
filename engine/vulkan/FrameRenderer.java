package engine.vulkan;

import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import engine.VkResultDecoder;
import engine._3d.RenderSystem3d;

import static org.lwjgl.vulkan.VK10.*;

import java.util.ArrayList;

import org.lwjgl.PointerBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.vulkan.KHRSwapchain.*;

import org.lwjgl.system.MemoryStack;

public class FrameRenderer
{
    ArrayList<RenderSystem3d> systems = new ArrayList<>();

    PointerBuffer commandBuffers;
    Swapchain chain;

    Device dev;
    GameWindow window;

    int currentFrameIndex = 0;
    //int currentImageIndex = 0;
    //boolean isFrameStarted = false;

    public int MAX_FRAMES_IN_FLIGHT;

    Viewport view = new Viewport(); //TODO move Viewport to Vulkan!!!

    public FrameRenderer(Device d, GameWindow w)
    {
        dev = d;
        window = w;
        chain = new Swapchain(dev, window.getSize());
        MAX_FRAMES_IN_FLIGHT = chain.getImageCount();
        createCommandBuffers();
    }

    public void free()
    {
        freeCommandBuffers();
    }

    private void createCommandBuffers()
    {
        try(MemoryStack stack = stackPush())
        {
            commandBuffers = PointerBuffer.allocateDirect(MAX_FRAMES_IN_FLIGHT);

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType              ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO );
            allocInfo.level              ( VK_COMMAND_BUFFER_LEVEL_PRIMARY                );
            allocInfo.commandPool        ( dev.getCommandPool()                     );
            allocInfo.commandBufferCount ( commandBuffers.limit());

            int result = vkAllocateCommandBuffers(dev.getLogical(), allocInfo, commandBuffers);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to allocate command buffers: " + VkResultDecoder.decode(result));
                throw e;
            }
        }
    }

    private void freeCommandBuffers()
    {
        vkFreeCommandBuffers( dev.getLogical(), dev.getCommandPool(), commandBuffers);
        commandBuffers.clear();
    }

    private void renderGameObjects(VkCommandBuffer cmd)
    {
        for(RenderSystem3d system : systems)
        {
            system.setFrameProperties(cmd, view);
            system.run();
        }
    }

    public void attachRenderSystem(RenderSystem3d system)
    {
        systems.add(system);
    }

    public void renderFrame()
    {
        //Update view Volume
        view.setViewDirection();

        float aspect = getAspectRatio();

        //view.setOrthographicProjection(-aspect, aspect, -1f, 1f, 0f, 10.f);
        view.setPerspectiveProjection( (float) Math.toRadians(50.d), aspect, 0.1f, 100.f);


        int imageIndex = chain.acquireNextImage();

        if (imageIndex < 0)
        {
            recreateSwapChain();
            return;
        }

        try(MemoryStack stack = stackPush())
        {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO );

            VkCommandBuffer commandBuffer = new VkCommandBuffer(commandBuffers.get(imageIndex), dev.getLogical());

            int result = vkBeginCommandBuffer(commandBuffer, beginInfo);
            if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to begin recording command buffer!");
                throw e;
            }

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType       ( VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO );
            renderPassInfo.renderPass  ( chain.getRenderPass()                    );
            renderPassInfo.framebuffer ( chain.getFrameBuffer(imageIndex)         );

            VkOffset2D offset = VkOffset2D.calloc(stack).set(0, 0);
            renderPassInfo.renderArea(VkRect2D.calloc(stack).offset(offset).extent(chain.getSwapchainExtent()));

            VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
            clearValues.get(0).color       ( VkClearColorValue.calloc(stack).float32(stack.floats(0.01f, 0.01f, 0.01f, 1.0f)));
            clearValues.get(1).depthStencil( VkClearDepthStencilValue.calloc(stack).set(1.0f, 0) );

            renderPassInfo.pClearValues   ( clearValues );

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);


            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.get(0).x        ( 0.0f                          );
            viewport.get(0).y        ( 0.0f                          );
            viewport.get(0).width    ( chain.getSwapchainExtent().width()  );
            viewport.get(0).height   ( chain.getSwapchainExtent().height() );
            viewport.get(0).minDepth ( 0.0f                          );
            viewport.get(0).maxDepth ( 1.0f                          );

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.get(0).set(offset, chain.getSwapchainExtent());

            vkCmdSetViewport(commandBuffer, 0, viewport);
            vkCmdSetScissor (commandBuffer, 0,  scissor );

            try
            {
                renderGameObjects(commandBuffer); // TODO cut in here
            }
            catch (NullPointerException e)
            {
                System.out.println("An Null Pointer Exception occured in rendering");
            }
            

            vkCmdEndRenderPass(commandBuffer);

            result = vkEndCommandBuffer(commandBuffer);
            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to record command buffer: " + VkResultDecoder.decode(result));
                throw e;
            }

            result = chain.submitCommandBuffers(commandBuffer, imageIndex);

            if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR)
            {
                recreateSwapChain();
                return;
            }
            else if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to present swap chain image!");
                throw e;
            }

            currentFrameIndex = (currentFrameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
        }
    }


    // GETTERS
    public long getRenderPass()
    {
        return chain.getRenderPass();
    }

    public int size()
    {
        return commandBuffers.limit();
    }

    public float getAspectRatio()
    {
        return chain.getExtentAspectRatio();
    }

    public Viewport getViewport()
    {
        return view;
    }
    // COMPLEX STUFF
    public void recreateSwapChain()
    {
        VkExtent2D windowSize = window.getSize();
        for(; windowSize.width() == 0 || windowSize.height() == 0; windowSize = window.getSize() )
        {
            glfwWaitEvents();
        }
        vkDeviceWaitIdle( dev.getLogical() );

        chain = new Swapchain(dev, windowSize, chain);

        //TODO check doublecheck image and depth format to haven't to recreate render pass ( Chapter 11 min. 20 )
        for(RenderSystem3d system : systems)
        {
            system.update(getRenderPass());
        }
    }
}
