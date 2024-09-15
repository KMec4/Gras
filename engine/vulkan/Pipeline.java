package engine.vulkan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import engine.VkResultDecoder;
import engine.vulkan.Descriptors.DescriptorSetLayout;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Pipeline
{

    long graphicsPipeline = 0L;
    long pipelineLayout = 0L;

    public Pipeline(
        VkDevice logical,
        long renderPass,
        Shaders shaders
    )
    {
        try(MemoryStack stack = stackPush())
        {

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
            vertexInputInfo.set(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO, 0L, 0, shaders.vertexBlueprint.getBindingDescriptions(stack), shaders.vertexBlueprint.getAttributeDescriptions(stack) );

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
            inputAssembly.sType     (VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            if(shaders.tessellationControlShaderPath != null && shaders.tessellationEvaluationShaderPath != null)
            {
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_PATCH_LIST);
            }
            else
            {
                inputAssembly.topology  (VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            }
            inputAssembly.primitiveRestartEnable(false);

            // ===> VIEWPORT & SCISSOR <===

            /*
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(chain.getSwapChainExtent().width());
            viewport.height(chain.getSwapChainExtent().height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(chain.getSwapChainExtent());
            */

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
            //viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            //viewportState.pViewports(viewport);
            //viewportState.pScissors(scissor);

            viewportState.set(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO, 0L, 0, 1, null, 1, null);
            

            // ===> RASTERIZATION STAGE <===

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
            rasterizer.sType                  (VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable       (false            );
            rasterizer.rasterizerDiscardEnable(false            );
            rasterizer.polygonMode            (VK_POLYGON_MODE_FILL   );
            rasterizer.lineWidth              (1.0f             );
            rasterizer.cullMode               (VK_CULL_MODE_NONE      );
            rasterizer.frontFace              (VK_FRONT_FACE_CLOCKWISE);
            rasterizer.depthBiasEnable        (false            );
            rasterizer.depthBiasConstantFactor(0.0f             );
            rasterizer.depthBiasClamp         (0.0f             );
            rasterizer.depthBiasSlopeFactor   (0.0f             );

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable  (false          );
            multisampling.rasterizationSamples (VK_SAMPLE_COUNT_1_BIT);
            multisampling.minSampleShading     (1.0f           );
            multisampling.pSampleMask          (null           );
            multisampling.alphaToCoverageEnable(false          );
            multisampling.alphaToOneEnable     (false          );

            // ===> COLOR BLENDING <===

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
            colorBlendAttachment.colorWriteMask     (VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable        (false          );
            colorBlendAttachment.srcColorBlendFactor( VK_BLEND_FACTOR_ONE );  // Optional
            colorBlendAttachment.dstColorBlendFactor( VK_BLEND_FACTOR_ZERO);  // Optional
            colorBlendAttachment.colorBlendOp       ( VK_BLEND_OP_ADD     );  // Optional
            colorBlendAttachment.srcAlphaBlendFactor( VK_BLEND_FACTOR_ONE );  // Optional
            colorBlendAttachment.dstAlphaBlendFactor( VK_BLEND_FACTOR_ZERO);  // Optional
            colorBlendAttachment.alphaBlendOp       ( VK_BLEND_OP_ADD     );  // Optional

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
            colorBlending.sType         (VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable (false         );
            colorBlending.logicOp       (VK_LOGIC_OP_COPY    );
            colorBlending.pAttachments  (colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            colorBlending.blendConstants().put(0, 0.0f);  // Optional
            colorBlending.blendConstants().put(1, 0.0f);  // Optional
            colorBlending.blendConstants().put(2, 0.0f);  // Optional
            colorBlending.blendConstants().put(3, 0.0f);  // Optional

            // ===> DEPTH STENCIL <===

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
            depthStencil.sType                ( VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO );
            depthStencil.depthTestEnable      ( true         );   
            depthStencil.depthWriteEnable     ( true         );   
            depthStencil.depthCompareOp       ( VK_COMPARE_OP_LESS );
            depthStencil.depthBoundsTestEnable( false         );   // true
            depthStencil.minDepthBounds       ( 0.0f         );       // Optional
            depthStencil.maxDepthBounds       ( 1.0f         );       // Optional
            depthStencil.stencilTestEnable    ( false        );   
            depthStencil.front                (                    );       // Optional
            depthStencil.back                 (                    );       // Optional

            // ===> DYNAMIC STATE <===

            VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack);
            
            IntBuffer dynamicStateEnables = stack.mallocInt(2);
            dynamicStateEnables.put(0, VK_DYNAMIC_STATE_VIEWPORT);
            dynamicStateEnables.put(1, VK_DYNAMIC_STATE_SCISSOR );

            dynamicState.sType            (VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO );
            dynamicState.pDynamicStates   ( dynamicStateEnables );
            dynamicState.flags            (0              );

            // ===> PIPELINE LAYOUT CREATION <===

            LongBuffer dsl = stack.callocLong(shaders.descriptorSetLayouts.length);
            for(DescriptorSetLayout d : shaders.descriptorSetLayouts)
            {
                dsl.put( d.getDescriptorSetLayout() );
            }
            dsl.rewind();
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType                  ( VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.setLayoutCount         ( shaders.descriptorSetLayouts.length          );
            pipelineLayoutInfo.pSetLayouts            ( dsl                                          );
            pipelineLayoutInfo.pPushConstantRanges    ( shaders.pushInfo.getPushConstantRange(stack) );

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            if(vkCreatePipelineLayout(logical, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS)
            {
                throw new RuntimeException("Failed to create pipeline layout");
            }
  

            // >>>> CREATE PIPE <<<<

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);

            pipelineInfo.stageCount (2);
            pipelineInfo.pStages    ( createShaderStages(stack, logical, shaders) ); // shader creation
            if(shaders.tessellationControlShaderPath != null && shaders.tessellationEvaluationShaderPath != null)
            {
                pipelineInfo.pTessellationState
                (
                    VkPipelineTessellationStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .patchControlPoints(3)
                );
            }

            pipelineInfo.pVertexInputState  (vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState     (viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState  (multisampling);
            pipelineInfo.pColorBlendState   (colorBlending);
            pipelineInfo.pDepthStencilState (depthStencil);
            pipelineInfo.pDynamicState      (dynamicState);

            pipelineInfo.layout     (pPipelineLayout.get(0));
            pipelineInfo.renderPass (renderPass);
            pipelineInfo.subpass    (0);

            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex (-1);


            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            int result = vkCreateGraphicsPipelines(logical, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to create GraphicsPipeline; ErrorCode [VkResult]: " + result + " = " + VkResultDecoder.decode(result));
                throw e;
            }
            graphicsPipeline = pGraphicsPipeline.get(0);
            pipelineLayout = pPipelineLayout.get(0);
        }       
    }

    public long getPipelineLayout()
    {
        return pipelineLayout;
    }

    public long getPipeline()
    {
        return graphicsPipeline;
    }

    private long loadShader(MemoryStack stack, String path, VkDevice device)
    {
        try 
        {
            LongBuffer module = stack.mallocLong(1);
            File f = new File(path);
            
            if(!f.canRead())
            {
                RuntimeException e = new RuntimeException("Recourse File isn't readable:\nFile " + f.getAbsoluteFile() + "\nDoes Exist: " + f.exists() + "\nIs File: " + f.isFile() );
                throw e;
            }
            FileInputStream fis = new FileInputStream(f);
            ByteBuffer b = stack.calloc( (int)fis.getChannel().size() + 1 );
            FileChannel fc = fis.getChannel();
            while( fc.read(b) != -1 );
            fis.close();
            b.flip();

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(b);

            int result = vkCreateShaderModule(device, createInfo, null, module);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to create shader module: [VK_Result] " + result + " Meaning: " + VkResultDecoder.decode(result));
                throw e;
            }
            return module.get(0);
        }
        catch (FileNotFoundException e)
        {
            //logger.log(Level.WARNING, " -> Shader file does not exist!", e);
            //module = 0L;
            return 0;
        }
        catch (SecurityException e)
        {
            //logger.log(Level.WARNING, " -> Can not read Shader file!", e);
            //module = 0L;
            return 0;
        }
        catch (IOException e)
        {
            //logger.log(Level.WARNING, " -> Can not read Shader file!", e);
            //module = 0L;
            return 0;
        }
    }

    private VkPipelineShaderStageCreateInfo.Buffer createShaderStages(MemoryStack stack, VkDevice logical, Shaders info)
    {
        int shaderCount = 0;
        if(info.fragmentShaderPath != null) shaderCount++;
        if(info.geometryShaderPath != null) shaderCount++;
        if(info.tessellationControlShaderPath != null) shaderCount++;
        if(info.tessellationEvaluationShaderPath != null) shaderCount++;
        if(info.vertexShaderPath != null) shaderCount++;

        //ShaderStages______________________________________________________

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(shaderCount, stack);
        int i = 0;

        if(info.fragmentShaderPath != null)
        {
            shaderStages.get(i).sType (VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            shaderStages.get(i).stage (VK_SHADER_STAGE_FRAGMENT_BIT);
            shaderStages.get(i).module( loadShader(stack, info.fragmentShaderPath, logical));
            shaderStages.get(i).pName (stack.UTF8("main"));
            shaderStages.get(i).flags (0);
            shaderStages.get(i).pSpecializationInfo(null);
            i++;
        }
        if(info.geometryShaderPath != null)
        {
            shaderStages.get(i).sType (VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            shaderStages.get(i).stage (VK_SHADER_STAGE_GEOMETRY_BIT);
            shaderStages.get(i).module( loadShader(stack, info.geometryShaderPath, logical));
            shaderStages.get(i).pName (stack.UTF8("main"));
            shaderStages.get(i).flags (0);
            shaderStages.get(i).pSpecializationInfo(null);
            i++;
        }
        if(info.tessellationControlShaderPath != null)
        {
            shaderStages.get(i).sType (VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            shaderStages.get(i).stage (VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT);
            shaderStages.get(i).module( loadShader(stack, info.tessellationControlShaderPath, logical));
            shaderStages.get(i).pName (stack.UTF8("main"));
            shaderStages.get(i).flags (0);
            shaderStages.get(i).pSpecializationInfo(null);
            i++;
        }
        if(info.tessellationEvaluationShaderPath != null)
        {
            shaderStages.get(i).sType (VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            shaderStages.get(i).stage (VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT);
            shaderStages.get(i).module( loadShader(stack, info.tessellationEvaluationShaderPath, logical));
            shaderStages.get(i).pName (stack.UTF8("main"));
            shaderStages.get(i).flags (0);
            shaderStages.get(i).pSpecializationInfo(null);
            i++;
        }
        if(info.vertexShaderPath != null)
        {
            shaderStages.get(i).sType (VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            shaderStages.get(i).stage (VK_SHADER_STAGE_VERTEX_BIT);
            shaderStages.get(i).module( loadShader(stack, info.vertexShaderPath, logical));
            shaderStages.get(i).pName (stack.UTF8("main"));
            shaderStages.get(i).flags (0);
            shaderStages.get(i).pSpecializationInfo(null);
            i++;
        }

        return shaderStages;

    }

}
        
