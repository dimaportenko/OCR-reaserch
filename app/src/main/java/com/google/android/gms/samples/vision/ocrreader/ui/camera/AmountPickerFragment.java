package com.google.android.gms.samples.vision.ocrreader.ui.camera;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.google.android.gms.samples.vision.ocrreader.R;

/**
 * Created by Dmytro Portenko on 5/20/17.
 */

public class AmountPickerFragment extends DialogFragment {

    private int mAmount = 1;
    private int mTotalAmount = 1;
    private AmountPickerFragmentListener listener = null;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.amount_picker_dialog, null);
        final NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.amountPicker);
        amountPicker.setMinValue(1);
        amountPicker.setMaxValue(100);
        amountPicker.setValue(mAmount);

        final NumberPicker amountPickerTotal = (NumberPicker) view.findViewById(R.id.amountPickerTotal);
        amountPickerTotal.setMinValue(1);
        amountPickerTotal.setMaxValue(100);
        amountPickerTotal.setValue(mTotalAmount);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.amount_picker_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(listener != null) {
                            listener.onPositiveButtonClick(amountPicker.getValue(), amountPickerTotal.getValue());
                        }
                    }
                })
                .setNegativeButton(R.string.amount_picker_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AmountPickerFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void setAmountPickerFragmentListener(AmountPickerFragmentListener listener) {
        this.listener = listener;
    }

    public interface AmountPickerFragmentListener {
        public void onPositiveButtonClick(int amount, int totalAmount);
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int mAmount) {
        this.mAmount = mAmount;
    }


    public int getTotalAmount() {
        return mTotalAmount;
    }

    public void setTotalAmount(int mTotalAmount) {
        this.mTotalAmount = mTotalAmount;
    }
}
