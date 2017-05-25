package com.google.android.gms.samples.vision.ocrreader.data;

import com.google.android.gms.samples.vision.ocrreader.helper.DataConvertor;
import com.google.android.gms.samples.vision.ocrreader.helper.UniqID;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dmytro Portenko on 11/29/16.
 */

public class ContainerModel {

    private int mId;
    private TextBlock mText;

    private Text selectedText;
    private HashMap<Text, AmountModel> mTextAmounts = new HashMap<Text, AmountModel>();

    private List<Text> mElements = new ArrayList<Text>();
    private List<Text> mActiveElements = new ArrayList<Text>();


    public ContainerModel(int id, TextBlock text) {
        mId = id;
        mText = text;

        updateElementsList();
    }

    public void setSelectedText(Text selectedText) {
        this.selectedText = selectedText;
    }

    private void updateElementsList() {
        if (mText != null) {
            List<? extends Text> textComponents = mText.getComponents();
            for(Text line : textComponents) {
                List<? extends Text> lineComponents = line.getComponents();
                for (Text currentText : lineComponents) {
                    String value = DataConvertor.getNumberFromString(currentText.getValue());
                    if (value != null) {
                        mElements.add(currentText);
                        mTextAmounts.put(currentText, new AmountModel());
                    }
                }
            }
        }
    }

    public AmountModel getSelectedTextAmount() {
        if (selectedText == null) {
            return null;
        }
        return getAmount(this.selectedText);

    }

    public void setSelectedTextAmount(int amount, int totalAmount) {
        if (selectedText == null) {
            return;
        }
        AmountModel amountModel = getAmount(this.selectedText);
        amountModel.amount = amount;
        amountModel.total = totalAmount;
    }

    public AmountModel getAmount(Text text) {
        return mTextAmounts.get(text);
    }

    public List<Text> getElements() {
        return mElements;
    }

    public TextBlock getText() {
        return mText;
    }
    public int getId() {
        return mId;
    }

}
