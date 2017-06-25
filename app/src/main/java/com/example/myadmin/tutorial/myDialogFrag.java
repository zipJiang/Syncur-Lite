package com.example.myadmin.tutorial;

/*
    这个类是自定义的Dialog类，用以调节鼠标的敏感度
    利用button激活的 snackbar 来调用
*/

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by myadmin on 06/06/2017.
 */
public class myDialogFrag extends DialogFragment {

    /* newInstance 用来生成一个myDialogFrag 的实例 */
    public static myDialogFrag newInstance(int title) {
        myDialogFrag frag = new myDialogFrag();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View myView = inflater.inflate(R.layout.popup, null);

        /* snackBar会激活这个dialog，主要功能是调节鼠标敏感度，敏感度调节通过手机端传送的
         * 速度参数上乘上一个系数得到
         */

        builder.setTitle(R.string.sensi)
                .setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TextView myTextView = (TextView)myView.findViewById(R.id.seekBarProg);
                        String progValue = myTextView.getText().toString();
                        Integer intProgValue = Integer.parseInt(progValue);
                        float localCoeff = (float)(intProgValue.intValue()) / (float)(1000 * 4);
                        TouchScreenActivity.coeff = localCoeff;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setView(myView);

        /* snackBar的基础设置 */

        SeekBar mySeekBar = (SeekBar)myView.findViewById(R.id.seekBarSensi);
        TextView subTextView = (TextView)myView.findViewById(R.id.seekBarProg);
        subTextView.setText(String.valueOf(TouchScreenActivity.coeff));
        mySeekBar.setMax(1 * 1000 * 4);
        mySeekBar.setProgress((int)(TouchScreenActivity.coeff * 1000 * 4));
        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue;
            TextView myTextView = (TextView)myView.findViewById(R.id.seekBarProg);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                myTextView.setText(String.valueOf((float)progressValue / (float)(1000 * 4)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myTextView.setText(String.valueOf(progressValue));
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
