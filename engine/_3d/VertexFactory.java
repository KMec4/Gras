package engine._3d;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import engine.Vulkan;

import static org.lwjgl.system.MemoryStack.stackGet;

import static org.lwjgl.vulkan.VK10.*;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class VertexFactory
{
    public static class VertexBlueprint
    {
        public final int[] attributeDescriptionFormats;
        public final int[] offsets;
        public final int size;

        public VertexBlueprint(int[] attributeDescriptionFormats, int[] floatsPerElement)
        {
            Logger log = System.getLogger("");
            int maxItems = Math.min(attributeDescriptionFormats.length, floatsPerElement.length);
            this.attributeDescriptionFormats = new int[maxItems];
            offsets = new int[maxItems];
            int currentOffset = 0;
            String msg = "\nCreating new Vertex Blueprint\n-----------------------------";
            for(int i = 0; i < maxItems; i++)
            {
                msg += "\n" + i + " @offset" + currentOffset + " : " + vkFormatToString(attributeDescriptionFormats[i]);
                if((attributeDescriptionFormats[i] & VK_FORMAT_FEATURE_VERTEX_BUFFER_BIT) != VK_FORMAT_FEATURE_VERTEX_BUFFER_BIT)
                {
                    msg += " !!! DOES NOT SUPPORT VK_FORMAT_FEATURE_VERTEX_BUFFER_BIT !!!";
                }
                offsets[i] = currentOffset;
                this.attributeDescriptionFormats[i] = attributeDescriptionFormats[i];
                currentOffset += floatsPerElement[i] * 4;
            }
            size = currentOffset;
            log.log(Level.INFO, msg + "\n SIZE: "+size+"\n-----------------------------\n");
        }

        public VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack stack)
        {
            VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.calloc(1, stack);
            bindingDescriptions.get(0).binding   (0                      );
            bindingDescriptions.get(0).stride    ( size                        );
            bindingDescriptions.get(0).inputRate ( VK_VERTEX_INPUT_RATE_VERTEX );
    
            return bindingDescriptions;
        }

        public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack)
        {
            VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(offsets.length, stack);
            for(int i = 0; i < offsets.length; i++)
            {
                attributeDescriptions.get(i).binding  (0);
                attributeDescriptions.get(i).location (i);
                attributeDescriptions.get(i).format   (attributeDescriptionFormats[i]);
                attributeDescriptions.get(i).offset   (offsets[i]);
            }
            return attributeDescriptions;
        }
    }

    public static class PushConstantInfo
    {
        final int SIZE;
        final int usedInShaderStagesFlag;

        public PushConstantInfo(int size, @NativeType(value="VkShaderStageFlags") int stageFlags)
        {
            int maxPushConstantSize = Vulkan.getRenderDevice().getLimits(stackGet()).maxPushConstantsSize();

            String msg = "\nCreating new PushConstantInfo\n-----------------------------\n";
            msg += "Shader Stages:\n" + vkStageFlagToString(stageFlags);
            msg += "SIZE: " + size + " (max:" + maxPushConstantSize + ")";
            if(size > maxPushConstantSize)
            {
                msg += " !!! The Push Constant is to big !!!";
            }
            SIZE = size;
            usedInShaderStagesFlag = stageFlags;
            System.getLogger("").log(Level.INFO, msg + "\n-----------------------------\n");
        }

        public VkPushConstantRange.Buffer getPushConstantRange(MemoryStack stack)
        {
            VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc(1, stack);
            pushConstantRange.get(0).stageFlags ( usedInShaderStagesFlag );
            pushConstantRange.get(0).offset     ( 0                );
            pushConstantRange.get(0).size       ( SIZE );
            return pushConstantRange;
        }
    }

    private static String vkFormatToString(int format)
    {
        switch (format)
        {
        case VK_FORMAT_UNDEFINED                  :
            return "VK_FORMAT_UNDEFINED                 ";
        case VK_FORMAT_R4G4_UNORM_PACK8           :
            return "VK_FORMAT_R4G4_UNORM_PACK8          ";
        case VK_FORMAT_R4G4B4A4_UNORM_PACK16      :
            return "VK_FORMAT_R4G4B4A4_UNORM_PACK16     ";
        case VK_FORMAT_B4G4R4A4_UNORM_PACK16      :
            return "VK_FORMAT_B4G4R4A4_UNORM_PACK16     ";
        case VK_FORMAT_R5G6B5_UNORM_PACK16        :
            return "VK_FORMAT_R5G6B5_UNORM_PACK16       ";
        case VK_FORMAT_B5G6R5_UNORM_PACK16        :
            return "VK_FORMAT_B5G6R5_UNORM_PACK16       ";
        case VK_FORMAT_R5G5B5A1_UNORM_PACK16      :
            return "VK_FORMAT_R5G5B5A1_UNORM_PACK16     ";
        case VK_FORMAT_B5G5R5A1_UNORM_PACK16      :
            return "VK_FORMAT_B5G5R5A1_UNORM_PACK16     ";
        case VK_FORMAT_A1R5G5B5_UNORM_PACK16      :
            return "VK_FORMAT_A1R5G5B5_UNORM_PACK16     ";
        case VK_FORMAT_R8_UNORM                   :
            return "VK_FORMAT_R8_UNORM                  ";
        case VK_FORMAT_R8_SNORM                   :
            return "VK_FORMAT_R8_SNORM                  ";
        case VK_FORMAT_R8_USCALED                 :
            return "VK_FORMAT_R8_USCALED                ";
        case VK_FORMAT_R8_SSCALED                 :
            return "VK_FORMAT_R8_SSCALED                ";
        case VK_FORMAT_R8_UINT                    :
            return "VK_FORMAT_R8_UINT                   ";
        case VK_FORMAT_R8_SINT                    :
            return "VK_FORMAT_R8_SINT                   ";
        case VK_FORMAT_R8_SRGB                    :
            return "VK_FORMAT_R8_SRGB                   ";
        case VK_FORMAT_R8G8_UNORM                 :
            return "VK_FORMAT_R8G8_UNORM                ";
        case VK_FORMAT_R8G8_SNORM                 :
            return "VK_FORMAT_R8G8_SNORM                ";
        case VK_FORMAT_R8G8_USCALED               :
            return "VK_FORMAT_R8G8_USCALED              ";
        case VK_FORMAT_R8G8_SSCALED               :
            return "VK_FORMAT_R8G8_SSCALED              ";
        case VK_FORMAT_R8G8_UINT                  :
            return "VK_FORMAT_R8G8_UINT                 ";
        case VK_FORMAT_R8G8_SINT                  :
            return "VK_FORMAT_R8G8_SINT                 ";
        case VK_FORMAT_R8G8_SRGB                  :
            return "VK_FORMAT_R8G8_SRGB                 ";
        case VK_FORMAT_R8G8B8_UNORM               :
            return "VK_FORMAT_R8G8B8_UNORM              ";
        case VK_FORMAT_R8G8B8_SNORM               :
            return "VK_FORMAT_R8G8B8_SNORM              ";
        case VK_FORMAT_R8G8B8_USCALED             :
            return "VK_FORMAT_R8G8B8_USCALED            ";
        case VK_FORMAT_R8G8B8_SSCALED             :
            return "VK_FORMAT_R8G8B8_SSCALED            ";
        case VK_FORMAT_R8G8B8_UINT                :
            return "VK_FORMAT_R8G8B8_UINT               ";
        case VK_FORMAT_R8G8B8_SINT                :
            return "VK_FORMAT_R8G8B8_SINT               ";
        case VK_FORMAT_R8G8B8_SRGB                :
            return "VK_FORMAT_R8G8B8_SRGB               ";
        case VK_FORMAT_B8G8R8_UNORM               :
            return "VK_FORMAT_B8G8R8_UNORM              ";
        case VK_FORMAT_B8G8R8_SNORM               :
            return "VK_FORMAT_B8G8R8_SNORM              ";
        case VK_FORMAT_B8G8R8_USCALED             :
            return "VK_FORMAT_B8G8R8_USCALED            ";
        case VK_FORMAT_B8G8R8_SSCALED             :
            return "VK_FORMAT_B8G8R8_SSCALED            ";
        case VK_FORMAT_B8G8R8_UINT                :
            return "VK_FORMAT_B8G8R8_UINT               ";
        case VK_FORMAT_B8G8R8_SINT                :
            return "VK_FORMAT_B8G8R8_SINT               ";
        case VK_FORMAT_B8G8R8_SRGB                :
            return "VK_FORMAT_B8G8R8_SRGB               ";
        case VK_FORMAT_R8G8B8A8_UNORM             :
            return "VK_FORMAT_R8G8B8A8_UNORM            ";
        case VK_FORMAT_R8G8B8A8_SNORM             :
            return "VK_FORMAT_R8G8B8A8_SNORM            ";
        case VK_FORMAT_R8G8B8A8_USCALED           :
            return "VK_FORMAT_R8G8B8A8_USCALED          ";
        case VK_FORMAT_R8G8B8A8_SSCALED           :
            return "VK_FORMAT_R8G8B8A8_SSCALED          ";
        case VK_FORMAT_R8G8B8A8_UINT              :
            return "VK_FORMAT_R8G8B8A8_UINT             ";
        case VK_FORMAT_R8G8B8A8_SINT              :
            return "VK_FORMAT_R8G8B8A8_SINT             ";
        case VK_FORMAT_R8G8B8A8_SRGB              :
            return "VK_FORMAT_R8G8B8A8_SRGB             ";
        case VK_FORMAT_B8G8R8A8_UNORM             :
            return "VK_FORMAT_B8G8R8A8_UNORM            ";
        case VK_FORMAT_B8G8R8A8_SNORM             :
            return "VK_FORMAT_B8G8R8A8_SNORM            ";
        case VK_FORMAT_B8G8R8A8_USCALED           :
            return "VK_FORMAT_B8G8R8A8_USCALED          ";
        case VK_FORMAT_B8G8R8A8_SSCALED           :
            return "VK_FORMAT_B8G8R8A8_SSCALED          ";
        case VK_FORMAT_B8G8R8A8_UINT              :
            return "VK_FORMAT_B8G8R8A8_UINT             ";
        case VK_FORMAT_B8G8R8A8_SINT              :
            return "VK_FORMAT_B8G8R8A8_SINT             ";
        case VK_FORMAT_B8G8R8A8_SRGB              :
            return "VK_FORMAT_B8G8R8A8_SRGB             ";
        case VK_FORMAT_A8B8G8R8_UNORM_PACK32      :
            return "VK_FORMAT_A8B8G8R8_UNORM_PACK32     ";
        case VK_FORMAT_A8B8G8R8_SNORM_PACK32      :
            return "VK_FORMAT_A8B8G8R8_SNORM_PACK32     ";
        case VK_FORMAT_A8B8G8R8_USCALED_PACK32    :
            return "VK_FORMAT_A8B8G8R8_USCALED_PACK32   ";
        case VK_FORMAT_A8B8G8R8_SSCALED_PACK32    :
            return "VK_FORMAT_A8B8G8R8_SSCALED_PACK32   ";
        case VK_FORMAT_A8B8G8R8_UINT_PACK32       :
            return "VK_FORMAT_A8B8G8R8_UINT_PACK32      ";
        case VK_FORMAT_A8B8G8R8_SINT_PACK32       :
            return "VK_FORMAT_A8B8G8R8_SINT_PACK32      ";
        case VK_FORMAT_A8B8G8R8_SRGB_PACK32       :
            return "VK_FORMAT_A8B8G8R8_SRGB_PACK32      ";
        case VK_FORMAT_A2R10G10B10_UNORM_PACK32   :
            return "VK_FORMAT_A2R10G10B10_UNORM_PACK32  ";
        case VK_FORMAT_A2R10G10B10_SNORM_PACK32   :
            return "VK_FORMAT_A2R10G10B10_SNORM_PACK32  ";
        case VK_FORMAT_A2R10G10B10_USCALED_PACK32 :
            return "VK_FORMAT_A2R10G10B10_USCALED_PACK32";
        case VK_FORMAT_A2R10G10B10_SSCALED_PACK32 :
            return "VK_FORMAT_A2R10G10B10_SSCALED_PACK32";
        case VK_FORMAT_A2R10G10B10_UINT_PACK32    :
            return "VK_FORMAT_A2R10G10B10_UINT_PACK32   ";
        case VK_FORMAT_A2R10G10B10_SINT_PACK32    :
            return "VK_FORMAT_A2R10G10B10_SINT_PACK32   ";
        case VK_FORMAT_A2B10G10R10_UNORM_PACK32   :
            return "VK_FORMAT_A2B10G10R10_UNORM_PACK32  ";
        case VK_FORMAT_A2B10G10R10_SNORM_PACK32   :
            return "VK_FORMAT_A2B10G10R10_SNORM_PACK32  ";
        case VK_FORMAT_A2B10G10R10_USCALED_PACK32 :
            return "VK_FORMAT_A2B10G10R10_USCALED_PACK32";
        case VK_FORMAT_A2B10G10R10_SSCALED_PACK32 :
            return "VK_FORMAT_A2B10G10R10_SSCALED_PACK32";
        case VK_FORMAT_A2B10G10R10_UINT_PACK32    :
            return "VK_FORMAT_A2B10G10R10_UINT_PACK32   ";
        case VK_FORMAT_A2B10G10R10_SINT_PACK32    :
            return "VK_FORMAT_A2B10G10R10_SINT_PACK32   ";
        case VK_FORMAT_R16_UNORM                  :
            return "VK_FORMAT_R16_UNORM                 ";
        case VK_FORMAT_R16_SNORM                  :
            return "VK_FORMAT_R16_SNORM                 ";
        case VK_FORMAT_R16_USCALED                :
            return "VK_FORMAT_R16_USCALED               ";
        case VK_FORMAT_R16_SSCALED                :
            return "VK_FORMAT_R16_SSCALED               ";
        case VK_FORMAT_R16_UINT                   :
            return "VK_FORMAT_R16_UINT                  ";
        case VK_FORMAT_R16_SINT                   :
            return "VK_FORMAT_R16_SINT                  ";
        case VK_FORMAT_R16_SFLOAT                 :
            return "VK_FORMAT_R16_SFLOAT                ";
        case VK_FORMAT_R16G16_UNORM               :
            return "VK_FORMAT_R16G16_UNORM              ";
        case VK_FORMAT_R16G16_SNORM               :
            return "VK_FORMAT_R16G16_SNORM              ";
        case VK_FORMAT_R16G16_USCALED             :
            return "VK_FORMAT_R16G16_USCALED            ";
        case VK_FORMAT_R16G16_SSCALED             :
            return "VK_FORMAT_R16G16_SSCALED            ";
        case VK_FORMAT_R16G16_UINT                :
            return "VK_FORMAT_R16G16_UINT               ";
        case VK_FORMAT_R16G16_SINT                :
            return "VK_FORMAT_R16G16_SINT               ";
        case VK_FORMAT_R16G16_SFLOAT              :
            return "VK_FORMAT_R16G16_SFLOAT             ";
        case VK_FORMAT_R16G16B16_UNORM            :
            return "VK_FORMAT_R16G16B16_UNORM           ";
        case VK_FORMAT_R16G16B16_SNORM            :
            return "VK_FORMAT_R16G16B16_SNORM           ";
        case VK_FORMAT_R16G16B16_USCALED          :
            return "VK_FORMAT_R16G16B16_USCALED         ";
        case VK_FORMAT_R16G16B16_SSCALED          :
            return "VK_FORMAT_R16G16B16_SSCALED         ";
        case VK_FORMAT_R16G16B16_UINT             :
            return "VK_FORMAT_R16G16B16_UINT            ";
        case VK_FORMAT_R16G16B16_SINT             :
            return "VK_FORMAT_R16G16B16_SINT            ";
        case VK_FORMAT_R16G16B16_SFLOAT           :
            return "VK_FORMAT_R16G16B16_SFLOAT          ";
        case VK_FORMAT_R16G16B16A16_UNORM         :
            return "VK_FORMAT_R16G16B16A16_UNORM        ";
        case VK_FORMAT_R16G16B16A16_SNORM         :
            return "VK_FORMAT_R16G16B16A16_SNORM        ";
        case VK_FORMAT_R16G16B16A16_USCALED       :
            return "VK_FORMAT_R16G16B16A16_USCALED      ";
        case VK_FORMAT_R16G16B16A16_SSCALED       :
            return "VK_FORMAT_R16G16B16A16_SSCALED      ";
        case VK_FORMAT_R16G16B16A16_UINT          :
            return "VK_FORMAT_R16G16B16A16_UINT         ";
        case VK_FORMAT_R16G16B16A16_SINT          :
            return "VK_FORMAT_R16G16B16A16_SINT         ";
        case VK_FORMAT_R16G16B16A16_SFLOAT        :
            return "VK_FORMAT_R16G16B16A16_SFLOAT       ";
        case VK_FORMAT_R32_UINT                   :
            return "VK_FORMAT_R32_UINT                  ";
        case VK_FORMAT_R32_SINT                   :
            return "VK_FORMAT_R32_SINT                  ";
        case VK_FORMAT_R32_SFLOAT                 :
            return "VK_FORMAT_R32_SFLOAT                ";
        case VK_FORMAT_R32G32_UINT                :
            return "VK_FORMAT_R32G32_UINT               ";
        case VK_FORMAT_R32G32_SINT                :
            return "VK_FORMAT_R32G32_SINT               ";
        case VK_FORMAT_R32G32_SFLOAT              :
            return "VK_FORMAT_R32G32_SFLOAT             ";
        case VK_FORMAT_R32G32B32_UINT             :
            return "VK_FORMAT_R32G32B32_UINT            ";
        case VK_FORMAT_R32G32B32_SINT             :
            return "VK_FORMAT_R32G32B32_SINT            ";
        case VK_FORMAT_R32G32B32_SFLOAT           :
            return "VK_FORMAT_R32G32B32_SFLOAT          ";
        case VK_FORMAT_R32G32B32A32_UINT          :
            return "VK_FORMAT_R32G32B32A32_UINT         ";
        case VK_FORMAT_R32G32B32A32_SINT          :
            return "VK_FORMAT_R32G32B32A32_SINT         ";
        case VK_FORMAT_R32G32B32A32_SFLOAT        :
            return "VK_FORMAT_R32G32B32A32_SFLOAT       ";
        case VK_FORMAT_R64_UINT                   :
            return "VK_FORMAT_R64_UINT                  ";
        case VK_FORMAT_R64_SINT                   :
            return "VK_FORMAT_R64_SINT                  ";
        case VK_FORMAT_R64_SFLOAT                 :
            return "VK_FORMAT_R64_SFLOAT                ";
        case VK_FORMAT_R64G64_UINT                :
            return "VK_FORMAT_R64G64_UINT               ";
        case VK_FORMAT_R64G64_SINT                :
            return "VK_FORMAT_R64G64_SINT               ";
        case VK_FORMAT_R64G64_SFLOAT              :
            return "VK_FORMAT_R64G64_SFLOAT             ";
        case VK_FORMAT_R64G64B64_UINT             :
            return "VK_FORMAT_R64G64B64_UINT            ";
        case VK_FORMAT_R64G64B64_SINT             :
            return "VK_FORMAT_R64G64B64_SINT            ";
        case VK_FORMAT_R64G64B64_SFLOAT           :
            return "VK_FORMAT_R64G64B64_SFLOAT          ";
        case VK_FORMAT_R64G64B64A64_UINT          :
            return "VK_FORMAT_R64G64B64A64_UINT         ";
        case VK_FORMAT_R64G64B64A64_SINT          :
            return "VK_FORMAT_R64G64B64A64_SINT         ";
        case VK_FORMAT_R64G64B64A64_SFLOAT        :
            return "VK_FORMAT_R64G64B64A64_SFLOAT       ";
        case VK_FORMAT_B10G11R11_UFLOAT_PACK32    :
            return "VK_FORMAT_B10G11R11_UFLOAT_PACK32   ";
        case VK_FORMAT_E5B9G9R9_UFLOAT_PACK32     :
            return "VK_FORMAT_E5B9G9R9_UFLOAT_PACK32    ";
        case VK_FORMAT_D16_UNORM                  :
            return "VK_FORMAT_D16_UNORM                 ";
        case VK_FORMAT_X8_D24_UNORM_PACK32        :
            return "VK_FORMAT_X8_D24_UNORM_PACK32       ";
        case VK_FORMAT_D32_SFLOAT                 :
            return "VK_FORMAT_D32_SFLOAT                ";
        case VK_FORMAT_S8_UINT                    :
            return "VK_FORMAT_S8_UINT                   ";
        case VK_FORMAT_D16_UNORM_S8_UINT          :
            return "VK_FORMAT_D16_UNORM_S8_UINT         ";
        case VK_FORMAT_D24_UNORM_S8_UINT          :
            return "VK_FORMAT_D24_UNORM_S8_UINT         ";
        case VK_FORMAT_D32_SFLOAT_S8_UINT         :
            return "VK_FORMAT_D32_SFLOAT_S8_UINT        ";
        case VK_FORMAT_BC1_RGB_UNORM_BLOCK        :
            return "VK_FORMAT_BC1_RGB_UNORM_BLOCK       ";
        case VK_FORMAT_BC1_RGB_SRGB_BLOCK         :
            return "VK_FORMAT_BC1_RGB_SRGB_BLOCK        ";
        case VK_FORMAT_BC1_RGBA_UNORM_BLOCK       :
            return "VK_FORMAT_BC1_RGBA_UNORM_BLOCK      ";
        case VK_FORMAT_BC1_RGBA_SRGB_BLOCK        :
            return "VK_FORMAT_BC1_RGBA_SRGB_BLOCK       ";
        case VK_FORMAT_BC2_UNORM_BLOCK            :
            return "VK_FORMAT_BC2_UNORM_BLOCK           ";
        case VK_FORMAT_BC2_SRGB_BLOCK             :
            return "VK_FORMAT_BC2_SRGB_BLOCK            ";
        case VK_FORMAT_BC3_UNORM_BLOCK            :
            return "VK_FORMAT_BC3_UNORM_BLOCK           ";
        case VK_FORMAT_BC3_SRGB_BLOCK             :
            return "VK_FORMAT_BC3_SRGB_BLOCK            ";
        case VK_FORMAT_BC4_UNORM_BLOCK            :
            return "VK_FORMAT_BC4_UNORM_BLOCK           ";
        case VK_FORMAT_BC4_SNORM_BLOCK            :
            return "VK_FORMAT_BC4_SNORM_BLOCK           ";
        case VK_FORMAT_BC5_UNORM_BLOCK            :
            return "VK_FORMAT_BC5_UNORM_BLOCK           ";
        case VK_FORMAT_BC5_SNORM_BLOCK            :
            return "VK_FORMAT_BC5_SNORM_BLOCK           ";
        case VK_FORMAT_BC6H_UFLOAT_BLOCK          :
            return "VK_FORMAT_BC6H_UFLOAT_BLOCK         ";
        case VK_FORMAT_BC6H_SFLOAT_BLOCK          :
            return "VK_FORMAT_BC6H_SFLOAT_BLOCK         ";
        case VK_FORMAT_BC7_UNORM_BLOCK            :
            return "VK_FORMAT_BC7_UNORM_BLOCK           ";
        case VK_FORMAT_BC7_SRGB_BLOCK             :
            return "VK_FORMAT_BC7_SRGB_BLOCK            ";
        case VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK    :
            return "VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK   ";
        case VK_FORMAT_ETC2_R8G8B8_SRGB_BLOCK     :
            return "VK_FORMAT_ETC2_R8G8B8_SRGB_BLOCK    ";
        case VK_FORMAT_ETC2_R8G8B8A1_UNORM_BLOCK  :
            return "VK_FORMAT_ETC2_R8G8B8A1_UNORM_BLOCK ";
        case VK_FORMAT_ETC2_R8G8B8A1_SRGB_BLOCK   :
            return "VK_FORMAT_ETC2_R8G8B8A1_SRGB_BLOCK  ";
        case VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK  :
            return "VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK ";
        case VK_FORMAT_ETC2_R8G8B8A8_SRGB_BLOCK   :
            return "VK_FORMAT_ETC2_R8G8B8A8_SRGB_BLOCK  ";
        case VK_FORMAT_EAC_R11_UNORM_BLOCK        :
            return "VK_FORMAT_EAC_R11_UNORM_BLOCK       ";
        case VK_FORMAT_EAC_R11_SNORM_BLOCK        :
            return "VK_FORMAT_EAC_R11_SNORM_BLOCK       ";
        case VK_FORMAT_EAC_R11G11_UNORM_BLOCK     :
            return "VK_FORMAT_EAC_R11G11_UNORM_BLOCK    ";
        case VK_FORMAT_EAC_R11G11_SNORM_BLOCK     :
            return "VK_FORMAT_EAC_R11G11_SNORM_BLOCK    ";
        case VK_FORMAT_ASTC_4x4_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_4x4_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_4x4_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_4x4_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_5x4_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_5x4_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_5x4_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_5x4_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_5x5_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_5x5_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_5x5_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_5x5_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_6x5_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_6x5_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_6x5_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_6x5_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_6x6_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_6x6_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_6x6_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_6x6_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_8x5_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_8x5_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_8x5_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_8x5_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_8x6_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_8x6_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_8x6_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_8x6_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_8x8_UNORM_BLOCK       :
            return "VK_FORMAT_ASTC_8x8_UNORM_BLOCK      ";
        case VK_FORMAT_ASTC_8x8_SRGB_BLOCK        :
            return "VK_FORMAT_ASTC_8x8_SRGB_BLOCK       ";
        case VK_FORMAT_ASTC_10x5_UNORM_BLOCK      :
            return "VK_FORMAT_ASTC_10x5_UNORM_BLOCK     ";
        case VK_FORMAT_ASTC_10x5_SRGB_BLOCK       :
            return "VK_FORMAT_ASTC_10x5_SRGB_BLOCK      ";
        case VK_FORMAT_ASTC_10x6_UNORM_BLOCK      :
            return "VK_FORMAT_ASTC_10x6_UNORM_BLOCK     ";
        case VK_FORMAT_ASTC_10x6_SRGB_BLOCK       :
            return "VK_FORMAT_ASTC_10x6_SRGB_BLOCK      ";
        case VK_FORMAT_ASTC_10x8_UNORM_BLOCK      :
            return "VK_FORMAT_ASTC_10x8_UNORM_BLOCK     ";
        case VK_FORMAT_ASTC_10x8_SRGB_BLOCK       :
            return "VK_FORMAT_ASTC_10x8_SRGB_BLOCK      ";
        case VK_FORMAT_ASTC_10x10_UNORM_BLOCK     :
            return "VK_FORMAT_ASTC_10x10_UNORM_BLOCK    ";
        case VK_FORMAT_ASTC_10x10_SRGB_BLOCK      :
            return "VK_FORMAT_ASTC_10x10_SRGB_BLOCK     ";
        case VK_FORMAT_ASTC_12x10_UNORM_BLOCK     :
            return "VK_FORMAT_ASTC_12x10_UNORM_BLOCK    ";
        case VK_FORMAT_ASTC_12x10_SRGB_BLOCK      :
            return "VK_FORMAT_ASTC_12x10_SRGB_BLOCK     ";
        case VK_FORMAT_ASTC_12x12_UNORM_BLOCK     :
            return "VK_FORMAT_ASTC_12x12_UNORM_BLOCK    ";
        case VK_FORMAT_ASTC_12x12_SRGB_BLOCK      :
            return "VK_FORMAT_ASTC_12x12_SRGB_BLOCK     ";
        default:
            return "UNKNOWN                             ";
        }
    }

    private static String vkStageFlagToString(int flag)
    {
        String msg = "";
        if(testFlag(flag, VK_SHADER_STAGE_VERTEX_BIT))
        {
            msg += "    VK_SHADER_STAGE_VERTEX_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_VERTEX_BIT;
        }
        if(testFlag(flag, VK_SHADER_STAGE_FRAGMENT_BIT))
        {
            msg += "    VK_SHADER_STAGE_FRAGMENT_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_FRAGMENT_BIT;
        }
        if(testFlag(flag, VK_SHADER_STAGE_GEOMETRY_BIT))
        {
            msg += "    VK_SHADER_STAGE_GEOMETRY_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_GEOMETRY_BIT;
        }
        if(testFlag(flag, VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT))
        {
            msg += "    VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
        }
        if(testFlag(flag, VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT))
        {
            msg += "    VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
        }
        if(testFlag(flag, VK_SHADER_STAGE_COMPUTE_BIT))
        {
            msg += "    VK_SHADER_STAGE_COMPUTE_BIT\n";
            flag &= 0xFFFFFFFF - VK_SHADER_STAGE_COMPUTE_BIT;
        }
        if(flag != 0)
        {
            msg += "    UNKNOWN (" + Integer.toBinaryString(flag) + "b)\n";
        }
        return msg;
    }

    private static boolean testFlag(int data, int flag)
    {
        return (data & flag) == flag;
    }
}