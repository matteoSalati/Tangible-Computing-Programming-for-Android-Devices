package sallo.tcpapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static org.opencv.imgproc.Imgproc.resize;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TCPAppTag";
    private static final int SELECT_PICTURE = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private String selectedImagePath, solution = "[";
    Mat sampledImage = null;
    Mat circleImage = null;
    Mat histImage = null;
    Mat circles = null;
    Bitmap bitMapGallery;
    boolean firstImage = true; //Resetta grafica
    boolean scrivi = false;
    boolean errWhile = false;
    boolean errStopExcess = false;
    int N = 11;
    boolean[] errIf = new boolean[N];
    boolean[] errIfCond = new boolean[N];
    boolean[] errElse = new boolean[N];
    int number, nLevel = 1;
    double radiusMIN;
    double radiusMAX;

    private Toolbar mToolbar;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton actionFloat, galleryFloat, circleFloat, colorFloat, confirmFloat;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private TextView galleryText, circleText, colorText, confirmText, textCommand;
    private Boolean isFabOpen = false;
    private ImageView galleryImageView;//, hystoImageView;
    private Button npLevel;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV caricato correttamente");
                    break;

                default:
                    super.onManagerConnected(status);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 0; i < N; i++){
            errIf[i] = false;
            errIfCond[i] = false;
            errElse[i] = false;

        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMain);
        actionFloat = (FloatingActionButton) findViewById(R.id.action_fab);
        galleryFloat  = (FloatingActionButton) findViewById(R.id.gallery_fab);
        circleFloat  = (FloatingActionButton) findViewById(R.id.circle_fab);
        colorFloat  = (FloatingActionButton) findViewById(R.id.color_fab);
        confirmFloat  = (FloatingActionButton) findViewById(R.id.confirm_fab);
        npLevel = (Button) findViewById(R.id.nLevel);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0); // clear all scroll flags

        galleryText  = (TextView) findViewById(R.id.gallery_text);
        circleText  = (TextView) findViewById(R.id.circle_text);
        colorText  = (TextView) findViewById(R.id.color_text);
        confirmText  = (TextView) findViewById(R.id.confirm_text);
        textCommand = (TextView) findViewById(R.id.textCommand);

        galleryImageView = (ImageView) findViewById(R.id.gallery_imageview);
        //hystoImageView = (ImageView) findViewById(R.id.hysto_imageview);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        actionFloat.setOnClickListener(this);
        galleryFloat.setOnClickListener(this);
        circleFloat.setOnClickListener(this);
        colorFloat.setOnClickListener(this);
        confirmFloat.setOnClickListener(this);

        npLevel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Snackbar snackbar;
        switch(id) {
            case R.id.action_fab:
                animateFAB();
                break;

            case R.id.gallery_fab:
                animateFAB();
                //snackbar = Snackbar.make(coordinatorLayout, "Apro la galleria", Snackbar.LENGTH_LONG);
                //snackbar.show();
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle(getResources().getString(R.string.alertPermission));
                        alertBuilder.setMessage(getResources().getString(R.string.messagePermission));
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                            }
                        });
                        AlertDialog alert = alertBuilder.create();
                        alert.show();

                    } else {
                        ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                    }
                } else {
                    circleImage = null;
                    histImage = null;
                    openGallery();

                }
                break;

            case R.id.circle_fab:
                animateFAB();
                //snackbar = Snackbar.make(coordinatorLayout, "Trovo i cerchi nella foto", Snackbar.LENGTH_LONG);
                //snackbar.show();
                if(sampledImage == null){
                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.gallery_miss), Snackbar.LENGTH_LONG);
                    snackbar.show();

                } else {
                    circleImage = findCircles();
                    displayImage(circleImage, 1);

                }
                break;

            case R.id.color_fab:
                animateFAB();
                //snackbar = Snackbar.make(coordinatorLayout, "Riconosco i colori della foto", Snackbar.LENGTH_LONG);
                //snackbar.show();
                if(sampledImage == null){
                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.gallery_miss), Snackbar.LENGTH_LONG);
                    snackbar.show();

                } else {
                    if(circleImage == null){
                        snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.circle_miss), Snackbar.LENGTH_LONG);
                        snackbar.show();

                    } else {
                        histImage = new Mat();
                        circleImage.copyTo(histImage);
                        histImage = calcHist(histImage);
                        displayImage(histImage, 2);

                    }
                }
                break;

            case R.id.confirm_fab:
                animateFAB();
                //snackbar = Snackbar.make(coordinatorLayout, "CONFERMA", Snackbar.LENGTH_LONG);
                //snackbar.show();
                if(sampledImage == null){
                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.gallery_miss), Snackbar.LENGTH_LONG);
                    snackbar.show();

                } else {
                    if(circleImage == null){
                        snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.circle_miss), Snackbar.LENGTH_LONG);
                        snackbar.show();

                    } else {
                        if(histImage == null){
                            snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.hist_miss), Snackbar.LENGTH_LONG);
                            snackbar.show();

                        } else {
                            if(!scrivi &!errWhile & !errStopExcess) {
                                int totI = 0;
                                int totC = 0;
                                int totE = 0;
                                for(int i = 0; i < N; i++) {
                                    if(!errIf[i]) {
                                        totI++;

                                    }
                                    if(!errIfCond[i]) {
                                        totC++;

                                    }
                                    if(!errElse[i]) {
                                        totE++;

                                    }
                                }
                                if(totI == N & totC == N & totE == N) {
                                    Intent intent = new Intent(this, BlocklyActivity.class);
                                    //intent.putExtra("simboliJSON", "[10,8,1,4,3,3,9,6,2,9,5]");
                                    intent.putExtra("simboliJSON", solution);
                                    intent.putExtra("level", nLevel);
                                    startActivity(intent);

                                } else {
                                    if(totI != N) {
                                        snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_if), Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                    if(totC != N) {
                                        snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_if_cond), Snackbar.LENGTH_LONG);
                                        snackbar.show();

                                    }
                                    if(totE != N) {
                                        snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_else), Snackbar.LENGTH_LONG);
                                        snackbar.show();

                                    }
                                }
                            } else {
                                if(scrivi){
                                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_scrivi), Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                } else if(errWhile){
                                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_while), Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                } else if(errStopExcess){
                                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.error_stop_excess), Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                }


                            }
                        }
                    }
                }
                break;

            case R.id.nLevel:
                final NumberPicker npLevel = new NumberPicker(this);
                npLevel.setMinValue(1);
                npLevel.setMaxValue(10);
                npLevel.setValue(1);

                AlertDialog.Builder alertLevelBuilder = new AlertDialog.Builder(this);
                alertLevelBuilder.setCancelable(true);
                alertLevelBuilder.setTitle(getResources().getString(R.string.alertLevel));
                alertLevelBuilder.setMessage(getResources().getString(R.string.messageLevel));
                alertLevelBuilder.setView(npLevel);
                alertLevelBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onValueChange(npLevel.getValue());
                        dialog.dismiss();
                    }
                });
                alertLevelBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertLevel = alertLevelBuilder.create();
                alertLevel.show();
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this, mLoaderCallback);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button, so long
        //as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            //Snackbar snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.implement), Snackbar.LENGTH_LONG);
            //snackbar.show();
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                    Log.i(TAG, "Permessi concessi");

                }
                break;

            }
        }
    }

    private void openGallery(){
        //Reset Grafico
        if(!firstImage){
            //hystoImageView.setImageResource(R.mipmap.ic_launcher);
            textCommand.setText(" ");
            solution = "[";

        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.image_select)), SELECT_PICTURE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "Immagine caricata - Path: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(sampledImage, 1);

            }
        }
    }

    private String getPath(Uri uri) {
        if(uri == null) {
            return null;

        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);

        }
        return uri.getPath();

    }

    private double calculateSubSampleSize(Mat srcImage, int reqWidth, int reqHeight) {
        int height = srcImage.height();
        int width = srcImage.width();
        double inSampleSize = 1;
        if(height > reqHeight || width > reqWidth) {
            //Calcoliamo i rapporti tra altezza e larghezza richiesti e quelli dell'immagine sorgente
            double heightRatio = (double) reqHeight / (double) height;
            double widthRatio = (double) reqWidth / (double) width;
            //Scegliamo tra i due rapporti il minore
            inSampleSize = heightRatio<widthRatio ? heightRatio :widthRatio;

        }
        return inSampleSize;

    }

    private void loadImage(String path) {
        Mat originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();
        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        sampledImage = new Mat();
        double downSampleRatio = calculateSubSampleSize(rgbImage,width,height);
        resize(rgbImage, sampledImage, new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);
        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    // ottieni l'immagine specchiata
                    sampledImage = sampledImage.t();
                    // flip lungo l'asse y
                    Core.flip(sampledImage, sampledImage, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    // ottieni l'immagine "sotto-sopra"
                    sampledImage = sampledImage.t();
                    // flip lungo l'asse x
                    Core.flip(sampledImage, sampledImage, 0);
                    break;

            }
        } catch(IOException e) {
            e.printStackTrace();

        }
    }

    private void displayImage(Mat image, int operation) {
        switch(operation) {
            //L'immagine
            case 1:
                bitMapGallery = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);
                // Convertiamo l'immagine di tipo Mat in una Bitmap
                Utils.matToBitmap(image, bitMapGallery);
                galleryImageView.setImageBitmap(bitMapGallery);
                //Log.i(TAG, "Immagine visualizzata correttamente");
                if(firstImage) {
                    firstImage = false;

                }
                break;

            //L'istogramma
            case 2:
                Bitmap bitMapGallery2 = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(image, bitMapGallery2);
                //hystoImageView.setImageBitmap(bitMapGallery2);
                //Log.i(TAG, "Istogramma visualizzato correttamente");
                break;
        }
    }

    //Con GBHT (HT Basato sul Gradiente)
    private Mat findCircles() {
        int mMinThres = 80, mMaxThres = 100;
        number = 0;
        radiusMAX = 0;
        radiusMIN = 10000;
        Mat matGallery = new Mat(bitMapGallery.getHeight(), bitMapGallery.getWidth(), CvType.CV_8UC4);
        Mat matGreyGallery = new Mat(bitMapGallery.getHeight(), bitMapGallery.getWidth(), CvType.CV_8UC1);
        Mat mEdgeImage = new Mat(bitMapGallery.getHeight(), bitMapGallery.getWidth(), CvType.CV_8UC1);

        Utils.bitmapToMat(bitMapGallery, matGallery);
        Imgproc.cvtColor(matGallery, matGreyGallery, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(matGreyGallery, mEdgeImage, mMinThres, mMaxThres);

        double minDist = 150; //Troppo piccolo = cerchi multipli falsi & Troppo grande = cerchi corretti perduti
        int thickness = 5;
        double cannyHighThreshold = 150;
        double accumlatorThreshold = 50;
        circles = new Mat();

        Imgproc.HoughCircles(matGreyGallery, circles, Imgproc.CV_HOUGH_GRADIENT, 1, minDist, cannyHighThreshold, accumlatorThreshold, 0, 0);
        Mat mask = new Mat(matGallery.rows(), matGallery.cols(), CvType.CV_8UC4);
        for(int i = 0; i < circles.cols(); i++) {
            double[] circle = circles.get(0, i);
            double centerX = circle[0], centerY = circle[1], radius = circle[2];
            //Per trovare di ogni immagine il massimo e minimo raggio
            if(radius > radiusMAX) {
                radiusMAX = radius;

            }
            if(radius < radiusMIN) {
                radiusMIN = radius;

            }
            org.opencv.core.Point center = new org.opencv.core.Point(centerX, centerY);
            Imgproc.circle(matGallery, center, (int) radius, new Scalar(0, 0, 0), thickness);

            //Maschera per evidenziare area trovata
            Imgproc.circle(mask, center, (int) radius, new Scalar(255, 255, 255), -1);
            number++;

        }
        Log.i(TAG, "Trovati " + number + " comandi");
        textCommand.setText(number + " " + getResources().getString(R.string.command));
        Mat filtredImage = new Mat(matGallery.rows(), matGallery.cols(), CvType.CV_8UC4);
        matGallery.copyTo(filtredImage, mask); //Filtro l'immagine
        displayImage(filtredImage, 2);
        return matGallery;

    }

    private Mat calcHist(Mat image) {
        int mHistSizeNum = 128;
        Mat copyImage = new Mat(image.rows(), image.cols(), CvType.CV_8UC4);
        Mat histImage = null;
        int[] medBuff = new int[3];
        Map<Double, String> colors = new TreeMap<>();
        MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);

        if(number > 0) {
            //Log.i(TAG, "-->" + circles.cols());
            for(int i = 0; i < circles.cols(); i++) {
                Log.i(TAG, "#" + i);
                double radiusINT;
                double radiusEST;
                Mat maskCircle = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);
                Mat tmp = new Mat(image.rows(), image.cols(), CvType.CV_8UC4);

                double[] circle = circles.get(0, i);
                double centerX = circle[0], centerY = circle[1], radius = circle[2];
                org.opencv.core.Point center = new org.opencv.core.Point(centerX, centerY);
                double DIST = (radiusMAX - radiusMIN) / 2;

                //Log.i(TAG, "RADIUSMAX: " + radiusMAX + " - RADIUSMIN: " + radiusMIN);
                //Log.i(TAG, "RADIUS ---> " + radius);
                image.copyTo(tmp);
                if(number == 1) {
                    Log.i(TAG, "E' stato trovato un unico comando");
                    Imgproc.circle(maskCircle, center, (int) (radius * 1.9), new Scalar(255, 255, 255), -1);
                    Imgproc.circle(maskCircle, center, (int) (radius * 0.95), new Scalar(0, 0, 0), -1);
                    image.copyTo(copyImage, maskCircle); //Filtro l'immagine

                    //}
                } else {
                    if (radiusMIN > radiusMAX - 2) {
                        //Gestire se tutti cerchi esterni o tutti cerchi interni
                        Log.i(TAG, "Tutti cerchi interni/esterni");
                        Imgproc.circle(maskCircle, center, (int) (radius * 1.9), new Scalar(255, 255, 255), -1);
                        Imgproc.circle(maskCircle, center, (int) (radius * 0.95), new Scalar(0, 0, 0), -1);
                        image.copyTo(copyImage, maskCircle); //Filtro l'immagine

                    } else {
                        if (radius >= radiusMIN && radius < radiusMIN + DIST) {
                            Log.i(TAG, "E' stato trovato un cilindro interno");
                            //disegnare cerchio esterno
                            radiusEST = radius * 2;
                            Imgproc.circle(image, center, (int) radiusEST, new Scalar(0, 0, 0), 5);
                            //maschero le due parti
                            Imgproc.circle(maskCircle, center, (int) radiusEST, new Scalar(255, 255, 255), -1);
                            Imgproc.circle(maskCircle, center, (int) radius, new Scalar(0, 0, 0), -1);
                            image.copyTo(copyImage, maskCircle); //Filtro l'immagine

                        } else if (radius > radiusMAX - DIST && radius <= radiusMAX) {
                            Log.i(TAG, "E' stato trovato un cilindro esterno");
                            //disegnare cerchio interno
                            radiusINT = radius * 0.5;
                            Imgproc.circle(image, center, (int) radiusINT, new Scalar(0, 0, 0), 5);
                            //maschero le due parti
                            Imgproc.circle(maskCircle, center, (int) radius, new Scalar(255, 255, 255), -1);
                            Imgproc.circle(maskCircle, center, (int) radiusINT, new Scalar(0, 0, 0), -1);
                            image.copyTo(copyImage, maskCircle); //Filtro l'immagine

                        }
                    }
                }
                //Rilevo il colore
                Mat hist = new Mat();
                Mat hist_red = new Mat();
                Mat hist_green = new Mat();
                Mat hist_blue = new Mat();
                float[] mBuff = new float[mHistSizeNum];
                Scalar mColorsRGB[] = new Scalar[]{
                        new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)

                };

                org.opencv.core.Point mP1 = new org.opencv.core.Point();
                org.opencv.core.Point mP2 = new org.opencv.core.Point();

                int thickness = copyImage.cols() / (mHistSizeNum + 10) / 3;
                if(thickness > 3) thickness = 3;
                MatOfInt mChannels[] = new MatOfInt[]{
                        new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)

                };
                Size sizeRgba = copyImage.size();
                histImage = new Mat(copyImage.rows(), copyImage.cols(), CvType.CV_8UC4);
                Scalar color = new Scalar(0, 0, 0);
                histImage.setTo(color);
                int offset = (int) ((sizeRgba.width - (3 * mHistSizeNum + 30) * thickness));

                //RGB
                for(int c = 0; c < 3; c++) {
                    int med = 0;
                    //3° parametro è la maschera
                    Imgproc.calcHist(Arrays.asList(tmp), mChannels[c], maskCircle, hist, mHistSize, histogramRanges);
                    //Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                    Mat histGAUS = new Mat();
                    Imgproc.GaussianBlur(hist, histGAUS, new Size(99, 99), 0, 0);
                    histGAUS.get(0, 0, mBuff);
                    for(int h = 0; h < mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                        mP1.y = sizeRgba.height - 1;
                        mP2.y = mP1.y - (int) mBuff[h];
                        Imgproc.line(histImage, mP1, mP2, mColorsRGB[c], thickness);

                    }
                    if(c == 0) {
                        histGAUS.copyTo(hist_red);

                    } else if(c == 1) {
                        histGAUS.copyTo(hist_green);

                    } else if(c == 2) {
                        histGAUS.copyTo(hist_blue);

                    }
                    int v = 0;
                    for(int j = 0; j < mHistSizeNum; j++) {
                        if(mBuff[j] > med) { //Se una immagine è rossa, allora avrà più pixel in una tonalità rossa
                            med = (int) mBuff[j];
                            v = j;

                        }
                    }
                    medBuff[c] = v;
                    Log.d(TAG, "----------------- PICCO #" + c + " = " + medBuff[c]);

                }

                //Log.i(TAG, "Istogramma creato correttamente");
                //RED
                if(medBuff[0] > medBuff[1] && medBuff[0] > medBuff[2]) {
                    /*-------------------Inizio Pattern-------------------*/
                    Mat start = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(255, 0, 0));
                    Mat else_ = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(150, 0, 0));

                    Mat hist_st = new Mat();
                    Mat hist_e = new Mat();

                    Imgproc.calcHist(Arrays.asList(start), new MatOfInt(0), maskCircle, hist_st, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(else_), new MatOfInt(0), maskCircle, hist_e, mHistSize, histogramRanges);

                    Mat hist_st_gaus = new Mat();
                    Mat hist_e_gaus = new Mat();

                    //Valori finali = la deviazione standard in x e y. Se messi a 0 OpenCV li calcola in automatico sulla base del Size
                    Imgproc.GaussianBlur(hist_st, hist_st_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_e, hist_e_gaus, new Size(99, 99), 0, 0);
                    /*-------------------Fine Pattern-------------------*/

                    double compare_st = Imgproc.compareHist(hist_red, hist_st_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_e = Imgproc.compareHist(hist_red, hist_e_gaus, Imgproc.CV_COMP_INTERSECT);

                    Log.i(TAG, compare_st + " / " + compare_e);
                    double[] array_hist_compare_red = {compare_st, compare_e};
                    double highest_red = array_hist_compare_red[0];
                    for(int j = 1; j < array_hist_compare_red.length; j++) {
                        int retval = Double.compare(highest_red, array_hist_compare_red[j]);
                        if(retval < 0) {
                            highest_red = array_hist_compare_red[j];

                        }
                    }
                    //Log.i(TAG, String.valueOf(highest_blue));

                    if(highest_red == array_hist_compare_red[0]) {
                        colors.put(centerY, "000");
                        Log.i(TAG, "START");

                    } else if(highest_red == array_hist_compare_red[1]) {
                        colors.put(centerY, "4");
                        Log.i(TAG, "ELSE");

                    }
                    //BLUE
                } else if(medBuff[2] > medBuff[0] && medBuff[2] > medBuff[1]) {
                    /*-------------------Inizio Pattern-------------------*/
                    Mat stop = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 0, 255));
                    Mat while_stop = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 0, 170));
                    Mat if_stop = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 0, 85));

                    Mat hist_s = new Mat();
                    Mat hist_w_s = new Mat();
                    Mat hist_i_s = new Mat();

                    Imgproc.calcHist(Arrays.asList(stop), new MatOfInt(2), maskCircle, hist_s, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(while_stop), new MatOfInt(2), maskCircle, hist_w_s, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(if_stop), new MatOfInt(2), maskCircle, hist_i_s, mHistSize, histogramRanges);

                    Mat hist_s_gaus = new Mat();
                    Mat hist_w_s_gaus = new Mat();
                    Mat hist_i_s_gaus = new Mat();

                    //Valori finali = la deviazione standard in x e y. Se messi a 0 OpenCV li calcola in automatico sulla base del Size
                    Imgproc.GaussianBlur(hist_s, hist_s_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_w_s, hist_w_s_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_i_s, hist_i_s_gaus, new Size(99, 99), 0, 0);
                    /*-------------------Fine Pattern-------------------*/

                    double compare_s = Imgproc.compareHist(hist_blue, hist_s_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_w_s = Imgproc.compareHist(hist_blue, hist_w_s_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_i_s = Imgproc.compareHist(hist_blue, hist_i_s_gaus, Imgproc.CV_COMP_INTERSECT);

                    Log.i(TAG, compare_s + " / " + compare_w_s + " / " + compare_i_s);
                    double[] array_hist_compare_blue = {compare_s, compare_w_s, compare_i_s};
                    double highest_blue = array_hist_compare_blue[0];
                    for(int j = 1; j < array_hist_compare_blue.length; j++) {
                        int retval = Double.compare(highest_blue, array_hist_compare_blue[j]);
                        if(retval < 0) {
                            highest_blue = array_hist_compare_blue[j];

                        }
                    }
                    //Log.i(TAG, String.valueOf(highest_blue));

                    if(highest_blue == array_hist_compare_blue[0]) {
                        colors.put(centerY, "666");
                        Log.i(TAG, "STOP");

                    } else if(highest_blue == array_hist_compare_blue[1]) {
                        colors.put(centerY, "5");
                        Log.i(TAG, "STOP WHILE");

                    } else if(highest_blue == array_hist_compare_blue[2]) {
                        colors.put(centerY, "9");
                        Log.i(TAG, "STOP IF");

                    }
                    //GREEN
                } else if(medBuff[1] > medBuff[0] && medBuff[1] > medBuff[2]) {
                    /*-------------------Inizio Pattern-------------------*/
                    Mat forward = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 255, 0));
                    Mat right = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 200, 0));
                    Mat left = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 150, 0));
                    Mat if_else = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 100, 0));
                    Mat while_ = new Mat(image.rows(), image.cols(), CvType.CV_8UC4, new Scalar(0, 50, 0));

                    Mat hist_f = new Mat();
                    Mat hist_r = new Mat();
                    Mat hist_l = new Mat();
                    Mat hist_i = new Mat();
                    Mat hist_w = new Mat();

                    Imgproc.calcHist(Arrays.asList(forward), new MatOfInt(1), maskCircle, hist_f, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(right), new MatOfInt(1), maskCircle, hist_r, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(left), new MatOfInt(1), maskCircle, hist_l, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(if_else), new MatOfInt(1), maskCircle, hist_i, mHistSize, histogramRanges);
                    Imgproc.calcHist(Arrays.asList(while_), new MatOfInt(1), maskCircle, hist_w, mHistSize, histogramRanges);

                    Mat hist_f_gaus = new Mat();
                    Mat hist_r_gaus = new Mat();
                    Mat hist_l_gaus = new Mat();
                    Mat hist_i_gaus = new Mat();
                    Mat hist_w_gaus = new Mat();

                    //Valori finali = la deviazione standard in x e y. Se messi a 0 OpenCV li calcola in automatico sulla base del Size
                    Imgproc.GaussianBlur(hist_f, hist_f_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_r, hist_r_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_l, hist_l_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_i, hist_i_gaus, new Size(99, 99), 0, 0);
                    Imgproc.GaussianBlur(hist_w, hist_w_gaus, new Size(99, 99), 0, 0);
                    /*-------------------Fine Pattern-------------------*/

                    //Istogramma pattern
                    /*Mat pattHist = new Mat(image.rows(), image.cols(), CvType.CV_8UC4);
                    pattHist.setTo(color);
                    float[] pattMBuff = new float[mHistSizeNum];
                    int pattThickness = image.cols() / (mHistSizeNum + 10) / 3;
                    if(pattThickness > 3) pattThickness = 3;
                    Size pattSizeRgba = image.size();
                    int pattOffset = (int) ((pattSizeRgba.width - (3 * mHistSizeNum + 30) * pattThickness));
                    hist_f_gaus.get(0, 0, pattMBuff);

                    for(int h = 0; h < mHistSizeNum; h++) {
                        eP1.x = eP2.x = pattOffset + (1 * (mHistSizeNum + 10) + h) * pattThickness;
                        eP1.y = pattSizeRgba.height - 1;
                        eP2.y = eP1.y - (int) pattMBuff[h];
                        Imgproc.line(pattHist, eP1, eP2, mColorsRGB[1], pattThickness);

                    }
                    displayImage(pattHist, 1);*/

                    //CV_COMP_CORREL CV_COMP_INTERSECT < 0
                    //CV_COMP_BHATTACHARYYA > 0
                    double compare_f = Imgproc.compareHist(hist_green, hist_f_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_r = Imgproc.compareHist(hist_green, hist_r_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_l = Imgproc.compareHist(hist_green, hist_l_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_i = Imgproc.compareHist(hist_green, hist_i_gaus, Imgproc.CV_COMP_INTERSECT);
                    double compare_w = Imgproc.compareHist(hist_green, hist_w_gaus, Imgproc.CV_COMP_INTERSECT);

                    //Log.i(TAG, compare_f + " / " + compare_r + " / " + compare_l + " / " + compare_i + " / " + compare_w);
                    double[] array_hist_compare_green = {compare_f, compare_r, compare_l, compare_i, compare_w};
                    double highest_green = array_hist_compare_green[0];
                    for(int j = 1; j < array_hist_compare_green.length; j++) {
                        int retval = Double.compare(highest_green, array_hist_compare_green[j]);
                        if(retval < 0) {
                            highest_green = array_hist_compare_green[j];

                        }
                    }
                    //Log.i(TAG, String.valueOf(highest_green));

                    if(highest_green == array_hist_compare_green[0]) {
                        colors.put(centerY, "1");
                        Log.i(TAG, "FORWARD");

                    } else if(highest_green == array_hist_compare_green[2]) {
                        colors.put(centerY, "2");
                        Log.i(TAG, "TURN LEFT");

                    } else if(highest_green == array_hist_compare_green[1]) {
                        colors.put(centerY, "3");
                        Log.i(TAG, "TURN RIGHT");

                    } else if(highest_green == array_hist_compare_green[4]) {
                        colors.put(centerY, "10");
                        Log.i(TAG, "WHILE");

                    } else if(highest_green == array_hist_compare_green[3]) {
                        colors.put(centerY, "333");
                        Log.i(TAG, "IF");

                    }
                } else {
                    colors.put(centerY, getResources().getString(R.string.unknown));
                    Log.i(TAG, "UNKNOWN");

                }
            }
            //Comandi in ordine
            String resultColors = "";
            scrivi = false;
            errWhile = false;
            errStopExcess = false;
            int contI = 1;
            int contC = 1;
            int contE = 1;
            for(int i = 0; i < N; i++){
                errIf[i] = false;
                errIfCond[i] = false;
                errElse[i] = false;

            }
            Log.d(TAG, "-------------------------------------------------------------------------------------");
            for(Map.Entry<Double, String> entry : colors.entrySet()) {
                Log.d(TAG, "contI / contC / contE \n" + contI + contC + contE);
                if(entry.getValue() == "1" && scrivi) {
                    if(errIf[contI - 1] & errIfCond[contC - 1]){
                        resultColors = resultColors + getResources().getString(R.string.if_forward) + " - ";
                        solution = solution + "8,";
                        contC--;
                        errIfCond[contC] = false;

                    } else {
                        resultColors = resultColors + getResources().getString(R.string.forward) + " - ";
                        solution = solution + "1,";

                    }
                } else if(entry.getValue() == "2" && scrivi) {
                    if(errIf[contI - 1] & errIfCond[contC - 1]){
                        resultColors = resultColors + getResources().getString(R.string.if_left) + " - ";
                        solution = solution + "6,";
                        contC--;
                        errIfCond[contC] = false;

                    } else {
                        resultColors = resultColors + getResources().getString(R.string.left) + " - ";
                        solution = solution + "2,";

                    }
                } else if(entry.getValue() == "3" && scrivi) {
                    if(errIf[contI - 1] & errIfCond[contC - 1]){
                        resultColors = resultColors + getResources().getString(R.string.if_right) + " - ";
                        solution = solution + "7,";
                        contC--;
                        errIfCond[contC] = false;

                    } else {
                        resultColors = resultColors + getResources().getString(R.string.right) + " - ";
                        solution = solution + "3,";

                    }
                } else if(entry.getValue() == "10" && scrivi) {
                    if(!errWhile){
                        errWhile = true;

                    }
                    resultColors = resultColors + getResources().getString(R.string.while_) + " - ";
                    solution = solution + "10,";

                } else if(entry.getValue() == "5" && scrivi) {
                    if(errWhile) {
                        errWhile = false;

                    } else {
                        errWhile = true;

                    }
                    resultColors = resultColors + getResources().getString(R.string.while_stop) + " - ";
                    solution = solution + "5,";

                } else if(entry.getValue() == "333" && scrivi) {
                    errIf[contI] = true;
                    errIfCond[contC] = true;
                    contI++;
                    contC++;

                } else if(entry.getValue() == "9" && scrivi) {
                    if(errIf[contI - 1] & !errIfCond[contI - 1] & !errElse[contI - 1]) {
                        //OK
                        contI--;
                        errIf[contI] = false;

                    } else if(errIf[contI - 1] & errIfCond[contI - 1] & !errElse[contI - 1]) {
                        //Err Cond
                        contI--;
                        errIf[contI] = false;

                    } else if(!errIf[contI - 1] & errIfCond[contI - 1] & !errElse[contI - 1]) {
                        //Err Cond + Excess
                        errStopExcess = true;

                    } else if(!errIf[contI - 1] & !errIfCond[contI - 1] & !errElse[contI - 1]) {
                        //Err Excess
                        errStopExcess = true;

                    } else if(errIf[contI - 1] & errIfCond[contI - 1] & errElse[contI - 1]) {
                        //Err Cond
                        contI--;
                        contE--;
                        errIf[contI] = false;
                        errElse[contE] = false;

                    } else if(errIf[contI - 1] & !errIfCond[contI - 1] & errElse[contI - 1]) {
                        //OK
                        contI--;
                        contE--;
                        errIf[contI] = false;
                        errElse[contE] = false;

                    } else if(!errIf[contI - 1] & errIfCond[contI - 1] & errElse[contI - 1]) {
                        //Err Cond + Logica If/Else

                    } else if(!errIf[contI - 1] & !errIfCond[contI - 1] & errElse[contI - 1]) {
                        //Err Logica If/Else

                    }
                    resultColors = resultColors + getResources().getString(R.string.if_else_stop) + " - ";
                    solution = solution + "9,";

                } else if(entry.getValue() == "4") {
                    errElse[contE] = true;
                    contE++;
                    resultColors = resultColors + getResources().getString(R.string.else_) + " - ";
                    solution = solution + "4,";

                } else if(entry.getValue() == "000") {
                    scrivi = true;
                    resultColors = resultColors + getResources().getString(R.string.start) + " - ";

                } else if(entry.getValue() == "666") {
                    scrivi = false;
                    resultColors = resultColors + getResources().getString(R.string.stop) + ".";

                }
            }
            solution = solution.substring(0, solution.length() - 1);
            solution = solution + "]";
            Log.i(TAG, "Soluzione Blockly: " + solution);

            textCommand.setText(resultColors);
            displayImage(copyImage, 1);
            return image;
            //return histImage;

        }
        textCommand.setText(getResources().getString(R.string.nothing));
        return image;

    }

    public void animateFAB() {
        if(isFabOpen) {
            actionFloat.startAnimation(rotate_backward);
            galleryFloat.startAnimation(fab_close);
            circleFloat.startAnimation(fab_close);
            colorFloat.startAnimation(fab_close);
            confirmFloat.startAnimation(fab_close);
            galleryText.startAnimation(fab_close);
            circleText.startAnimation(fab_close);
            colorText.startAnimation(fab_close);
            confirmText.startAnimation(fab_close);
            galleryFloat.setClickable(false);
            circleFloat.setClickable(false);
            colorFloat.setClickable(false);
            confirmFloat.setClickable(false);
            isFabOpen = false;

        } else {
            actionFloat.startAnimation(rotate_forward);
            galleryFloat.startAnimation(fab_open);
            circleFloat.startAnimation(fab_open);
            colorFloat.startAnimation(fab_open);
            confirmFloat.startAnimation(fab_open);
            galleryText.startAnimation(fab_open);
            circleText.startAnimation(fab_open);
            colorText.startAnimation(fab_open);
            confirmText.startAnimation(fab_open);
            galleryFloat.setClickable(true);
            circleFloat.setClickable(true);
            colorFloat.setClickable(true)
            ;confirmFloat.setClickable(true);
            isFabOpen = true;

        }
    }

    public void onValueChange(int newVal) {
        nLevel = newVal;
        npLevel.setText(String.valueOf(newVal));

    }
}