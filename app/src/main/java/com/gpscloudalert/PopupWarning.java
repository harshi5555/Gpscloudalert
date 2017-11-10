package com.gpscloudalert;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.google.android.gms.wearable.DataMap.TAG;


public  class PopupWarning extends DialogFragment {
    LayoutInflater inflater;
    View view;
   private TextView warningStreet;
    String strtext;
    private Button btnOk;
    private TextView countDone;

    private static String countDoneText;
    public static TextString dataPoint;

    double  longitude,latiitude;



    public void setCountdown(String test){
        countDone.setText(test);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.activity_popup_warning, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //get arguments from the bundle
        Bundle mArgs = getArguments();
        String key = mArgs.getString("key");


        btnOk = (Button) view.findViewById(R.id.btnOk);
        warningStreet = (TextView) view.findViewById(R.id.warningStreet);
        warningStreet.setText("Warning Street:  " + key);
        countDone = (TextView)view.findViewById(R.id.countDown);
        //countDone.setText(dataPoint.getS());
       // Log.e(TAG, "count distance5555 " + dataPoint.getS());

        dataPoint = new TextString(countDone);

        builder.setView(view).create();
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                dismiss();
            }
        });



        return builder.create();
    }


    public class TextString {
         private String s;
        private TextView textView;

        public TextString(TextView tv){
            textView = tv;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
            textView.setText(s);
           // textView.invalidate();
            Log.e(TAG, "count distance5555 " + s);
        }


    }





    }












