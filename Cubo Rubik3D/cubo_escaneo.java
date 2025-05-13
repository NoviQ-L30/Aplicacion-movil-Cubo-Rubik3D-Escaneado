package com.example.scanner;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class cubo_escaneo {
    private Scalar colorRgb; // Color medio dentro del cuadrado
    private Rect rect; // Coordenadas del cuadrado a dibujar en la pantalla
    private Point center; // Coordenadas del centro del cuadrado
    private int size; // Tamaño del cuadrado (igual para todos)
    private String prec = "n"; // Carácter en el que se guarda el color anterior (para estabilizar la lectura)

    public cubo_escaneo(Point center, int size) {
        colorRgb = new Scalar(255, 0, 0);
        this.size = size;
        this.center = center;

        rect = new Rect();
        rect.x = (int) (center.x - size / 2);
        rect.y = (int) (center.y - size / 2);
        rect.width = size;
        rect.height = size;
    }

    public Scalar getColorRgb() {
        return colorRgb;
    }

    public void setColorRgb(Scalar colorHsv) {
        this.colorRgb = colorHsv;
    }

    public Rect getRect() {
        return rect;
    }

    public Point getTopLeftPoint() {
        return new Point(rect.x, rect.y);
    }

    public Point getBottomRightPoint() {
        return new Point(rect.x + rect.width, rect.y + rect.height);
    }

    public Point getCenter() {
        return center;
    }

    public int getSize() {
        return size;
    }

    // Método que devuelve el nombre del color contenido en el cuadrado
    public String getColor() {
        String tempString; // String en el que guardo el nombre del color

        // ASIGNO UN COLOR BASADO EN EL RANGO RGB (hecho un poco al azar siguiendo https://www.rapidtables.com/web/color/RGB_Color.html)
        if (colorRgb.val[0] >= 100 && colorRgb.val[1] < 100 && colorRgb.val[2] < 100) {
            tempString = "R";
        } else if (colorRgb.val[0] < 100 && colorRgb.val[1] >= 100 && colorRgb.val[2] < 100) {
            tempString = "G";
        } else if (colorRgb.val[0] < 100 && colorRgb.val[1] < 100 && colorRgb.val[2] >= 50) {
            tempString = "B";
        } else if (colorRgb.val[0] >= 150 && colorRgb.val[1] >= 170 && colorRgb.val[2] < 100) {
            tempString = "Y";
        } else if (colorRgb.val[0] >= 150 && colorRgb.val[1] >= 80 && colorRgb.val[1] <= 200 && colorRgb.val[2] < 100) {
            // A veces da problemas (rojo, amarillo y naranja se confunden según la luz)
            tempString = "O";
        } else if (colorRgb.val[0] > 150 && colorRgb.val[1] > 150 && colorRgb.val[2] > 150) {
            tempString = "W";
        } else {
            tempString = prec; // Si no puedo leer el color, probablemente se deba a la luz, para estabilizar los resultados, pongo el último color leído;
        }

        prec = tempString; // Guardo el color leído en prec

        return tempString;
    }
}
