package ru.sibsutis.planets;

import java.nio.*;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Planet {
    FloatBuffer m_VertexData;
    FloatBuffer m_NormalData;
    FloatBuffer m_ColorData;
    FloatBuffer m_TextureData;

    float m_Scale;
    float m_Squash;
    float m_Radius;
    int m_Stacks, m_Slices;
    public float[] m_Pos = {0.0f, 0.0f, 0.0f};

    public Planet(int stacks, int slices, float radius, float squash, GL10 gl, Context context, boolean imageId, int resourceId) {
        this.m_Stacks = stacks;
        this.m_Slices = slices;
        this.m_Radius = radius;
        this.m_Squash = squash;
        init(m_Stacks, m_Slices, radius, squash, gl, context, imageId, resourceId);
    }

    private void init(int stacks, int slices, float radius, float squash, GL10 gl, Context context, boolean imageId, int resourceId) {
        float[] vertexData;
        float[] normalData;
        float[] colorData;
        float[] textData = null;

        float colorIncrement;

        float blue = 0f;
        float red = 1.0f;

        int vIndex = 0;                //vertex index
        int cIndex = 0;                //color index
        int nIndex = 0;                //normal index
        int tIndex = 0;                //texture index

        if (imageId) {
            createTexture(gl, context, resourceId);
        }

        m_Scale = radius;
        m_Squash = squash;

        colorIncrement = 1.0f / (float) stacks;

        m_Stacks = stacks;
        m_Slices = slices;

        //Vertices
        vertexData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];

        //Color data
        colorData = new float[(4 * (m_Slices * 2 + 2) * m_Stacks)];

        // Нормальные указатели для освещения
        normalData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];

        if (imageId)
            textData = new float[2 * ((m_Slices * 2 + 2) * (m_Stacks))]; // массив координат для текстуры

        int phiIdx, thetaIdx;

        // Широта

        for (phiIdx = 0; phiIdx < m_Stacks; phiIdx++) {
            // Начинается с -1,57 и увеличивается до +1,57 радиан.
            // Первый круг.
            float phi0 = (float) Math.PI * ((float) (phiIdx) * (1.0f / (float) (m_Stacks)) - 0.5f);

            // Следующий или второй.
            float phi1 = (float) Math.PI * ((float) (phiIdx + 1) * (1.0f / (float) (m_Stacks)) - 0.5f);

            float cosPhi0 = (float) Math.cos(phi0);
            float sinPhi0 = (float) Math.sin(phi0);
            float cosPhi1 = (float) Math.cos(phi1);
            float sinPhi1 = (float) Math.sin(phi1);

            float cosTheta, sinTheta;

            // Долгота
            for (thetaIdx = 0; thetaIdx < m_Slices; thetaIdx++) {
                // Увеличение по кругу долготы каждый куска.
                float theta = (float) (2.0f * (float) Math.PI * ((float) thetaIdx) * (1.0 / (float) (m_Slices - 1)));
                cosTheta = (float) Math.cos(theta);
                sinTheta = (float) Math.sin(theta);

                // Получение x-y-z для первой вершины стека.
                vertexData[vIndex] = m_Scale * cosPhi0 * cosTheta;
                vertexData[vIndex + 1] = m_Scale * (sinPhi0 * m_Squash);
                vertexData[vIndex + 2] = m_Scale * (cosPhi0 * sinTheta);

                vertexData[vIndex + 3] = m_Scale * cosPhi1 * cosTheta;
                vertexData[vIndex + 4] = m_Scale * (sinPhi1 * m_Squash);
                vertexData[vIndex + 5] = m_Scale * (cosPhi1 * sinTheta);

                // Нормальные указатели для освещения

                normalData[nIndex] = cosPhi0 * cosTheta;
                normalData[nIndex + 2] = cosPhi0 * sinTheta;
                normalData[nIndex + 1] = sinPhi0;

                // Получение x-y-z для первой вершины стека N.

                normalData[nIndex + 3] = cosPhi1 * cosTheta;
                normalData[nIndex + 5] = cosPhi1 * sinTheta;
                normalData[nIndex + 4] = sinPhi1;

                if (textData != null) {   // вычисление координат текстуры
                    float texX = (float) thetaIdx * (1.0f / (float) (m_Slices - 1));
                    textData[tIndex] = texX;
                    textData[tIndex + 1] = (float) (phiIdx) * (1.0f / (float) (m_Stacks));
                    textData[tIndex + 2] = texX;
                    textData[tIndex + 3] = (float) (phiIdx + 1) * (1.0f / (float) (m_Stacks));
                }

                colorData[cIndex] = red;
                colorData[cIndex + 1] = 0f;
                colorData[cIndex + 2] = blue;
                colorData[cIndex + 4] = red;
                colorData[cIndex + 5] = 0f;
                colorData[cIndex + 6] = blue;
                colorData[cIndex + 3] = (float) 1.0;
                colorData[cIndex + 7] = (float) 1.0;

                cIndex += 2 * 4;
                vIndex += 2 * 3;
                nIndex += 2 * 3;

                if (textData != null)   // продвижение массива координат для хранения след. набора значений
                    tIndex += 2 * 2;

                blue += colorIncrement;
                red -= colorIncrement;

                // Вырожденный треугольник для соединения стеков и поддержания порядка намотки.
                vertexData[vIndex] = vertexData[vIndex + 3] = vertexData[vIndex - 3];
                vertexData[vIndex + 1] = vertexData[vIndex + 4] = vertexData[vIndex - 2];
                vertexData[vIndex + 2] = vertexData[vIndex + 5] = vertexData[vIndex - 1];

                normalData[nIndex] = normalData[nIndex + 3] = normalData[nIndex - 3];
                normalData[nIndex + 1] = normalData[nIndex + 4] = normalData[nIndex - 2];
                normalData[nIndex + 2] = normalData[nIndex + 5] = normalData[nIndex - 1];

                if (textData != null) {  // связь некоторых нитий, чтобы подготовиться к след. стеку
                    textData[tIndex] = textData[tIndex + 2] = textData[tIndex - 2];
                    textData[tIndex + 1] = textData[tIndex + 3] = textData[tIndex - 1];
                }
            }
        }

        m_Pos[0] = 0.0f;
        m_Pos[1] = 0.0f;
        m_Pos[2] = 0.0f;

        m_VertexData = makeFloatBuffer(vertexData);
        m_NormalData = makeFloatBuffer(normalData);
        m_ColorData = makeFloatBuffer(colorData);

        if (textData != null)
            m_TextureData = makeFloatBuffer(textData);
    }

    protected static FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public void draw(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        if (m_TextureData != null) {
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, m_TextureData);
        }

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, m_VertexData);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, m_NormalData);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, m_ColorData);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, (m_Slices + 1) * 2 * (m_Stacks - 1) + 2);


        int[] pointSize = new int[2];
        gl.glGetIntegerv(GL10.GL_SMOOTH_LINE_WIDTH_RANGE, makeIntBuffer(pointSize));
        gl.glGetError();
        gl.glGetIntegerv(GL10.GL_ALIASED_LINE_WIDTH_RANGE, makeIntBuffer(pointSize));
        gl.glGetError();


        gl.glDisable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }


    protected static IntBuffer makeIntBuffer(int[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer fb = bb.asIntBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    public void setPosition(float x, float y, float z) {
        m_Pos[0] = x;
        m_Pos[1] = y;
        m_Pos[2] = z;
    }

    private int[] textures = new int[1];

    public void createTexture(GL10 gl, Context contextRegf, int resource) {
        Bitmap tempImage = BitmapFactory.decodeResource(contextRegf.getResources(), resource);

        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, tempImage, 0);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        tempImage.recycle();

    }
}