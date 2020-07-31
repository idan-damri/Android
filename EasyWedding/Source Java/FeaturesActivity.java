package com.example.easywedding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;


import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.easywedding.Utils.Utils;

import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;

import java.util.Currency;

import java.util.Locale;

public class FeaturesActivity extends AppCompatActivity {

    private TextInputLayout mLayoutFeatureName;
    private TextInputLayout mLayoutPaymentBalance;
    private TextInputLayout mLayoutAdvancePayment;

    private EditText mSupplierName;
    private EditText mSupplierPhone;
    private EditText mQuantity;
    private EditText mFreeText;

    private Locale mLocal;
    private String mCurrencySymbol;
    // For handling focus and soft keyboard events
    // when we start finish the activity.
    private LinearLayout mDummyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        setFields();

        hideSoftKeyboard();

        setFeatureNameTextWatcher();

        setCurrencySymbolByLocal();


    }

    private void hideSoftKeyboard() {
        mDummyLayout.setFocusable(true);
        mDummyLayout.setFocusableInTouchMode(true);
        mDummyLayout.requestFocus();
    }

    /**
     *
     */
    private void setCurrencySymbolByLocal() {
        // If the layout direction is from right to left then change the text direction
        // to be from left to right since the user enters numbers
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            // mLayoutPaymentBalance.setEndIconActivated(false);
            //mLayoutPaymentBalance.setStartIconContentDescription(TextInputLayout.END_ICON_CLEAR_TEXT);
            mLayoutPaymentBalance.setTextDirection(TextInputLayout.TEXT_DIRECTION_LTR);
            mLayoutAdvancePayment.setTextDirection(TextInputLayout.TEXT_DIRECTION_LTR);
            // Show the locale currency symbol
            mLayoutPaymentBalance.setSuffixText(" " + mCurrencySymbol);
            mLayoutAdvancePayment.setSuffixText(" " + mCurrencySymbol);
        } else {
            mLayoutPaymentBalance.setPrefixText(" " + mCurrencySymbol);
            mLayoutAdvancePayment.setPrefixText(" " + mCurrencySymbol);
        }

    }


    private void setFields() {
        mLayoutFeatureName = findViewById(R.id.text_layout_feature_name);
        mSupplierName = ((TextInputLayout) findViewById(R.id.text_layout_supplier_name)).getEditText();
        mSupplierPhone = ((TextInputLayout) findViewById(R.id.text_layout_phone_number)).getEditText();
        mLayoutPaymentBalance = findViewById(R.id.text_layout_payment_balance);
        mLayoutAdvancePayment = findViewById(R.id.text_layout_advance_payment);
        mQuantity = ((TextInputLayout) findViewById(R.id.layout_feature_amount)).getEditText();
        mFreeText = ((TextInputLayout) findViewById(R.id.text_layout_feature_free_text)).getEditText();
        mLocal = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        mCurrencySymbol = Currency.getInstance(mLocal).getSymbol();
        mDummyLayout = findViewById(R.id.dummy_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.features_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_feature:
                if (isDataValid()) {
                    // TODO db actions

                    Utils.hideKeyboardInActivity(this);

                    mDummyLayout.requestFocus();

                    Toast.makeText(this, R.string.success_feature_saved, Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isDataValid() {
        String featureName = mLayoutFeatureName.getEditText().getText().toString();

        // Feature name field can't be empty, therefore if this field is empty show an error.
        if (TextUtils.isEmpty(featureName)) {
            mLayoutFeatureName.setError(getString(R.string.error_feature_name));
            mLayoutFeatureName.getEditText().requestFocus();

            return false;
        }

        parseCurrency();

        return true;

    }

    private void parseCurrency() {
        EditText balanceEditText = mLayoutPaymentBalance.getEditText();
        EditText advanceEditText = mLayoutAdvancePayment.getEditText();
        String paymentBalance = "";
        String advancePayment = "";

        if (balanceEditText != null) {
            paymentBalance = balanceEditText.getText().toString();

            if (!TextUtils.isEmpty(paymentBalance))
                paymentBalance = String.format(mLocal, "%1$,.2f", BigDecimal.valueOf(Double.parseDouble(paymentBalance)));

        }
        if (advanceEditText != null) {
            advancePayment = advanceEditText.getText().toString();

            if (!TextUtils.isEmpty(advancePayment)) {
                advancePayment = String.format(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0)
                        , "%1$,.2f", BigDecimal.valueOf(Double.parseDouble(advancePayment)));
            }
        }
        // for debugging
        // now the string contains "," so when this is edit mode you should
        // replace all "," with "" and the operate on the money.
        balanceEditText.setText(paymentBalance);
        advanceEditText.setText(advancePayment);

    }

    private void setFeatureNameTextWatcher() {
        mLayoutFeatureName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mLayoutFeatureName.setError("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}