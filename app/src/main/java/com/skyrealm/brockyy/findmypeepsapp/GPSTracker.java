package com.skyrealm.brockyy.findmypeepsapp;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextThemeWrapper;

import com.google.android.gms.location.LocationListener;
import com.skyrealm.brockyy.findmypeepsapp.R;

/**
 *         Gps location tracker class
 *         to get users location and other information related to location
 */
public class GPSTracker extends Service implements android.location.LocationListener
{


    /**
     * context of calling class
     */
    private Context mContext;

    /**
     * flag for gps status
     */
     boolean isGpsEnabled = false;

    /**
     * flag for network status
     */
     boolean isNetworkEnabled = false;

    /**
     * flag for gps
     */
    private boolean canGetLocation = false;

    /**
     * location
     */
    private Location mLocation;

    /**
     * latitude
     */
    private double mLatitude;

    /**
     * longitude
     */
    private double mLongitude;

    /**
     * min distance change to get location update
     */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;

    /**
     * min time for location update
     * 60000 = 1min
     */
    private static final long MIN_TIME_FOR_UPDATE = 60000;

    /**
     * location manager
     */
    private LocationManager mLocationManager;


    /**
     * @param mContext constructor of the class
     */
    public GPSTracker(Context mContext) {

        this.mContext = mContext;
        getLocation();
    }


    /**
     * @return location
     */
    public Location getLocation() {

        try {

            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            /*getting status of the gps*/
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            /*getting status of network provider*/
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled || !isNetworkEnabled) {

                /*no location provider enabled*/
            } else {

                this.canGetLocation = true;

                /*getting location from network provider*/
                if (isNetworkEnabled) {

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);

                    if (mLocationManager != null) {

                        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (mLocation != null) {

                            mLatitude = mLocation.getLatitude();

                            mLongitude = mLocation.getLongitude();
                        }
                    }
                    /*if gps is enabled then get location using gps*/
                    if (isGpsEnabled) {

                        if (mLocation == null) {

                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, (android.location.LocationListener) this);

                            if (mLocationManager != null) {

                                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (mLocation != null) {

                                    mLatitude = mLocation.getLatitude();

                                    mLongitude = mLocation.getLongitude();
                                }

                            }
                        }

                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return mLocation;
    }

    /**
     * call this function to stop using gps in your application
     */
    public void stopUsingGps() {

        if (mLocationManager != null) {

            mLocationManager.removeUpdates((android.location.LocationListener) GPSTracker.this);

        }
    }

    /**
     * @return latitude
     *         <p/>
     *         function to get latitude
     */
    public double getLatitude() {

        if (mLocation != null) {

            mLatitude = mLocation.getLatitude();
        }
        return mLatitude;
    }

    /**
     * @return longitude
     *         function to get longitude
     */
    public double getLongitude() {

        if (mLocation != null) {

            mLongitude = mLocation.getLongitude();

        }

        return mLongitude;
    }

    /**
     * @return to check gps or wifi is enabled or not
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * function to prompt user to open
     * settings to enable gps
     */
    public void showSettingsAlert() {

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AppTheme));

        mAlertDialog.setTitle("Gps Disabled");

        mAlertDialog.setMessage("GPS not enabled or internet not available.");

        mAlertDialog.setPositiveButton("settings", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(mIntent);
            }
        });

        mAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();

            }
        });

        final AlertDialog mcreateDialog = mAlertDialog.create();
        mcreateDialog.show();
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void LoginAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Location and Connection services");
        alertDialog.setMessage("Wifi/Data not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;


        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean isGPSEnabledOrNot() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            return false;
        } else {
            return true;
        }
    }
}
