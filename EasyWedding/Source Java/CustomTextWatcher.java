package com.example.easywedding;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

public class CustomTextWatcher implements TextWatcher {

    private TextInputLayout mTextInputLayout;
    private boolean[] changeInState;
    private int mChangedDataIndex;

    public CustomTextWatcher(TextInputLayout textInputLayout,boolean[] changeInState, int mChangedDataIndex){
        this.mTextInputLayout = textInputLayout;
        this.changeInState = changeInState;
        this.mChangedDataIndex = mChangedDataIndex;
    }

    public CustomTextWatcher(TextInputLayout textInputLayout){
        this.mTextInputLayout = textInputLayout;

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mTextInputLayout.setError("");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (changeInState != null)
            changeInState[mChangedDataIndex] = true;
    }
}
