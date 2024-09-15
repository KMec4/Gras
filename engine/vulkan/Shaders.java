package engine.vulkan;

import engine._3d.VertexFactory.PushConstantInfo;
import engine._3d.VertexFactory.VertexBlueprint;
import engine.vulkan.Descriptors.DescriptorSetLayout;

public class Shaders
{
    public String vertexShaderPath;
    public String geometryShaderPath;
    public String tessellationControlShaderPath;
    public String tessellationEvaluationShaderPath;
    public String fragmentShaderPath;

    public PushConstantInfo pushInfo;
    public VertexBlueprint vertexBlueprint;
    public DescriptorSetLayout[] descriptorSetLayouts;

    public Shaders(String vert, String frag)
    {
        vertexShaderPath = vert;
        fragmentShaderPath = frag;
    }

    public Shaders(String vert, String geo, String tessellControl, String tessellEvaluation, String frag)
    {
        vertexShaderPath = vert;
        fragmentShaderPath = frag;
        geometryShaderPath = geo;
        tessellationControlShaderPath = tessellControl;
        tessellationEvaluationShaderPath = tessellEvaluation;
    }
}