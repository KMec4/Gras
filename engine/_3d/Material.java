package engine._3d;

import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.HashMap;

import dev.dominion.ecs.api.Results.With1;
import engine._3d.VertexFactory.PushConstantInfo;
import engine._3d.VertexFactory.VertexBlueprint;
import engine.components.Renderable;
import engine.vulkan.Descriptors.DescriptorSetLayout;
import engine.vulkan.Shaders;

import static org.lwjgl.vulkan.VK10.*;

public abstract class Material
{
    public static final VertexBlueprint DEFAULT_BLUEPRINT = new VertexBlueprint(
        new int[] { VK_FORMAT_R32G32B32_SFLOAT, VK_FORMAT_R32G32B32_SFLOAT, VK_FORMAT_R32G32B32_SFLOAT, VK_FORMAT_R32G32_SFLOAT},
        new int[] { 3                         , 3                         , 3                         , 2                      }
    );
    public static final PushConstantInfo DEFAULT_PUSH_CONSTANT = new PushConstantInfo(128, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);

    static HashMap<String, Material> materials = new HashMap<String, Material>();
    public static Material getMaterial(String tag)
    {
        Material m = materials.get(tag);
        if( m == null )
        {
            System.getLogger("").log(Level.ERROR, "Cannot find Material [ " + tag + "]");
            return getMaterial("DEFAULT");
        }
        return m;
    }

    //Object
    public String tag;
    public Shaders shaders;
    int MaterialID;

    public Material(String vertexPath, String fragmentPath)
    {
        shaders.vertexShaderPath = vertexPath;
        shaders.fragmentShaderPath = fragmentPath;
        shaders.vertexBlueprint = DEFAULT_BLUEPRINT;
        shaders.pushInfo = DEFAULT_PUSH_CONSTANT;
        shaders.descriptorSetLayouts = new DescriptorSetLayout[0];
        tag = "Material" + materials.size();
    }

    public Material(String vertexPath, String fragmentPath, String tag)
    {
        shaders = new Shaders (vertexPath, fragmentPath);
        shaders.vertexBlueprint = DEFAULT_BLUEPRINT;
        shaders.pushInfo = DEFAULT_PUSH_CONSTANT;
        shaders.descriptorSetLayouts = new DescriptorSetLayout[0];
        this.tag = tag;
    }

    public Material(Shaders shaders, String tag)
    {
        this.shaders = shaders;
        this.tag = tag;
        shaders.vertexBlueprint = DEFAULT_BLUEPRINT;
        shaders.pushInfo = DEFAULT_PUSH_CONSTANT;
        shaders.descriptorSetLayouts = new DescriptorSetLayout[0];
    }

    public abstract void getPushConstantData(ByteBuffer dest, With1<Renderable> obj);
    public void bindDescriptorSets()
    {
        return;
    }
}