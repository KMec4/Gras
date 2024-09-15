package engine.menu.components;

import java.io.File;

import engine._3d.Mesh;

public class TextStyle
{
    private Mesh[] models = new Mesh[255];

    public TextStyle(File trueTypeFont, int size)
    {

    }

    public Mesh getModel(char c)
    {
        return models[c];
    }

}
