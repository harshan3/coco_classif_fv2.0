package com.example.coco_classif;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImageClassifier {

    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROABILITY_STD = 255.0f ;
    private static final float IMAGE_STD = 1.0f;
    private static final float IMAGE_MEAN = 0.0f;
    private final Interpreter tensorClassifier;
    private final int imageResizeX;
    private final List<String>labels;
    private final int imageResizeY;
    private  TensorImage inputImageBuffer;
    private final TensorBuffer probabilityImageBuffer;
    private final TensorProcessor probabilityProcessor;

    public  ImageClassifier(Activity activity) throws IOException {

        MappedByteBuffer classfireModel = FileUtil.loadMappedFile(activity,"cocomodel_quant.tflite");

         labels = FileUtil.loadLabels(activity, "coco_Label.txt");

        tensorClassifier = new Interpreter(classfireModel,null);

        int imageTensorIndex = 0;
        int probablityTensorIndex =0;

        int[] inputImageShape = tensorClassifier.getInputTensor(imageTensorIndex).shape();
        DataType inputDataType = tensorClassifier.getInputTensor(imageTensorIndex).dataType();

        int[] outputImaageShape = tensorClassifier.getOutputTensor(probablityTensorIndex).shape();
        DataType outputDataType = tensorClassifier.getOutputTensor(probablityTensorIndex).dataType();

        imageResizeX = inputImageShape[1];
        imageResizeY = inputImageShape[2];

        inputImageBuffer = new TensorImage(inputDataType);

        probabilityImageBuffer = TensorBuffer.createFixedSize(outputImaageShape,outputDataType);

        probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(PROBABILITY_MEAN,PROABILITY_STD)).build();

    }
    public List<Recognition>recognizeImage(final Bitmap bitmap, final  int sensorOriwentation){

        Bitmap resized_bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        List<Recognition> recognitions = new ArrayList<>();
        inputImageBuffer = loadImage(resized_bitmap,sensorOriwentation);
        tensorClassifier.run(inputImageBuffer.getBuffer(),probabilityImageBuffer.getBuffer().rewind());

        Map<String,Float> labelledProbability = new TensorLabel(labels,
                probabilityProcessor.process(probabilityImageBuffer)).getMapWithFloatValue();
        Integer i= new Integer(0);
        String minKey = null;
        Float minValue = new Float(0.0);
        for (Map.Entry<String, Float>entry : labelledProbability.entrySet()){
            if(i==0) {
                minValue = entry.getValue();
                minKey = entry.getKey();
            }else if(minValue > entry.getValue()){
                minValue = entry.getValue();
                minKey = entry.getKey();
            }
            i++;
            Log.d("myTag", "entry.getKey():  "+ entry.getKey() + "   " + "entry.getValue():  "+entry.getValue());

        }
        if(minKey != null){
            recognitions.add(new Recognition(minKey,minValue));
        }
        return recognitions;
    }

    private TensorImage loadImage(Bitmap bitmap, int sensorOrientation) {
        Bitmap resized_bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        inputImageBuffer.load(resized_bitmap);
        int noOfRotations = sensorOrientation/90;

        int cropSize = Math.min((resized_bitmap.getWidth()),resized_bitmap.getHeight());
       // Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.8), (int)(bitmap.getHeight()*0.8), true);


        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize,cropSize))
                .add(new ResizeOp(imageResizeX,imageResizeY,ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op(sensorOrientation))
                .add(new NormalizeOp(IMAGE_MEAN, IMAGE_STD))
                .build();
        return  imageProcessor.process(inputImageBuffer);
    }

    class Recognition implements Comparable{
        private String name;
        private float confidance;

        public Recognition(){


        }

        public Recognition(String name, float confidance) {
            this.name = name;
            this.confidance = confidance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getConfidance() {
            return confidance;
        }

        public void setConfidance(float confidance) {
            this.confidance = confidance;
        }

        @Override
        public String toString() {
            return "Recognition{" +
                    "name='" + name + '\'' +
                    ", confidance=" + confidance +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            return Float.compare(((Recognition)o).confidance,this.confidance);
        }
    }

}
