package engine.systems;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.stream.Stream;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.system.MemoryStack;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With1;
import engine.Player;
import engine._3d.pickingSystem.PickSystem;
import engine.components.Rotateable;
import engine.ecs.ScheduledSystem;
import engine.vulkan.GameWindow;

public class UserInputSystem extends ScheduledSystem<With1<Player>>
{
    Player player;
    PickSystem select = null;
    GameWindow w;

    public void init(GameWindow window, Player p)
    {
        player = p;
        w = window;
        glfwSetKeyCallback(window.getWindow(), new Callback());
    }

    public void linkPick3d(PickSystem instance)
    {
        select = instance;
    }

    private class Callback implements GLFWKeyCallbackI
    {

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods)
        {
            Vector2f rotation = new Vector2f(0f, 0f);
            switch(key)
            {
                case GLFW_KEY_W:
                    if(action == GLFW_RELEASE)
                    {
                        player.viewport.setSpeedM_S(0f);
                    }
                    else
                    {
                        player.viewport.setSpeedM_S(1.3f);
                    }
                    break;
                case GLFW_KEY_S:
                    if(action == GLFW_RELEASE)
                    {
                        player.viewport.setSpeedM_S(0f);
                    }
                    else
                    {
                        player.viewport.setSpeedM_S(-1.3f);
                    }
                    break;
                case GLFW_KEY_DOWN:
                    rotation.x -= 0.01f;
                    break;
                case GLFW_KEY_UP:
                    rotation.x += 0.01f;
                    break;
                case GLFW_KEY_LEFT:
                    rotation.y -= 0.01f;
                    break;
                case GLFW_KEY_RIGHT:
                    rotation.y += 0.01f;
                    break;
                case GLFW_KEY_ENTER:
                    if(select != null)
                    {
                        new Thread(select).start();
                    }
                    else
                    {
                        System.out.println("no picker");
                    }
                    break;

                }
        }
        
    }
/*
    double timeWReleased;
    public void doKeyboardEvents(Player p, double deltaTime)
    {
        glfwGetKey(w.getWindow(), p.keyBindings.moveForwards)
    }*/

    double xpos, ypos;
    public void doMouseEvents(Player p, double deltaTime)
    {
        try(MemoryStack stack = stackPush())
        {
            DoubleBuffer mouseX = stack.callocDouble(1);
            DoubleBuffer mouseY = stack.callocDouble(1);

            glfwGetCursorPos(w.getWindow(), mouseX, mouseY);

            float rotSpeedX = (float) (xpos - mouseX.get(0)) / 64f;
            float rotSpeedY = (float) (ypos - mouseY.get(0)) / 64f;

            xpos = mouseX.get(0);
            ypos = mouseY.get(0);


            if(rotSpeedX > p.maxRotationSpeedRad_s.x)
            {
                rotSpeedX = p.maxRotationSpeedRad_s.x;
            }
            else if(rotSpeedX < -p.maxRotationSpeedRad_s.x)
            {
                rotSpeedX = -p.maxRotationSpeedRad_s.x;
            }
            
            if(rotSpeedY > p.maxRotationSpeedRad_s.y)
            {
                rotSpeedY = p.maxRotationSpeedRad_s.y;
            }
            else if(rotSpeedY < -p.maxRotationSpeedRad_s.y)
            {
                rotSpeedY = -p.maxRotationSpeedRad_s.y;
            }

            p.viewport.getEntity().get(Rotateable.class).speeds.set(rotSpeedY, -rotSpeedX, 0);
        }
    }


    @Override
    public void execute(With1<Player> result, double deltaTime)
    {
        doMouseEvents(result.comp(), deltaTime);
    }

    @Override
    public Results<With1<Player>> requestData(Dominion ecs)
    {
        Results.With1<Player> result = new Results.With1<Player>(player, null);
        return new Results<With1<Player>>(){

            @Override
            public Iterator<With1<Player>> iterator() { throw new UnsupportedOperationException("Unimplemented method 'iterator'"); }
            @Override
            public Stream<With1<Player>> stream() { return Stream.of(result); }
            @Override
            public Stream<With1<Player>> parallelStream() { throw new UnsupportedOperationException("Unimplemented method 'parallelStream'"); }
            @Override
            public Results<With1<Player>> without(Class<?>... componentTypes) { throw new UnsupportedOperationException("Unimplemented method 'without'"); }
            @Override
            public Results<With1<Player>> withAlso(Class<?>... componentTypes) { throw new UnsupportedOperationException("Unimplemented method 'withAlso'"); }
            @Override
            public <S extends Enum<S>> Results<With1<Player>> withState(S state) { throw new UnsupportedOperationException("Unimplemented method 'withState'"); }
        };
    }
    
}