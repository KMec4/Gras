package engine;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Vector2f;

import engine.vulkan.Viewport;

public class Player
{
    public static class KeyBindings
    {
        public int moveForwards  = GLFW_KEY_W;
        public int moveBackwards = GLFW_KEY_S;
    }

    public Viewport viewport;
    public float maxSpeedm_s;
    public float accelerationm2_s;
    public Vector2f maxRotationSpeedRad_s;
    public KeyBindings keyBindings;


    public Player(Viewport v)
    {
        maxSpeedm_s = 1.3f;
        accelerationm2_s = 1f;
        maxRotationSpeedRad_s = new Vector2f(1.6f, 1.0f);
        viewport = v;
        keyBindings = new KeyBindings();

    }
}
