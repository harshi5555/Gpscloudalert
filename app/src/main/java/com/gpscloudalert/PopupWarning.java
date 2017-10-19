package com.gpscloudalert;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static android.content.Intent.getIntent;

public class PopupWarning extends DialogFragment {
    LayoutInflater inflater;
    View view;
   private TextView warningStreet;
    String strtext;
    private Button btnOk;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.activity_popup_warning, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle mArgs = getArguments();
        String key = mArgs.getString("key");
        btnOk = (Button) view.findViewById(R.id.btnOk);
        warningStreet = (TextView) view.findViewById(R.id.warningStreet);
        warningStreet.setText("Warning Street:  " + key);





       /* builder.setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();*/
        builder.setView(view).create();

        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                dismiss();
            }
        });

        return builder.create();
    }



    }







