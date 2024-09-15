

import java.io.File;
import java.io.IOException;

import org.joml.Vector3f;
import org.joml.Vector2f;
import org.lwjgl.system.Configuration;

import dev.dominion.ecs.api.Results.With2;
import engine.Player;
import engine.Vulkan;
import engine._3d.Material;
import engine._3d.Mesh;
import engine._3d.MeshUtils;
import engine._3d.Vertex;
import engine._3dObjects.Sphere;
import engine.components.Moveable;
import engine.components.Position;
import engine.components.Renderable;
import engine.components.Rotateable;
import engine.ecs.ECS;
import engine.ecs.GameObject;
import engine.physics.CollisionBox;
import engine.physics.GravitySystem;
import engine.physics.PhysSys;
import engine.physics.Physics;
import engine.systems.MovingSystem;
import engine.systems.RotationSystem;
import engine.systems.UserInputSystem;
import engine.world.Chunk;
import res.materials.gras.GrasMaterial;
import res.materials.normal.NormalMaterial;
import res.materials.semiTransparent.SemiTransparentMaterial;

public class main
{
    static
    {
        Configuration.STACK_SIZE.set(2048);
        System.setProperty("dominion.dominion-1.system-timeout-seconds", "3");
        //System.setProperty("dominion.logging-level", "ALL");
        //System.setProperty("dominion.dominion-1.logging-level", "ALL");
    }

    public static void main(String[] args) throws IOException
    {
        ECS.init();
        Vulkan engine = Vulkan.createDefaultInstance();
        UserInputSystem ui = new UserInputSystem();
        ui.init(engine.getWindow(), new Player(engine.getViewport()));
        //ui.linkPick3d(new Pick3d(engine.getDevice(), engine.getWindow(), engine.getViewport()));

        new GrasMaterial(engine.getRenderSystem());
        new NormalMaterial(engine.getRenderSystem());
        new SemiTransparentMaterial(engine.getRenderSystem());


        Mesh mesh = MeshUtils.loadMeshFromFile(new File("./res/model/simpleplane.obj"), new Vector3f(1f, 0f, .5f));
        Renderable r = new Renderable(mesh);
        r.model[0].setMaterial( Material.getMaterial("NORMALS") );
        r.scale = new Vector3f(8f);

        new GameObject( r , new Physics(), new Position(new Vector3f(), new Vector3f(0f, 1f, 0f)) ).getEntity().get(Physics.class).setMeshForPhysics(mesh);


        //File f = new File("Chunk1");
        //f.createNewFile();
        //Chunk.generateChunk(100, 100, 3.0f, 0, 0, 0).saveChunk(f);


        engine.allowRendering(true);
        System.err.println("main ready");

        new RotationSystem();
        new MovingSystem();
        new PhysSys();
        new GravitySystem();
        ECS.startAll();

        for(;;)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            Mesh m = new Sphere(10.0f, new Vector3f(1f));
            Renderable renderable = new Renderable(m);
            renderable.translation.add(.0f,-10f, 0f);
            GameObject g = new GameObject(
                renderable,
                new Physics(),
                new Moveable(),
                new Position(renderable.translation, new Vector3f(0, 1, 0))
                //new Rotateable(renderable.rotation, new Vector3f(0.1f, 0, 0.1f))
            );
            m.setMaterial(Material.getMaterial("NORMALS"));
            g.getEntity().get(Renderable.class).scale = new Vector3f(0.02f);
            g.getEntity().get(Physics.class).setMeshForPhysics(m);
            g.getEntity().get(Position.class).direction = g.getEntity().get(Moveable.class).direction;
        }


        //new Page(new MenuRenderer(engine.getRenderSystem()));
    }

    static class Gras extends GameObject
    {
        static Mesh model = new Mesh(6, Material.getMaterial("GRAS"));

        static
        {
            int x = 0;
            int z = 0;
            for(Vertex v : model)
            {
                v.n(new Vector3f(x, 1.0f, z));
                v.n(new Vector3f(x, 1.0f, z));
                v.n(new Vector3f(x, 1.0f, z));
                v.n(new Vector2f(x, 1.0f));
            }
            model.flush();
        };

        public Gras()
        {
            super(
                new Renderable(model)
            );
        }
    }

    static class figur extends GameObject
    {
        static Mesh mesh = MeshUtils.loadMeshFromFile(new File("./res/model/Bauer.obj"), new Vector3f(1f, 0f, .5f));

        public figur()
        {
            super(
                new Renderable(mesh),
                new Physics(),
                new Moveable(),
                new Position()
            );
            mesh.setMaterial(Material.getMaterial("GRAS"));
            getEntity().get(Physics.class).setMeshForPhysics(mesh);
            getEntity().get(Renderable.class).scale = new Vector3f(0.02f);
        }
    }
}
