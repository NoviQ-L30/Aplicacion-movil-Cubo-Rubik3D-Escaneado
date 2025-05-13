package com.example.scanner;

import cs.min2phase.Search;
import static org.opencv.imgproc.Imgproc.line;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.mostraMosse.MostraMosseActivity;

import java.util.ArrayList;

import static org.opencv.core.Core.mean;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.cornerHarris;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;


public class Escaner extends Activity implements CvCameraViewListener2 {

    private JavaCameraView camera;
    private BaseLoaderCallback baseLoaderCallback;
    private Mat mRgba;
    private Point textDrawPoint, arrowDrawPoint, textFaceIndex;
    private Scalar colorText = new Scalar(0,0,0,255);
    private Scalar colorTextBorder = new Scalar(255,255,255,255);
    private int font=FONT_HERSHEY_SIMPLEX;

    private ImageButton saveFaceButton;
    private ImageButton instructionsButton;
    private Intent instructionsIntent;
    private Intent cubeIntent;

    private int thicknessRect=13, sizeRect=125;
    private ArrayList<cubo_escaneo> cuboescaneos;
    private int squareLayoutDistance = 200;

    private Point[] squareLocations = {
            new Point(-1,-1),new Point(0,-1),new Point(1,-1),
            new Point(-1,0),new Point(0,0),new Point(1,0),
            new Point(-1,1),new Point(0,1),new Point(1,1)};
    private String[] faces;
    private int index=-1;
    private char[] sides =new char[6];
    private long timeOffset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        saveFaceButton =findViewById(R.id.saveFaceButton);
        instructionsButton=findViewById(R.id.instructions_button);
        faces = new String[6];
        cubeIntent = new Intent(this, MostraMosseActivity.class);

        if(permission()) {
            camera = findViewById(R.id.javaCameraView);
            camera.setVisibility(SurfaceView.VISIBLE);
            camera.setCameraPermissionGranted();
            camera.setCvCameraViewListener(this);

            baseLoaderCallback = new BaseLoaderCallback(this) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS: {
                            Log.d("OPENCV","OPENCV loaded successfully");
                            camera.enableView();
                        }
                        break;
                        default: {
                            super.onManagerConnected(status);
                            Log.d("OPENCV","OPENCV not loaded");
                        }
                        break;
                    }
                }
            };
        }

        if (!Search.isInited())
            Search.init();

        saveFaceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFace();
            }
        });

        instructionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(instructionsIntent);
            }
        });
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRgba=inputFrame.rgba();

        if(cuboescaneos ==null) {
            Point tempCenter = new Point(mRgba.width() / 2, mRgba.height() / 2);
            cuboescaneos =new ArrayList<>();

            for(Point squareLocation: squareLocations){
                cuboescaneos.add(new cubo_escaneo(new Point(squareLocation.x*squareLayoutDistance+tempCenter.x,squareLocation.y*squareLayoutDistance+tempCenter.y),sizeRect));
            }
        }
        if(System.currentTimeMillis()-timeOffset>=200)
            processColor();
        drawSquares();
        return mRgba;
    }


    private void processColor() {
        Scalar tmpColor;
        for(cubo_escaneo s : cuboescaneos) {
            Mat rectRgba = mRgba.submat(s.getRect());
            tmpColor = mean(rectRgba);
            s.setColorRgb(tmpColor);
        }
        timeOffset=System.currentTimeMillis();

    }

    private void drawSquares() {
        for (int i = 0; i < cuboescaneos.size(); i++) {
            cubo_escaneo s = cuboescaneos.get(i);
            Scalar showColor = charToRGB(s.getColor());

            // Dibujar un signo de "+" en lugar de un cuadrado
            Point center = new Point((s.getTopLeftPoint().x + s.getBottomRightPoint().x) / 2,
                    (s.getTopLeftPoint().y + s.getBottomRightPoint().y) / 2);
            int length = thicknessRect * 3; // Longitud de los brazos del signo de "+"

            // Dibujar líneas horizontales y verticales
            line(mRgba, new Point(center.x - length, center.y), new Point(center.x + length, center.y), showColor, thicknessRect);
            line(mRgba, new Point(center.x, center.y - length), new Point(center.x, center.y + length), showColor, thicknessRect);
        }


    textFaceIndex = new Point(80, 150);
        putText(mRgba, "Cara Numero "+(index+2), textFaceIndex, font, 4,colorTextBorder, 11 );

        arrowDrawPoint = new Point(80, mRgba.height() - 300);
        if(index==-1){
            putText(mRgba, "Escanea de izq a der", arrowDrawPoint, font, 2, colorTextBorder, 10);
            putText(mRgba, "", arrowDrawPoint, font, 2, colorText, 5);
        }
        if (index<3 && index>=0) {
            putText(mRgba, "", arrowDrawPoint, font, 2, colorTextBorder, 10);
            putText(mRgba, "", arrowDrawPoint, font, 2, colorText, 5);
        }
        else if(index==3) {
            putText(mRgba, "", arrowDrawPoint, font, 2, colorTextBorder, 10);
            putText(mRgba, "", arrowDrawPoint, font, 2, colorText, 5);
        }
        else if(index==4) {
            putText(mRgba, "", arrowDrawPoint, font, 2, colorTextBorder, 10);
            putText(mRgba, "", arrowDrawPoint, font, 2, colorText, 5);
        }
        else {

        }

        if(index>=0)
            drawLastFace();
    }

    void drawLastFace(){
        Point tmpPoint;
        int layoutDistance=55;
        int width=50;
        Point tempCenter = new Point(mRgba.width() -200, 200);

        for(int i=0; i<9; i++) {
            tmpPoint = new Point(squareLocations[i].x * layoutDistance + tempCenter.x, squareLocations[i].y * layoutDistance + tempCenter.y);
            Scalar showColor=charToRGB(faces[index].substring(i,i+1));
            rectangle(mRgba,new Rect((int)tmpPoint.x, (int)tmpPoint.y, width, width), showColor, -1);
        }
    }

    public void saveFace(){
        String tempString = "";
        for (int i = 0; i < cuboescaneos.size(); i++) {
            cubo_escaneo s = cuboescaneos.get(i);
            tempString += s.getColor();
        }



        if( tempString.contains("n")) {
            return;
        }
        index++;
        sides[index]=tempString.charAt(4);


        faces[index] = tempString;
        Log.i("faccia", faces[index].toString());


        if(index==5) {
            String sol = solve(faces, sides);
            Log.d("faccia", sol);

            if(sol.length()==0) {
                sol = "Cube already solved!";
                new AlertDialog.Builder(this)
                        .setTitle("ERROR")
                        .setMessage(sol+"\nScramble the cube and scan again.")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                index=-1;
            }
            else if (sol.contains("Error")) {
                switch (sol.charAt(sol.length() - 1)) {
                    case '1':
                        sol = "There are not exactly nine squares of each color!";
                        break;
                    case '2':
                        sol = "Not all 12 edges exist exactly once!";
                        break;
                    case '3':
                        sol = "Flip error: One edge has to be flipped!";
                        break;
                    case '4':
                        sol = "Not all 8 corners exist exactly once!";
                        break;
                    case '5':
                        sol = "Twist error: One corner has to be twisted!";
                        break;
                    case '6':
                        sol = "Parity error: Two corners or two edges have to be exchanged!";
                        break;
                    case '7':
                        sol = "No solution exists for the given maximum move number!";
                        break;
                    case '8':
                        sol = "Timeout, no solution found within given maximum time!";
                        break;
                }
                new AlertDialog.Builder(this)
                        .setTitle("ERROR")
                        .setMessage(sol+"\nBe sure to be in proper light conditions.\nScan again to continue.")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                index=-1;
            }

            else {
                cubeIntent.putExtra("configurazione", faces);
                cubeIntent.putExtra("colori facce", sides);
                startActivity(cubeIntent);
                index=-1;
            }
        }

    }



    public static String solve(String[] faces, char [] sides){
        Search search = new Search();






        String faceSwap[]=new String[6];
        faceSwap[0]=faces[4];
        faceSwap[1]=faces[1];
        faceSwap[2]=faces[0];
        faceSwap[3]=faces[5];
        faceSwap[4]=faces[3];
        faceSwap[5]=faces[2];
        StringBuffer tempString = new StringBuffer(54);
        String result;
        for (int i = 0; i < 54; i++)
            tempString.insert(i, 'B');


        for(int i=0; i<faces.length; i++){
            for(int j=0; j<9; j++){
                if(faceSwap[i].charAt(j)==sides[0])
                    tempString.setCharAt(9 * i + j, 'F'); //Front
                if(faceSwap[i].charAt(j)==sides[1])
                    tempString.setCharAt(9 * i + j, 'R'); //Right
                if(faceSwap[i].charAt(j)==sides[2])
                    tempString.setCharAt(9 * i + j, 'B'); //Back
                if(faceSwap[i].charAt(j)==sides[3])
                    tempString.setCharAt(9 * i + j, 'L'); //Left
                if(faceSwap[i].charAt(j)==sides[4])
                    tempString.setCharAt(9 * i + j, 'U'); //Up
                if(faceSwap[i].charAt(j)==sides[5])
                    tempString.setCharAt(9 * i + j, 'D'); //Down
            }
        }
        String cubeString=tempString.toString();

        Log.d("faccia", cubeString);

        result= search.solution(cubeString, 24, 100,0, 0);
        Log.d("faccia","RESULT: "+result);

        return result;
    }


    Scalar charToRGB(String color){
        Scalar showColor;
        switch (color){
            case "R":
                showColor=new Scalar(255,0,0);
                break;
            case "G":
                showColor=new Scalar(0,255,0);
                break;
            case "B":
                showColor=new Scalar(0,0,255);
                break;
            case "Y":
                showColor=new Scalar(255,255,0);
                break;
            case "O":
                showColor=new Scalar(255,165,0);
                break;
            case "W":
                showColor=new Scalar(255,255,255);
                break;
            default:
                showColor=new Scalar(0,0,0);
                break;
        }
        return showColor;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (camera != null)
            camera.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (camera != null)
            camera.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (camera != null) {
            camera.setRotation(0); // O ajusta el valor según sea necesario
        }
    }

    public void onCameraViewStopped() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            Log.d("OPENCV","OpenCV caricato correttamente");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
        else{
            Log.d("OPENCV","Errore OpenCV non caricato");
        }

    }


    private boolean permission() {
        if (ContextCompat.checkSelfPermission(Escaner.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 50);
        } else {
            return true;
        }
        return false;
    }

}