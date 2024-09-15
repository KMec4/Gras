package engine.menu;

import org.joml.Matrix4f;

import dev.dominion.ecs.api.Results.With1;
import engine._3d.Material;
import engine._3d.RenderSystem3d;
import engine.components.Renderable;

import java.nio.ByteBuffer;

public class MenuRenderer
{
    Material menuMaterial;
    
    public MenuRenderer(RenderSystem3d sys)
    {
        menuMaterial = new Material("engine/menu/shaders/vert.spirv", "engine/menu/shaders/frag.spirv")//, new Vertex.SolidVertex(new Vector3f(), new Vector3f(), new Vector3f()))
        {

            @Override
            public void getPushConstantData(ByteBuffer dest, Matrix4f camMatrix, Matrix4f viewMatrix, With1<Renderable> obj)
            {
                return;
            }
        };
        try
        {
            sys.addMaterial(menuMaterial);
        }
        catch (Exception e)
        {}
    }

    public static class ButtonStyle
    {

    }

    public static class TextAreaStyle
    {

    }

    public static class GraphStyle
    {

    }

}