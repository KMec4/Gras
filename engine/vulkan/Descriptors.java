package engine.vulkan;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import engine.VkResultDecoder;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FRAGMENTED_POOL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkResetDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import static org.lwjgl.vulkan.VK11.*;
import static org.lwjgl.vulkan.QCOMImageProcessing.VK_DESCRIPTOR_TYPE_BLOCK_MATCH_IMAGE_QCOM;
import static org.lwjgl.vulkan.QCOMImageProcessing.VK_DESCRIPTOR_TYPE_SAMPLE_WEIGHT_IMAGE_QCOM;;

public class Descriptors
{
    //TODO add Caching for layouts
    public static class DescriptorSetLayout
    {
        private int binding = 0;
        public ArrayList<VkDescriptorSetLayoutBinding> bindings = new ArrayList<>();
        private Device dev;
        long layout;

        /**
         * The DescriptorSetLayoutCreator is used to create the VkDescriptorSetLayout for a Pipeline.
         */
        public DescriptorSetLayout(Device dev)
        {
            this.dev = dev;
        }

        public DescriptorSetLayout nextBinding( int descriptorType, int stageFlags, int count )
        {
            VkDescriptorSetLayoutBinding descriptorBinding = VkDescriptorSetLayoutBinding.calloc();
            descriptorBinding.binding       (binding++);
            descriptorBinding.descriptorType(descriptorType);
            descriptorBinding.descriptorCount(count);
            descriptorBinding.stageFlags(stageFlags);
            bindings.add(descriptorBinding);
            return this;
        }

        public DescriptorSetLayout nextBinding( int descriptorType, int stageFlags )
        {
            return nextBinding(descriptorType, stageFlags, 1);
        }

        public long getDescriptorSetLayout()
        {
            if(layout == 0L)
            {
                build();
            }
            return layout;
        }

        public void build()
        {
            try(MemoryStack stack = stackPush())
            {
                if(layout != 0L)
                {
                    return;
                }
                VkDescriptorSetLayoutBinding.Buffer buffer = VkDescriptorSetLayoutBinding.calloc(bindings.size(), stack);
                for(VkDescriptorSetLayoutBinding binding : bindings)
                {
                    buffer.put(binding);
                }
                buffer.rewind();

                VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
                createInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
                createInfo.pBindings(buffer);

                LongBuffer ptr = stack.callocLong(1);
                int result = vkCreateDescriptorSetLayout(dev.getLogical(), createInfo, null, ptr);
                if(result != VK_SUCCESS)
                {
                    throw new RuntimeException("Can not create DescrtiptorSetLayout: " + VkResultDecoder.decode(result));
                }
                layout = ptr.get(0);
            }
        }

        public void free()
        {
            vkDestroyDescriptorSetLayout(dev.getLogical(), layout, null);
        }
    }

    public static class DescriptorAllocator
    {
        public static final int DEFALUT_POOL_SIZE = 1024;

        public final HashMap<Integer, Float> POOL_SIZES = new HashMap<Integer, Float>(11);
        private int[] descriptorTypes = new int[11];
        {
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_SAMPLER               , 0.5f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 4.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE         , 4.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_STORAGE_IMAGE         , 1.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER  , 1.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER  , 1.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER        , 2.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_STORAGE_BUFFER        , 2.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 1.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC, 1.0f );
            POOL_SIZES.put( VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT      , 0.5f );

            int i = 0;
            for(Integer value : POOL_SIZES.keySet())
            {
                descriptorTypes[i++] = value; 
            }
        }

        Device dev;
        int usedPoolsCount = 0;
        long[] usedPools = new long[8];
        int freePoolsCount = 0;
        long[] freePools = new long[8];

        public DescriptorAllocator(Device dev) // TODO boolean autoOptimise
        {
            this.dev = dev;
        }

        public synchronized long allocateDescriptorSet(long descriptorSetLayout) // may be threadsafe
        {
            try(MemoryStack stack = stackPush())
            {
                long currentPool;
                if(usedPoolsCount < 1)
                {
                    currentPool = grapPool();
                }
                else
                {
                    currentPool = usedPools[usedPoolsCount-1];
                }

                VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
                allocateInfo.sType$Default();
                allocateInfo.pSetLayouts(stack.longs(descriptorSetLayout));
                allocateInfo.descriptorPool(currentPool);

                LongBuffer ptr = stack.callocLong(1);
                int result = vkAllocateDescriptorSets(dev.getLogical(), allocateInfo, ptr);

                switch (result)
                {
                    case VK_SUCCESS:
                        return ptr.get(0);
                    
                    case VK_ERROR_FRAGMENTED_POOL:
                    case VK_ERROR_OUT_OF_POOL_MEMORY:
                        grapPool();
                        result = vkAllocateDescriptorSets(dev.getLogical(), allocateInfo, ptr);
                        if(result == VK_SUCCESS)
                        {
                            return ptr.get(0);
                        }
                }

                throw new RuntimeException("Can not allocate descriptor set: " + VkResultDecoder.decode(result));
            }
        }

        private long grapPool()
        {
            if(freePoolsCount > 0)
            {
                freePoolsCount--;
                return addToUsedPools(freePools[freePoolsCount]);
            }
            else
            {
                return addToUsedPools(createPool(stackPush(), DEFALUT_POOL_SIZE, 0));
            }
        }

        private long addToUsedPools(long l)
        {
            if(usedPoolsCount >= usedPools.length)
            {
                long[] newUsedPools = new long[usedPools.length + 16];
                System.arraycopy(usedPools, 0, newUsedPools, 0, usedPools.length);
                usedPools = newUsedPools;
            }
            usedPools[usedPoolsCount++] = l;
            return l;
        }

        private long createPool(MemoryStack stack, int size, int descriptorPoolFlags)
        {
            VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(11, stack);

            for(int i = 0; i < 11; i++)
            {
                sizes.get(i).set(descriptorTypes[i], (int) (POOL_SIZES.get(descriptorTypes[i]) * size));
            }

            VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            createInfo.flags(descriptorPoolFlags);
            createInfo.maxSets(size);
            createInfo.pPoolSizes(sizes);

            LongBuffer ptr = stack.callocLong(1);
            int result = vkCreateDescriptorPool(dev.getLogical(), createInfo, null, ptr);
            if(result != VK_SUCCESS)
            {
                throw new RuntimeException("Can not create descriptor pool: " + VkResultDecoder.decode(result));
            }
            return ptr.get(0);
        }

        public void free()
        {
            for(int i = 0; i < usedPoolsCount; i++)
            {
                vkDestroyDescriptorPool(dev.getLogical(), usedPools[i], null);
            }
            for(int i = 0; i < freePoolsCount; i++)
            {
                vkDestroyDescriptorPool(dev.getLogical(), freePools[i], null);
            }
            usedPoolsCount = 0;
            freePoolsCount = 0;
        }

        public void reset()
        {
            int size = usedPoolsCount + freePoolsCount;
            if(freePools.length != freePoolsCount)
            {
                long[] newFreePools = new long[size];
                System.arraycopy(freePools, 0, newFreePools, 0, freePoolsCount);
                freePools = newFreePools;
            }
            for(int i = 0; i < usedPoolsCount; i++)
            {
                vkResetDescriptorPool(dev.getLogical(), usedPools[i], 0);
                freePools[freePoolsCount++] = usedPools[i];
            }
            usedPoolsCount = 0;
        }
    }

    public static class Utils
    {
        public static long createDescriptor(Buffer[] buffers, int[] bindings, DescriptorSetLayout layout, DescriptorAllocator pool)
        {
            long descriptor = pool.allocateDescriptorSet(layout.getDescriptorSetLayout());
            return updateDescriptor(descriptor, buffers, bindings, layout);
        }

        public static long updateDescriptor(long descriptorSet, Buffer[] buffers, int[] bindings, DescriptorSetLayout layout)
        {
            int lastIndex = Math.min(buffers.length, bindings.length);

            for(int i = 0; i < lastIndex; i++)
            {
                int descriptorType = layout.bindings.get(bindings[i]).descriptorType();

                VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(1, stackGet());
                writes.get(0)
                .sType$Default()
                .descriptorCount(1)
                .dstSet(descriptorSet)
                .descriptorType(descriptorType)
                .dstBinding(bindings[i]);

                if( descriptorType == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER || descriptorType == VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE || descriptorType == VK_DESCRIPTOR_TYPE_STORAGE_IMAGE || descriptorType == VK_DESCRIPTOR_TYPE_SAMPLE_WEIGHT_IMAGE_QCOM || descriptorType == VK_DESCRIPTOR_TYPE_BLOCK_MATCH_IMAGE_QCOM )
                {
                    /*if(buffers[i] instanceof ImageBuffer)
                    {
                        ImageBuffer b = (ImageBuffer) buffers[i];
                        writes.pBufferInfo
                        (
                            VkDescriptorImageInfo.calloc(1, stackGet())
                        );
                    }
                    else*/ // TODO add imageBuffers
                    {
                        throw new RuntimeException("The descriptor type at binding " + bindings[i] + "is defined as IMAGE, but the buffer is no insrance of ImageBuffer!");
                    }
                }
                else
                {
                    writes.pBufferInfo
                    (
                        VkDescriptorBufferInfo.calloc(1, stackGet())
                        .offset(0)
                        .buffer(buffers[i].getBufferPointer())
                        .range(VK_WHOLE_SIZE)
                    );
                }

                vkUpdateDescriptorSets(layout.dev.getLogical(), writes, null);
            }
            return descriptorSet;
        }
    }
}