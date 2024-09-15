package res.materials.semiTransparent;

import java.nio.ByteBuffer;

import dev.dominion.ecs.api.Results.With1;
import engine._3d.Material;
import engine._3d.RenderSystem3d;
import engine._3d.VertexFactory.PushConstantInfo;
import engine.components.Renderable;
import engine.vulkan.Shaders;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;

public class SemiTransparentMaterial extends Material
{

    public SemiTransparentMaterial(RenderSystem3d sys)
    {
        super(new Shaders("res/materials/semiTransparent/vert.spirv", "res/materials/semiTransparent/frag.spirv"), "SEMI-TRANSPARENT");
        
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
