package com.example.mostraMosse;

import com.threeDBJ.MGraphicsLib.GLFace;
import com.threeDBJ.MGraphicsLib.GLShape;
import com.threeDBJ.MGraphicsLib.GLVertex;
import com.threeDBJ.MGraphicsLib.math.Vec3;

public class Cube extends GLShape {
    public static final int kBottom = 0;
    public static final int kFront = 1;
    public static final int kLeft = 2;
    public static final int kRight = 3;
    public static final int kBack = 4;
    public static final int kTop = 5;

    public int id;
    public Vec3 normal;

    public Cube(GLWorld world, float left, float bottom,
                float back, float right, float top, float front) {
        super(world);

        // Define los vértices del cubo
        GLVertex lbBack = new GLVertex(left, bottom, back);
        GLVertex rbBack = new GLVertex(right, bottom, back);
        GLVertex ltBack = new GLVertex(left, top, back);
        GLVertex rtBack = new GLVertex(right, top, back);
        GLVertex lbFront = new GLVertex(left, bottom, front);
        GLVertex rbFront = new GLVertex(right, bottom, front);
        GLVertex ltFront = new GLVertex(left, top, front);
        GLVertex rtFront = new GLVertex(right, top, front);

        // Crea las caras del cubo usando los vértices
        addCubeSide(rbBack, rbFront, lbBack, lbFront);  // Bottom
        addCubeSide(rbFront, rtFront, lbFront, ltFront); // Front
        addCubeSide(lbFront, ltFront, lbBack, ltBack);   // Left
        addCubeSide(rbBack, rtBack, rbFront, rtFront);   // Right
        addCubeSide(lbBack, ltBack, rbBack, rtBack);     // Back
        addCubeSide(rtFront, rtBack, ltFront, ltBack);   // Top

        // Asigna la textura a cada cara del cubo
        for (GLFace f : getFaceList()) {
            f.setTexture(getEnv().texture);
        }
    }

    private void addCubeSide(GLVertex rb, GLVertex rt, GLVertex lb, GLVertex lt) {
        // Agrega una cara al cubo con los vértices proporcionados
        addFace(new GLFace(addVertex(rb), addVertex(rt), addVertex(lb), addVertex(lt)));
    }
}
