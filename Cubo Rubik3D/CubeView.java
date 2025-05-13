package com.example.mostraMosse;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.scanner.R;
import com.threeDBJ.MGraphicsLib.texture.TextureFont;

import timber.log.Timber;

public class CubeView extends GLSurfaceView {

    private CubeRenderer renderer;
    TextureFont font;
    RubeCube cube;
    GLWorld world;
    MenuCubo menu;
    char[] configurazione;

    static boolean stopInput = false;

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (world != null) {
            world.setDimensions(w, h);
            menu.setDimensions(w, h);
            //renderer.worldBoundsSet = false;
        }
        Timber.d("onSizeChanged %d %d", w, h);
    }

    public CubeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CubeView(Context context, char[] configurazione) {
        this(context, null, configurazione);
    }

    public CubeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        font = new TextureFont(getContext(), R.drawable.roboto_regular, "roboto_regular_dims.txt");
        world = new GLWorld();
    }

    public CubeView(Context context, AttributeSet attrs, char[] configurazione) {
        super(context, attrs);
        font = new TextureFont(getContext(), R.drawable.roboto_regular, "roboto_regular_dims.txt");
        world = new GLWorld();
        this.configurazione = configurazione;
    }

    public void initialize(SharedPreferences prefs) {
        cube = new RubeCube(world, 3, configurazione);
        menu = new MenuCubo(cube, font);
        renderer = new CubeRenderer(getContext(), font, world, cube, menu, prefs);
        cube.setRenderer(renderer);
        world.setRubeCube(cube);
        setRenderer(renderer);
    }

    public void save(SharedPreferences prefs) {
        cube.save(prefs);
        menu.save(prefs);
    }

    public void restore(SharedPreferences prefs) {
        //menu.restore(prefs);
    }

    public boolean onTouchEvent(final MotionEvent e) {
        if (CubeView.stopInput) // Si hay una animación en curso, no gestionar las entradas
            return false;

        if (!menu.handleTouch(e)) {
            cube.handleTouch(e);
        }
        return true;
    }
}
