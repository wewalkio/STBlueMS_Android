package com.st.blesensor.cloud.TagoIO;

import android.util.Log;

import androidx.annotation.Nullable;

import com.st.blesensor.cloud.util.SubSamplingFeatureListener;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class TagoIOCentralFeatureListener extends SubSamplingFeatureListener {

    private static final List<Class<? extends Feature>> SUPPORTED_FEATURES = Arrays.asList(
            FeatureAcceleration.class,
            FeatureGyroscope.class,
            FeatureHumidity.class,
            FeatureMagnetometer.class,
            FeaturePressure.class,
            FeatureTemperature.class
    );

    private static final String DATA_TOKEN = "tago/data/post/";

    public static boolean isSupportedFeature(Feature f){
        return SUPPORTED_FEATURES.contains(f.getClass());
    }


    private IMqttAsyncClient mBroker;

    TagoIOCentralFeatureListener(IMqttAsyncClient client,long minUpdateInterval){
        super(minUpdateInterval);

        mBroker = client;

    }

    private JSONObject[] createAccelerometerJson(Feature.Sample sample) throws JSONException {

        JSONObject objX = new JSONObject();
        objX.put("variable","accelerometer_x");
        objX.put("value",FeatureAcceleration.getAccX(sample));
        JSONObject objY = new JSONObject();
        objY.put("variable","accelerometer_y");
        objY.put("value",FeatureAcceleration.getAccX(sample));
        JSONObject objZ = new JSONObject();
        objZ.put("variable","accelerometer_z");
        objZ.put("value",FeatureAcceleration.getAccZ(sample));

        return new JSONObject[]{objX,objY,objZ};
    }

    private JSONObject[] createGyroscopeJson(Feature.Sample sample) throws JSONException {


        JSONObject objX = new JSONObject();
        objX.put("variable","gyroscope_x");
        objX.put("value",FeatureGyroscope.getGyroX(sample));
        JSONObject objY = new JSONObject();
        objY.put("variable","gyroscope_y");
        objY.put("value",FeatureGyroscope.getGyroY(sample));
        JSONObject objZ = new JSONObject();
        objZ.put("variable","gyroscope_z");
        objZ.put("value",FeatureGyroscope.getGyroZ(sample));

        return new JSONObject[]{objX,objY,objZ};
    }

    private JSONObject[] createMagnetometerJson(Feature.Sample sample) throws JSONException {

        JSONObject objX = new JSONObject();
        objX.put("variable","magnetometer_x");
        objX.put("value",FeatureMagnetometer.getMagX(sample));
        JSONObject objY = new JSONObject();
        objY.put("variable","magnetometer_y");
        objY.put("value",FeatureMagnetometer.getMagY(sample));
        JSONObject objZ = new JSONObject();
        objZ.put("variable","magnetometer_z");
        objZ.put("value",FeatureMagnetometer.getMagZ(sample));

        return new JSONObject[]{objX,objY,objZ};

    }

    private JSONObject[] createTemperatureJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("variable","temperature");
        obj.put("value",FeatureTemperature.getTemperature(sample));
        return  new JSONObject[]{obj};
    }

    private JSONObject[] createPressureJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("variable","pressure");
        obj.put("value",FeaturePressure.getPressure(sample));
        return  new JSONObject[]{obj};
    }

    private JSONObject[] createHumidityJson(Feature.Sample sample) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("variable","humidity");
        obj.put("value",FeatureHumidity.getHumidity(sample));
        return  new JSONObject[]{obj};
    }

    private @Nullable
    JSONObject[] createTagoIOJSON(Feature f, Feature.Sample sample) throws JSONException {
        if (f instanceof  FeatureAcceleration){
            return createAccelerometerJson(sample);
        }else if ( f instanceof  FeatureGyroscope){
            return createGyroscopeJson(sample);
        }else if (f instanceof  FeatureMagnetometer){
            return createMagnetometerJson(sample);
        }else if (f instanceof  FeatureTemperature){
            return createTemperatureJson(sample);
        }else if (f instanceof FeaturePressure){
            return createPressureJson(sample);
        }else if ( f instanceof FeatureHumidity){
            return createHumidityJson(sample);
        }
        return null;
    }

    private long nNotificaiton = 0;

    @Override
    public void onNewDataUpdate(Feature f, Feature.Sample sample) {
        try {
            JSONObject[] objs = createTagoIOJSON(f,sample);
            if(objs == null)
                return;
            for(JSONObject obj : objs){
                MqttMessage msg = new MqttMessage(obj.toString().getBytes());
                msg.setQos(1);
                if (mBroker.isConnected()) {
                    Log.d("Update",nNotificaiton+"Listener: "+this+"f: "+f  );
                    nNotificaiton++;
                    mBroker.publish(DATA_TOKEN, msg);
                }
            }
        } catch (MqttException | JSONException | IllegalArgumentException e) {
            Log.e(getClass().getName(), "Error Logging the sample: " +
                    sample + "\nError:" + e.getMessage());
        }//try catch
    }
}
