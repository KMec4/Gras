package engine.systems;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.magicwerk.brownies.collections.BigList;

public class AnimationSystem
{
    static BigList<Animation<?>> animations = new BigList<Animation<?>>();
    static boolean CPUPrefered = true;
    static Thread animationThread;
    static final float minDT = .2f;

    public static void init()
    {
        animationThread = new ActionLoop(1000000);
        animationThread.start();
    }

    public static void add(Animation<?> a)
    {
        animations.add(a);
    }

    public static void remove(Animation<?> a)
    {
        animations.remove(a);
    }

    public static <T> void exec(Animation<T> a, float dt)
    {
        if(CPUPrefered && a.cpuA != null)
        {
            a.cpuA.animate(a.object, 1f);
        }
        else if(a.gpuA != null)
        {
            a.gpuA.animate(a.object, 1f);
        }
    }
    

    private static class ActionLoop extends Thread
    {
        int timeout;

        public ActionLoop(int timeoutMS)
        {
            super("AnimationSystem");
            this.timeout = timeoutMS;
        }
        @Override
        public void run()
        {
            long start = System.nanoTime();
            for(;;)
            {
                long now = System.nanoTime();
                float diff = ( (float) now - start ) / 1000000;
                if(diff < minDT)
                {
                    continue;
                }
                start = now;

                ExecutorService execute = Executors.newSingleThreadExecutor();
                for(Animation<?> a : animations)
                {
                    Future<String> future = execute.submit(new Callable<String>()
                    {
                        @Override
                        public String call() throws Exception
                        {
                            exec(a, diff);
                            return "";
                        }

                    });
                    try
                    {
                        future.get(timeout, TimeUnit.MILLISECONDS);
                    }
                    catch(TimeoutException e)
                    {
                        future.cancel(true);
                        System.out.println("A Animation took too long to execute");
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                execute.shutdownNow();
            }
        }
    }
}
