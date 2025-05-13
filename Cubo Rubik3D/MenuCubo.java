package com.example.mostraMosse;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;

import com.example.scanner.Escaner;
import com.example.scanner.R;
import com.threeDBJ.MGraphicsLib.GLColor;
import com.threeDBJ.MGraphicsLib.GLEnvironment;
import com.threeDBJ.MGraphicsLib.math.Vec2;
import com.threeDBJ.MGraphicsLib.texture.TextureButton;
import com.threeDBJ.MGraphicsLib.texture.TextureView.TextureClickListener;
import com.threeDBJ.MGraphicsLib.texture.TextureFont;
import com.threeDBJ.MGraphicsLib.texture.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import javax.microedition.khronos.opengles.GL11;

import timber.log.Timber;

public class MenuCubo extends GLEnvironment {

    private static final int NONE = 0, SINGLE_TOUCH = 1, MULTI_TOUCH = 2;

    private float MENU_HEIGHT, MENU_WIDTH;

    private RubeCube cube;

    private TextureFont font;

    private TextureView menuView;

    private TextureButton prevMove;
    private TextureButton nextMove;

    private char[] configurazione;
    private ArrayList<String> listaMosse;
    private ListIterator<String> iteratorMosse;

    private float xMin, xMax, yMin, yMax;
    private float x1, y1;
    private int activePtrId = -1, touchMode = NONE;

    public MenuCubo(RubeCube cube, TextureFont font) {
        this.cube = cube;
        this.font = font;
        this.configurazione = cube.getConfigurazione();
        setListaMosse(configurazione);

        menuView = new TextureView();

        nextMove = new TextureButton(this.font);
        prevMove = new TextureButton(this.font);

        generate();
        enableTextures();
    }

    public void setListaMosse(char[] configurazione) {
        char[] tmp = configurazione.clone();
        for (int i = 0; i < tmp.length; i++) {
            switch (tmp[i]) {
                case 'w':
                    tmp[i] = 'U';
                    break;
                case 'r':
                    tmp[i] = 'R';
                    break;
                case 'g':
                    tmp[i] = 'F';
                    break;
                case 'y':
                    tmp[i] = 'D';
                    break;
                case 'o':
                    tmp[i] = 'L';
                    break;
                case 'b':
                    tmp[i] = 'B';
                    break;
            }
        }

        long xtime = System.nanoTime();
        String solution = Escaner.solve(MostraMosseActivity.configurazione, MostraMosseActivity.coloriFacce);
        solution = solution.replaceAll("  ", " ");
        solution = RubeCube.replaceDoubleMove(solution);
        System.out.println("|||||| TEMPO calcolo soluzione: " + (System.nanoTime() - xtime));
        System.out.println("|||||| soluzione: " + solution);

        this.listaMosse = new ArrayList<>(Arrays.asList(solution.split(" ")));
        this.iteratorMosse = listaMosse.listIterator();
    }

    public void setBounds(float xMin, float xMax, float yMin, float yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        Timber.d("Cube %f %f %f %f", xMin, xMax, yMin, yMax);
        float z = 1f;
        float rat = (xMax - xMin) / (yMax - yMin);
        float fRat = (rat + 1f) / 2f;

        float w, h;
        float l = xMin + 0.05f;
        float r = xMax - 0.05f;
        float b = yMin + 0.05f;
        float t = b + 0.6f + (0.4f * rat);
        GLColor white = new GLColor(1, 1, 1);

        float xPadding = (r - l) / 16f;
        float yPadding = (t - b) / 10f;

        MENU_HEIGHT = (t - b);
        MENU_WIDTH = (r - l);

        menuView.setFace(l, r, b, t, z, white);
        menuView.setTextureBounds(1f, 1f);

        Vec2 tSize;
        GLColor textColor = new GLColor(0.5f, 0.5f, 0.5f);
        h = ((t - b) - (2 * yPadding));

        w = (MENU_WIDTH * 0.5f) - (2 * xPadding);

        prevMove.setFace(l + xPadding, l + xPadding + w, b + yPadding, t - yPadding, z + 0.02f, white);
        prevMove.setTextureBounds(1f, 1f);
        prevMove.setTextColor(textColor);
        prevMove.setTextSize(12f * fRat);
        tSize = font.measureText("Prev. Move", prevMove.textSize);
        prevMove.setPadding(w / 2f - ((tSize.x + 0.02f) / 2f), 0, 0, h / 2f - tSize.y / 2f);

        nextMove.setFace(l + w + (3 * xPadding), l + (3 * xPadding) + (2 * w), b + yPadding, t - yPadding, z + 0.02f, white);
        nextMove.setTextureBounds(1f, 1f);
        nextMove.setTextColor(textColor);
        nextMove.setTextSize(12f * fRat);
        tSize = font.measureText("Next Move", nextMove.textSize);
        nextMove.setPadding(w / 2f - ((tSize.x + 0.02f) / 2f), 0, 0, h / 2f - tSize.y / 2f);

        menuView.addChild(prevMove);
        menuView.addChild(nextMove);

        generate();

        prevMove.setText("Prev. Move");
        nextMove.setText("Next Move");
    }

    public Vec2 screenToWorld(float x, float y) {
        Vec2 p = new Vec2(x * adjustWidth, 1f - y * adjustHeight);
        p.x = p.x * (xMax - xMin) + xMin;
        p.y = p.y * (yMax - yMin) + yMin;
        return p;
    }

    private void resetTouch() {
        touchMode = NONE;
        activePtrId = -1;
    }

    public boolean handleTouch(MotionEvent e) {
        Vec2 worldCoords;

        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touchMode = SINGLE_TOUCH;
                activePtrId = e.getPointerId(0);
                x1 = e.getX();
                y1 = e.getY();
                worldCoords = screenToWorld(x1, y1);
                return menuView.handleActionDown(worldCoords);
            case MotionEvent.ACTION_UP:
                resetTouch();
                worldCoords = screenToWorld(x1, y1);
                return menuView.handleActionUp(worldCoords);
            case MotionEvent.ACTION_CANCEL:
                resetTouch();
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchMode == SINGLE_TOUCH) {
                    final int ptrInd = e.findPointerIndex(activePtrId);
                    float x = e.getX(ptrInd);
                    float y = e.getY(ptrInd);
                    if (touchMode == SINGLE_TOUCH) {
                        worldCoords = screenToWorld(x, y);
                        return menuView.handleActionMove(worldCoords);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                touchMode = MULTI_TOUCH;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (e.getPointerCount() == 1) {
                    touchMode = SINGLE_TOUCH;
                }
                break;
        }
        return false;
    }

    public void save(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        int indexMossa;
        if (iteratorMosse.hasNext()) {
            indexMossa = iteratorMosse.nextIndex();
        } else {
            indexMossa = iteratorMosse.previousIndex();
            indexMossa++;
        }
        edit.putInt("indexMossa", indexMossa);
        System.out.println("saving indexMossa: " + indexMossa);
        edit.commit();
    }

    public void restore(SharedPreferences prefs) {
        int indexMossa = prefs.getInt("indexMossa", -1);
        if (indexMossa == -1) {
            Timber.d("ERRORE: l'iteratore della lista-mosse non è stato ripristinato correttamente");
        } else {
            System.out.println("restoring indexMossa: " + indexMossa);
            for (int i = 0; i < indexMossa; i++)
                iteratorMosse.next();
        }
    }
}
