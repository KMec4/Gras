package engine.vulkan;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
    
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
    

public class Buffer //TODO implement auto closeable and some cleanup stuff
{
    public static class BufferProperties
    {
        public boolean isDeviceLocal;
        public boolean isHostOnly;
        public final int bufferSizeBytes;
        /**
         * only required if !isHostOnly
         */
        public final int bufferUsageFlags;
        /**
         * only required if !isHostOnly
         */
        public final int memoryPropertieFlags;
        
        /**
         * creates a buffer on the gpu
         * @param isDeviceLocal more efficient for the gpu, less for the host
         * @param bufferSizeBytes self-explaining
         * @param bufferUsageFlags is it Vetex-? Index-? or whatever Buffer. See Vulkan specs!
         * @param additionalMemoryPropertieFlags you need more flags then Device Local or Host Visible? See Vulkan specs!
         */
        public BufferProperties(boolean isDeviceLocal, int bufferSizeBytes, int bufferUsageFlags, int additionalMemoryPropertieFlags)
        {
            this.isDeviceLocal = isDeviceLocal;
            this.isHostOnly = false;
            this.bufferSizeBytes = bufferSizeBytes;
            
            if(isDeviceLocal)
            {
                this.memoryPropertieFlags = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT | additionalMemoryPropertieFlags;
                this.bufferUsageFlags = VK_BUFFER_USAGE_TRANSFER_SRC_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT | bufferUsageFlags;
            }
            else
            {
                this.memoryPropertieFlags = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT | additionalMemoryPropertieFlags; // TODO More efficency by deleting coherent bit???
                this.bufferUsageFlags = bufferUsageFlags;
            }
        }
        /**
         * creates a host only buffer
         * @param bufferSizeBytes self-explaining
         */
        public BufferProperties(int bufferSizeBytes)
        {
            isDeviceLocal = false;
            isHostOnly = true;
            this.bufferSizeBytes = bufferSizeBytes;
            bufferUsageFlags = 0;
            memoryPropertieFlags = 0;
        }
    }

    long gpuMemP = 0L;
    long bufferP = 0L;

    final ByteBuffer data;
    final Device dev;
    final BufferProperties properties;
    
    private Buffer(ByteBuffer fb, Device dev, BufferProperties props)
    {
        data = fb;
        this.dev = dev;
        properties = props;

        if(!properties.isHostOnly)
        {
            MemoryStack stack = stackGet();
            LongBuffer memoryP = stack.callocLong(1);
            LongBuffer bufferP = stack.callocLong(1);
            dev.createBuffer(
                fb.limit(),
                props.bufferUsageFlags,
                props.memoryPropertieFlags,
                bufferP,
                memoryP
                );
            this.bufferP = bufferP.get(0);
            this.gpuMemP = memoryP.get(0);
        }
    }
    
    public static Buffer callocBuffer(MemoryStack stack, Device dev, BufferProperties properties)
    {
        return new Buffer(stack.calloc(properties.bufferSizeBytes), dev, properties);
    }
    public static Buffer callocBuffer(Device dev, BufferProperties properties)
    {
        return new Buffer(MemoryUtil.memAlloc(properties.bufferSizeBytes), dev, properties);
    }
    
/*        public static Buffer callocBufferInstance(MemoryStack stack, int size, long gpuMemP, long bufferP, boolean isDeviceLocal)
        {
            Buffer vb = new Buffer(stack.calloc(size));
            vb.isLocalOnly = isDeviceLocal;
            vb.deviceLocal = false;
            vb.gpuMemP = gpuMemP;
            vb.bufferP = bufferP;
            vb.dev = Vulkan.getRenderDevice();
            vb.load();
            return vb;
        }*/
    
    public ByteBuffer getBuffer()
    {
        return data;
    }
    
    public int getSizeBytes()
    {
        return data.limit();
    }

    /**
    * Maps the Buffer to the GPU and copys all data. Then unmap.
    */
    public void flush()
    {
        flush(0, properties.bufferSizeBytes);
    }
    
    /**
     * Maps the Buffer to the GPU and copys data. Then unmap.
     */
    public void flush(int offset, long size)
    {
        if(!properties.isHostOnly)
        {
            MemoryStack stack = stackGet();
            if(properties.isDeviceLocal)
            {
                LongBuffer memoryP = stack.callocLong(1);
                LongBuffer bufferP = stack.callocLong(1);
    
                dev.createBuffer( // create staging buffer
                    data.limit(),
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    bufferP,
                    memoryP
                    );
    
                PointerBuffer dataP = stack.mallocPointer(1); // copy the data to a vixible gpu buffer
                vkMapMemory(dev.getLogical(), memoryP.get(0), offset, size, 0, dataP);
                MemoryUtil.memCopy( MemoryUtil.memAddress(data), dataP.get(0), size);
                vkUnmapMemory(dev.getLogical(), memoryP.get(0));

                dev.copyBuffer(bufferP.get(0), this.bufferP, offset, data.limit());
                dev.freeBuffer(bufferP.get(0), memoryP.get(0));
            }
            else
            {
                PointerBuffer dataP = stack.mallocPointer(1);
                vkMapMemory(dev.getLogical(), gpuMemP, offset, size, 0, dataP);
                MemoryUtil.memCopy( MemoryUtil.memAddress(data), dataP.get(0), size);
                vkUnmapMemory(dev.getLogical(), gpuMemP);
            }
        }
    }
    
    /**
     * loads all data from the gpu. Then unmap.
     */
    public void load()
    {
        if(!properties.isHostOnly)
        {
            MemoryStack stack = stackGet();
            if(properties.isDeviceLocal)
            {
                LongBuffer memoryP = stack.callocLong(1);
                LongBuffer bufferP = stack.callocLong(1);

                dev.createBuffer( // create staging buffer
                    data.limit(),
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    bufferP,
                    memoryP
                    );

                dev.copyBuffer(this.bufferP, bufferP.get(0), data.limit());

                PointerBuffer dataP = stack.mallocPointer(1); // copy the data to a vixible gpu buffer
                vkMapMemory(dev.getLogical(), memoryP.get(0), 0, getSizeBytes(), 0, dataP);
                MemoryUtil.memCopy( dataP.get(0), MemoryUtil.memAddress(data), getSizeBytes());
                vkUnmapMemory(dev.getLogical(), memoryP.get(0));

                dev.freeBuffer(bufferP.get(0), memoryP.get(0));
            }
            else
            {
                PointerBuffer dataP = stack.mallocPointer(1);
                vkMapMemory(dev.getLogical(), gpuMemP, 0, getSizeBytes(), 0, dataP);
                MemoryUtil.memCopy( dataP.get(0), MemoryUtil.memAddress(data), getSizeBytes());
                vkUnmapMemory(dev.getLogical(), gpuMemP);
            }
        }
    }

    public void free() // TODO implement
    {

    }

    public long getMemoryPointer()
    {
        return gpuMemP;
    }

    public long getBufferPointer()
    {
        return bufferP;
    }
}