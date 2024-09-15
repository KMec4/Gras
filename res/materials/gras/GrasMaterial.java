package res.materials.gras;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;

import java.nio.ByteBuffer;

import dev.dominion.ecs.api.Results.With1;
import engine._3d.Material;
import engine._3d.RenderSystem3d;
import engine._3d.VertexFactory.PushConstantInfo;
import engine.components.Renderable;
import engine.vulkan.Shaders;

public class GrasMaterial extends Material
{

    public GrasMaterial(RenderSystem3d sys)
    {
        super(new Shaders
        (
            "res/materials/gras/vert.spirv",
            "res/materials/gras/geom.spirv",
            "res/materials/gras/tesc.spirv",
            "res/materials/gras/tese.spirv",
            "res/materials/gras/frag.spirv"
        ), "GRAS");
        super.shaders.pushInfo = new PushConstantInfo(128, VK_SHADER_STAGE_ALL_GRAPHICS);
        try
        {
            sys.addMaterial(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void getPushConstantData(ByteBuffer dest, With1<Renderable> obj)
    {
        Material.getMaterial("DEFAULT").getPushConstantData(dest, obj);
    }
}
