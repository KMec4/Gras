package engine._3d.pickingSystem;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkCommandBuffer;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With3;
import engine.components.Renderable;
import engine._3d.Vertex;
import engine.components.Pickable;
import engine.components.Position;
import engine.ecs.GameObject;
import engine.ecs.ECSSystem;
import engine.vulkan.Device;
import engine.vulkan.GameWindow;
import engine.vulkan.Viewport;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;

public class PickSystem extends ECSSystem<With3<GameObject, Renderable,Position>>
{
    Matrix4f camMatrix = new Matrix4f();
    VkCommandBuffer cmd;
    Pick3d backend;

    public PickSystem(Device d, GameWindow w, Viewport v)
    {
        backend = new Pick3d(d, w, v);
    }

    @Override
    public Results<With3<GameObject, Renderable,Position>> requestData(Dominion ecs)
    {
        return ecs.findEntitiesWith(GameObject.class, Renderable.class, Position.class);
    }

    @Override
    public void onStart()
    {
        cmd = backend.pickingSystemStartRendering(camMatrix);
    }

    @Override
    public void execute(With3<GameObject, Renderable,Position> result, double deltaTime)
    {
        Renderable renderable = result.comp2();
        //Position position = result.comp3();

        Matrix4f transform = new Matrix4f();
        camMatrix.mul(calcMat4(renderable), transform);

        //TODO fix push stuff
        vkCmdPushConstants(
            cmd,
            backend.pipe.getPipelineLayout(),
            VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
            0,
            new Vertex.PushConstantPicker(result.comp1().id, transform).getData()
        );

        renderable.model[0].submitToCommandBuffer(cmd);
    }

    @Override
    public void onEnd()
    {
        Pickable selected = GameObject.getAllGameObjects().get(backend.pickingSystemEndRendering(cmd)).getEntity().get(Pickable.class);
        if(selected != null)
        {
            selected.picked();
        }
    }
    
    static Matrix4f calcMat4(Renderable obj)
    {
        Matrix4f mat = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        return mat
        .translate(obj.translation)
        .setRotationYXZ(obj.rotation.y, obj.rotation.x, obj.rotation.z)
        .scale(obj.scale);
    }
}
