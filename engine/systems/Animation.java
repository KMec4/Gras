package engine.systems;


public class Animation<T>
{
    CPUAnimationAction<T> cpuA;
    GPUAnimationAction<T> gpuA;
    T object;

    public interface CPUAnimationAction<T>
    {
        public void animate(T object, float deltaTime);
    }

    public interface GPUAnimationAction<T>
    {
        public void animate(T object, float deltaTime);
    }

    public Animation (T t)
    {
        object = t;
    }

    public void makeCPUPowered(CPUAnimationAction<T> action)
    {
        cpuA = action;
    }

    public void makeGPUPowered(GPUAnimationAction<T> action)
    {
        gpuA = action;
    }

    public void enable()
    {
        AnimationSystem.add(this);
    }

    public void disable()
    {
        AnimationSystem.remove(this);
    }
}
