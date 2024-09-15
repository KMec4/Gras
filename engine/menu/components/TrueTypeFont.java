package engine.menu.components;

/*
 * source: https://github.com/SebLague/Text-Rendering/
 * [ ported to java! (; ]
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TrueTypeFont
{
    int numberOfTables;

    // Flag masks
    private static final int OnCurve = 0;
    private static final int IsSingleByteX = 1;
    private static final int IsSingleByteY = 2;
    private static final int Repeat = 3;
    private static final int InstructionX = 4;
    private static final int InstructionY = 5;

    public static TrueTypeFont readFile(File f) throws FileNotFoundException, IOException
    {
        TrueTypeFont font = new TrueTypeFont();

        FileInputStream fis = new FileInputStream(f);
        ByteBuffer data = ByteBuffer.wrap(fis.readAllBytes());
        fis.close();

        skip(data, 4);
        font.numberOfTables = data.getShort();
        skip(data, 6);

        System.out.println("Number of Tables: " + font.numberOfTables);
        System.out.println("--------------------------------");

        for(int i = 0; i < font.numberOfTables; i++)
        {
            String tag   = readString(data, 4);
            int checksum = data.getInt();
            int offset   = data.getInt();
            int length   = data.getInt();
            System.out.println("TAG: " + tag + " LOCATION: " + offset);
        }
            

        return null;
    }

    static GlyphData[] readAllGlyphs(ByteBuffer buffer, int[] glyphLocations, GlyphMap[] mappings)
    {
        GlyphData[] glyphs = new GlyphData[mappings.length];

        for (int i = 0; i < mappings.length; i++)
        {
            GlyphMap mapping = mappings[i];

            GlyphData glyphData = readGlyph(buffer, glyphLocations, mapping.glyphIndex);
            glyphData.unicodeValue = mapping.unicode;
            glyphs[i] = glyphData;
        }

        return glyphs;
    }

    public static GlyphData readGlyph(ByteBuffer buffer, int[] glyphLocations, int glyphIndex)
    {
        int glyphLocation = glyphLocations[glyphIndex];
        buffer.position(glyphLocation);
        int contourCount = buffer.getShort();

        GlyphData glyphData = new GlyphData();
        glyphData.glyphIndex = glyphIndex;

        glyphData.minX = buffer.getShort();
        glyphData.minY = buffer.getShort();
        glyphData.maxX = buffer.getShort();
        glyphData.maxY = buffer.getShort();

        if(contourCount >= 0) // than is simple glyphe
        {
            

            // Read contour ends
            int numPoints = 0;
            int[] contourEndIndices = new int[contourCount];

            for (int i = 0; i < contourCount; i++)
            {
                int contourEndIndex = buffer.getShort();
                numPoints = Math.max(numPoints, contourEndIndex + 1);
                contourEndIndices[i] = contourEndIndex;
            }

            int instructionsLength = buffer.getShort();
            skip(buffer, instructionsLength); // skip instructions (hinting stuff)

            byte[] allFlags = new byte[numPoints];
            Point[] points = new Point[numPoints];

            for (int i = 0; i < numPoints; i++)
            {
                byte flag = buffer.get();
                allFlags[i] = flag;

                if (testFlag(flag, Repeat))
                {
                    int repeatCount = buffer.get();

                    for (int r = 0; r < repeatCount; r++)
                    {
                        i++;
                        allFlags[i] = flag;
                    }
                }
            }

            readCoords(true, numPoints, allFlags, buffer, points);
            readCoords(false, numPoints, allFlags, buffer, points);
            glyphData.points = points;
            glyphData.contourEndIndices = contourEndIndices;
        }
        else // is compound glyphe
        {
            ArrayList<Point> allPoints = new ArrayList<Point>(64);
            ArrayList<Integer> allContourEndIndices = new ArrayList<Integer>(64);

            while (true)
            {
                GlyphData componentGlyph = new GlyphData();
                boolean hasMoreGlyphs = false;
                try
                {
                    hasMoreGlyphs = readNextComponentGlyph(buffer, glyphLocations, glyphLocation, componentGlyph);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // Add all contour end indices from the simple glyph component to the compound glyph's data
                // Note: indices must be offset to account for previously-added component glyphs
                for (int endIndex : componentGlyph.contourEndIndices)
                {
                    allContourEndIndices.add(endIndex + allPoints.size());
                }
                allPoints.addAll(Arrays.asList(componentGlyph.points));

                if (!hasMoreGlyphs) break;
            }

            glyphData.points = allPoints.toArray(new Point[0]);
            int[] allIndices = new int[allContourEndIndices.size()];
            for(int i = 0; i < allIndices.length; i++)
            {
                allIndices[i] = allContourEndIndices.get(i);
            }
            glyphData.contourEndIndices = allIndices;
        }
        return glyphData;
    }

    static private void readCoords(boolean readingX, int numPoints, byte[] allFlags, ByteBuffer buffer, Point[] points)
    {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        int singleByteFlagBit;
        int instructionFlagMask;
        if(readingX)
        {
            singleByteFlagBit = IsSingleByteX;
            instructionFlagMask = InstructionX;
        }
        else
        {
            singleByteFlagBit = IsSingleByteY;
            instructionFlagMask = InstructionY;
        }

        int coordVal = 0;

        for (int i = 0; i < numPoints; i++)
        {
            byte currFlag = allFlags[i];

            // Offset value is represented with 1 byte (unsigned)
            // Here the instruction flag tells us whether to add or subtract the offset
            if (testFlag(currFlag, singleByteFlagBit))
            {
                int coordOffset = buffer.get();
                boolean positiveOffset = testFlag(currFlag, instructionFlagMask);
                coordVal += positiveOffset ? coordOffset : -coordOffset;
            }
            // Offset value is represented with 2 bytes (signed)
            // Here the instruction flag tells us whether an offset value exists or not
            else if (!testFlag(currFlag, instructionFlagMask))
            {
                coordVal += buffer.getShort();
            }

            if (readingX) points[i].X = coordVal;
            else points[i].Y = coordVal;
            points[i].OnCurve = testFlag(currFlag, OnCurve);

            min = Math.min(min, coordVal);
            max = Math.max(max, coordVal);
        }
    }
    
    static private  boolean readNextComponentGlyph(ByteBuffer buffer, int[] glyphLocations, int glyphLocation, GlyphData dest) throws Exception
    {
        int flag = buffer.getShort();
        int glyphIndex = buffer.getShort();

        int componentGlyphLocation = glyphLocations[glyphIndex];
        // If compound glyph refers to itself, return empty glyph to avoid infinite loop.
        // Had an issue with this on the 'carriage return' character in robotoslab.
        // There's likely a bug in my parsing somewhere, but this is my work-around for now...
        if (componentGlyphLocation == glyphLocation)
        {
            dest = new GlyphData();
            dest.points = new Point[0];
            dest.contourEndIndices = new int[0];
            return  false;
        }

        // Decode flags
        boolean argsAre2Bytes             = testFlag(flag, 0);
        boolean argsAreXYValues           = testFlag(flag, 1);
        boolean roundXYToGrid             = testFlag(flag, 2);
        boolean isSingleScaleValue        = testFlag(flag, 3);
        boolean isMoreComponentsAfterThis = testFlag(flag, 5);
        boolean isXAndYScale              = testFlag(flag, 6);
        boolean is2x2Matrix               = testFlag(flag, 7);
        boolean hasInstructions           = testFlag(flag, 8);
        boolean useThisComponentMetrics   = testFlag(flag, 9);
        boolean componentsOverlap         = testFlag(flag, 10);

        // Read args (these are either x/y offsets, or point number)
        int arg1;
        int arg2;
        if(argsAre2Bytes)
        {
            arg1 = buffer.getShort();
            arg2 = buffer.getShort();
        }
        else
        {
            arg1 = (int) buffer.get();
            arg2 = (int) buffer.get();
        }

        if (!argsAreXYValues)
        {
            throw new Exception("TODO: Args1&2 are point indices to be matched, rather than offsets");
        }

        double offsetX = arg1;
        double offsetY = arg2;

        double iHat_x = 1;
        double iHat_y = 0;
        double jHat_x = 0;
        double jHat_y = 1;

        if (isSingleScaleValue)
        {
            iHat_x = ReadFixedPoint2Dot14(buffer);
            jHat_y = iHat_x;
        }
        else if (isXAndYScale)
        {
            iHat_x = ReadFixedPoint2Dot14(buffer);
            jHat_y = ReadFixedPoint2Dot14(buffer);
        }
        // Todo: incomplete implemntation
        else if (is2x2Matrix)
        {
            iHat_x = ReadFixedPoint2Dot14(buffer);
            iHat_y = ReadFixedPoint2Dot14(buffer);
            jHat_x = ReadFixedPoint2Dot14(buffer);
            jHat_y = ReadFixedPoint2Dot14(buffer);
        }

        int currentCompoundGlyphReadLocation = buffer.position();
        GlyphData simpleGlyph = readGlyph(buffer, glyphLocations, glyphIndex);
        buffer.position(currentCompoundGlyphReadLocation);

        for (int i = 0; i < simpleGlyph.points.length; i++)
        {
            int x = simpleGlyph.points[i].X;
            int y = simpleGlyph.points[i].Y;
            simpleGlyph.points[i].X = (int) (iHat_x * x + jHat_x * y + offsetX);
            simpleGlyph.points[i].Y = (int) (iHat_y * x + jHat_y * y + offsetY);
        }

        dest.unicodeValue      = simpleGlyph.unicodeValue;
        dest.glyphIndex        = simpleGlyph.glyphIndex;
        dest.points            = simpleGlyph.points;
        dest.contourEndIndices = simpleGlyph.contourEndIndices;
        dest.advanceWidth      = simpleGlyph.advanceWidth;
        dest.leftSideBearing   = simpleGlyph.leftSideBearing;

        dest.minX = simpleGlyph.minX;
        dest.maxX = simpleGlyph.maxX;
        dest.minY = simpleGlyph.minY;
        dest.maxY = simpleGlyph.maxY;

        return isMoreComponentsAfterThis;
    }

    public static void skip(ByteBuffer b, int bytesCount)
    {
        b.position(b.position() + bytesCount);
    }

    private static String readString(ByteBuffer b, int length)
    {
        byte[] data = new byte[length];
        b.get(data, 0, length);
        return new String(data, 0, length);
    }

    public static double ReadFixedPoint2Dot14(ByteBuffer b)
    {
        return b.getShort() / (double) (1 << 14);
    }

    private static boolean testFlag(byte data, int index)
    {
        return ((data >> index) & 1) == 1;
    }

    private static boolean testFlag(int data, int index)
    {
        return ((data >> index) & 1) == 1;
    }

    public static class GlyphData
    {
        public int     unicodeValue;
        public int     glyphIndex;
        public Point[] points;
        public int[]   contourEndIndices;
        public int     advanceWidth;
        public int     leftSideBearing;

        public int     minX;
        public int     maxX;
        public int     minY;
        public int     maxY;

        public int width()  { return maxX - minX; }
        public int height() { return maxY - minY; }
    }

    public class GlyphMap
    {
        public final int glyphIndex;
        public final int unicode;

        public GlyphMap(int index, int unicode)
        {
            glyphIndex = index;
            this.unicode = unicode;
        }
    }

    public static class Point
    {
        public int X;
        public int Y;
        public boolean OnCurve;

        public Point(int x, int y)
        {
            X = x;
            Y = y;
        }

        public Point(int x, int y, boolean onCurve)
        {
            X = x;
            Y = y;
            OnCurve = onCurve;
        }
    }


    public static void main(String[] args)
    {
        try
        {
            File f = new File("./res/fonts/JetBrainsMono-Bold.ttf");
            System.out.println(f.getAbsolutePath());
            TrueTypeFont.readFile(f);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}