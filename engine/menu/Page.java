package engine.menu;

import org.joml.Vector3f;

import engine._3d.Mesh;
import engine.components.Renderable;
import engine.ecs.GameObject;

public class Page extends GameObject
{
    static Mesh model = new Mesh(6);

    static
    {
        model.next() .n( new Vector3f(-1f, -1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
        model.next() .n( new Vector3f( 1f, -1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
        model.next() .n( new Vector3f(-1f,  1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
        model.next() .n( new Vector3f( 1f, -1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
        model.next() .n( new Vector3f( 1f,  1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
        model.next() .n( new Vector3f(-1f,  1f, 0f) ) .n( new Vector3f() ) .n( new Vector3f() );
    };
    /*public static Page createPage(File pageData, MenuRenderer renderer)
    {
        return null;
    }*/

    public Page(MenuRenderer renderer)
    {
        super(
            new Renderable(model)
        );
    }
}
