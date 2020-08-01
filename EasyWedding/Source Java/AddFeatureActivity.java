package com.example.easywedding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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

import com.example.easywedding.model.Feature;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Currency;

import java.util.Locale;
import java.text.DecimalFormat;
import java.text.ParseException;


public class AddFeatureActivity extends AppCompatActivity {

    private TextInputLayout mLayoutFeatureName;
    private TextInputLayout mLayoutPaymentBalance;
    private TextInputLayout mLayoutAdvancePayment;
    private TextInputLayout mLayoutEmail;
    private TextInputLayout mLayoutSupplierName;

    private EditText mSupplierNameEditText;
    private EditText mSupplierPhone;
    private EditText mSupplierEmail;
    private EditText mQuantityEditText;
    private EditText mFreeText;
    private EditText mFeatureNameEditText;
    private EditText mEmailEditText;
    private EditText mPaymentBalanceEditText;
    private EditText mAdvancePaymentEditText;
    // For the currency of the advance payment and payment balance
    private Locale mLocal;
    private String mCurrencySymbol;
    // For handling focus and soft keyboard events
    // when we start finish the activity.
    private LinearLayout mDummyLayout;
    // db
    private DatabaseReference mRootReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        String featureName = getIntent().getStringExtra(FeaturesFragment.EXTRA_FEATURE_NAME);
        // If user is adding a new feature then the title should be corresponding
        // to adding a new feature.
        if (featureName == null)
            setTitle(R.string.title_add_feature);
        else {
            // TODO update an existing feature
        }

        // Enable the up button.
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);


        setFields();

        hideSoftKeyboard();

        setTextWatchers();

        setCurrencySymbolByLocal();


    }


    private void setTextWatchers() {
        mFeatureNameEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutFeatureName));
        mEmailEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutEmail));
        mSupplierNameEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutSupplierName));
        mPaymentBalanceEditText.addTextChangedListener(new NumberTextWatcher(mPaymentBalanceEditText));
        mAdvancePaymentEditText.addTextChangedListener(new NumberTextWatcher(mAdvancePaymentEditText));
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
        mLayoutSupplierName = findViewById(R.id.text_layout_supplier_name);
        mSupplierPhone = ((TextInputLayout) findViewById(R.id.text_layout_phone_number)).getEditText();
        mLayoutEmail = findViewById(R.id.text_layout_email);
        mSupplierEmail = mLayoutEmail.getEditText();
        mLayoutPaymentBalance = findViewById(R.id.text_layout_payment_balance);
        mLayoutAdvancePayment = findViewById(R.id.text_layout_advance_payment);
        mQuantityEditText = ((TextInputLayout) findViewById(R.id.layout_feature_amount)).getEditText();
        mFreeText = ((TextInputLayout) findViewById(R.id.text_layout_feature_free_text)).getEditText();
        mLocal = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        mCurrencySymbol = Currency.getInstance(mLocal).getSymbol();
        mDummyLayout = findViewById(R.id.dummy_layout);
        mFeatureNameEditText = mLayoutFeatureName.getEditText();
        mEmailEditText = mLayoutEmail.getEditText();
        mSupplierNameEditText = mLayoutSupplierName.getEditText();
        mPaymentBalanceEditText = mLayoutPaymentBalance.getEditText();
        mAdvancePaymentEditText = mLayoutAdvancePayment.getEditText();

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
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
                // Release focus from text layouts.
                mDummyLayout.requestFocus();
                // If the data that the user entered is valid then
                // close the soft keyboard and release focus from all the text layout.
                if (isDataValid()) {

                    Utils.hideKeyboardInActivity(this);

                    mDummyLayout.requestFocus();

                    updateDatabase();

                    Toast.makeText(this, R.string.success_feature_saved, Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDatabase() {
        String featureName = mFeatureNameEditText.getText().toString();
        String paymentBalance = mPaymentBalanceEditText.getText().toString();
        String advancePayment = mAdvancePaymentEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();

        String supplierEmail = mEmailEditText.getText().toString();
        String supplierName = mSupplierNameEditText.getText().toString();
        String supplierPhone = mSupplierPhone.getText().toString();

        String supplierKey = null;
        // If the user gave a name of the supplier (which is a must if he gave
        // for him a phone number or an email) then create a record for this supplier
        if (!TextUtils.isEmpty(supplierName)){
            supplierKey = mRootReference.child(Constants.PATH_SUPPLIERS).push().getKey();
        }







    }

    private boolean isDataValid() {
        String featureName = mFeatureNameEditText.getText().toString();
        String supplierEmail = mEmailEditText.getText().toString();

        // Feature name field can't be empty, therefore if this field is empty show an error.
        if (TextUtils.isEmpty(featureName)) {
            mLayoutFeatureName.setError(getString(R.string.error_feature_name));
            mLayoutFeatureName.requestFocus();

            return false;
        }

        // If the email string is not empty and
        // pattern doesn't match a valid email pattern
        // then show an error.
        if (!TextUtils.isEmpty(supplierEmail) && !Utils.isValidEmail(supplierEmail)) {
            mLayoutEmail.setError(getString(R.string.error_chat_email_input));
            mLayoutEmail.requestFocus();

            return false;
        }
        // If the user provided a phone or an email for the supplier
        // then he must provide a name for the supplier
        if ((!TextUtils.isEmpty(mSupplierPhone.getText())
                || !TextUtils.isEmpty(mSupplierEmail.getText()))
                && TextUtils.isEmpty(mSupplierNameEditText.getText()) ){
            mLayoutSupplierName.setError(getString(R.string.error_supplier_name));
            mLayoutSupplierName.requestFocus();

            return false;
        }
        else
            mLayoutSupplierName.setError("");

        return true;

    }



    /**
     * @param stringDecimal the string to format
     * @return formatted string
     */
    private String parseStringDecimal(String stringDecimal) {
        try {
            if (!TextUtils.isEmpty(stringDecimal))
                stringDecimal =  String.format(mLocal, "%1$,.2f", BigDecimal.valueOf(Double.parseDouble(stringDecimal)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return stringDecimal;
    }


    /**
     * TextWatcher for the feature name, supplier name and supplier email
     */
    private static class FeatureTextWatcher implements TextWatcher{

        private TextInputLayout mTextInputLayout;

        public FeatureTextWatcher(TextInputLayout textInputLayout){
            mTextInputLayout = textInputLayout;

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

        }
    }


    /**
     * Format the user's input, adding thousands separators
     */
    private class NumberTextWatcher implements TextWatcher {

        private DecimalFormat df;
        private DecimalFormat dfnd;
        private boolean hasFractionalPart;

        private EditText et;

        public NumberTextWatcher(EditText et)
        {
            df = new DecimalFormat("#,###.##");
            df.setDecimalSeparatorAlwaysShown(true);
            dfnd = new DecimalFormat("#,###");
            this.et = et;
            hasFractionalPart = false;
        }

        @SuppressWarnings("unused")
        private static final String TAG = "NumberTextWatcher";

        @Override
        public void afterTextChanged(Editable s)
        {
            et.removeTextChangedListener(this);

            try {
                int inilen, endlen;
                inilen = et.getText().length();

                String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "");
                Number n = df.parse(v);
                int cp = et.getSelectionStart();
                if (hasFractionalPart) {
                    et.setText(df.format(n));
                } else {
                    et.setText(dfnd.format(n));
                }
                endlen = et.getText().length();
                int sel = (cp + (endlen - inilen));
                if (sel > 0 && sel <= et.getText().length()) {
                    et.setSelection(sel);
                } else {
                    // place cursor at the end?
                    et.setSelection(et.getText().length() - 1);
                }
            } catch (NumberFormatException | ParseException nfe) {
                nfe.printStackTrace();
                Toast.makeText(AddFeatureActivity.this, "An error occurred, check your input", Toast.LENGTH_LONG).show();
            }

            et.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator())))
            {
                hasFractionalPart = true;
            } else {
                hasFractionalPart = false;
            }
        }

    }
}