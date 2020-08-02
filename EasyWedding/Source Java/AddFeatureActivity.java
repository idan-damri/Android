package com.example.easywedding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;


import android.content.Intent;
import android.os.Bundle;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.easywedding.Utils.Utils;

import com.example.easywedding.model.Feature;
import com.example.easywedding.model.Supplier;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

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
    private TextInputLayout mLayoutSupplierPhone;
    private TextInputLayout mLayoutQuantity;
    private TextInputLayout mLayoutFreeText;

    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mSupplierEmailEditText;
    private EditText mQuantityEditText;
    private EditText mFreeTextEditText;
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

    private final static int SKIPPED_CURRENCY_SYMBOL_INDEX = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        Intent intent = getIntent();

        // Enable the up button.
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);


        setFields();

        handleLocaleSensitiveInputFields();

        if (isAddingNewFeature(intent))
            setTitle(R.string.title_add_feature);
        else
            setForEditExistingFeature(intent);

        setTextWatchers();

        takeFocusFromInputFields();

        setEndIconsInvisibleOnStartup();



    }

    private void setEndIconsInvisibleOnStartup() {
        mLayoutPaymentBalance.setEndIconVisible(false);
        mLayoutAdvancePayment.setEndIconVisible(false);
        mLayoutSupplierPhone.setEndIconVisible(false);
        mLayoutSupplierName.setEndIconVisible(false);
        mLayoutEmail.setEndIconVisible(false);
        mLayoutQuantity.setEndIconVisible(false);
        mLayoutFreeText.setEndIconVisible(false);
    }

    /**
     * The user is editing an existing feature so set the input fields with the relevant information.
     * @param intent the intent we got from FeaturesFragment.
     */
    private void setForEditExistingFeature(Intent intent) {
            Feature feature = (Feature) intent.getParcelableExtra(FeaturesFragment.EXTRA_FEATURE);
            setTitle(feature.getName());
            mFeatureNameEditText.setText(feature.getName());
            mQuantityEditText.setText(feature.getQuantity());
            mFreeTextEditText.setText(feature.getFreeText());
            if (!TextUtils.isEmpty(feature.getPaymentBalance()))
            mPaymentBalanceEditText.setText(feature.getPaymentBalance().substring(SKIPPED_CURRENCY_SYMBOL_INDEX));
            if (!TextUtils.isEmpty(feature.getAdvancePayment()))
                mAdvancePaymentEditText.setText(feature.getAdvancePayment().substring(SKIPPED_CURRENCY_SYMBOL_INDEX));

            if (intent.hasExtra(FeaturesFragment.EXTRA_SUPPLIER)) {
                Supplier supplier = (Supplier) intent.getParcelableExtra(FeaturesFragment.EXTRA_SUPPLIER);
                mSupplierNameEditText.setText(supplier.getSupplierName());
                mSupplierEmailEditText.setText(supplier.getSupplierEmail());
                mSupplierPhoneEditText.setText(supplier.getSupplierPhoneNumber());
            }
        }


    /**
     *
     * @param intent the intent we got from FeaturesFragment
     * @return true if the user is adding a new feature, false otherwise.
     */
    private boolean isAddingNewFeature(Intent intent) {
        // If the user is adding a new feature then we didn't put this extras
        // in FeaturesFragment. We checked only for the name of the feature and
        // not also for the supplier name since you can't save a a feature
        // that have a supplier name but don't have a feature name.
        return !intent.hasExtra(FeaturesFragment.EXTRA_FEATURE);
    }


    private void setTextWatchers() {
        mFeatureNameEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutFeatureName));
        mEmailEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutEmail));
        mSupplierNameEditText.addTextChangedListener(new FeatureTextWatcher(mLayoutSupplierName));
        mPaymentBalanceEditText.addTextChangedListener(new NumberTextWatcher(mPaymentBalanceEditText));
        mAdvancePaymentEditText.addTextChangedListener(new NumberTextWatcher(mAdvancePaymentEditText));
    }


    private void takeFocusFromInputFields() {
        mDummyLayout.setFocusable(true);
        mDummyLayout.setFocusableInTouchMode(true);
        mDummyLayout.requestFocus();
    }

    /**
     *
     */
    private void handleLocaleSensitiveInputFields() {
        // If the layout direction is from right to left then change the text direction
        // to be from left to right since the user enters numbers
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mLayoutPaymentBalance.setTextDirection(TextInputLayout.TEXT_DIRECTION_LTR);
            mLayoutAdvancePayment.setTextDirection(TextInputLayout.TEXT_DIRECTION_LTR);
            // Show the locale currency symbol from the left of the input
            // in a RTL layout (and not from the start as usual).
            mLayoutPaymentBalance.setSuffixText(" " + mCurrencySymbol);
            mLayoutAdvancePayment.setSuffixText(" " + mCurrencySymbol);
            // start writing the phone number from the right (in a RTL layout)
            mSupplierPhoneEditText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            mSupplierEmailEditText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            mLayoutPaymentBalance.setPrefixText(" " + mCurrencySymbol);
            mLayoutAdvancePayment.setPrefixText(" " + mCurrencySymbol);
        }

    }


    private void setFields() {
        mLayoutFeatureName = findViewById(R.id.text_layout_feature_name);
        mLayoutSupplierName = findViewById(R.id.text_layout_supplier_name);
        mLayoutEmail = findViewById(R.id.text_layout_email);
        mSupplierEmailEditText = mLayoutEmail.getEditText();
        mLayoutPaymentBalance = findViewById(R.id.text_layout_payment_balance);
        mLayoutAdvancePayment = findViewById(R.id.text_layout_advance_payment);
        mLayoutSupplierPhone = findViewById(R.id.text_layout_phone_number);
        mSupplierPhoneEditText = mLayoutSupplierPhone.getEditText();
        mLocal = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        mCurrencySymbol = Currency.getInstance(mLocal).getSymbol();
        mLocal = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
        mCurrencySymbol = Currency.getInstance(mLocal).getSymbol();
        mDummyLayout = findViewById(R.id.dummy_layout);
        mFeatureNameEditText = mLayoutFeatureName.getEditText();
        mEmailEditText = mLayoutEmail.getEditText();
        mSupplierNameEditText = mLayoutSupplierName.getEditText();
        mPaymentBalanceEditText = mLayoutPaymentBalance.getEditText();
        mAdvancePaymentEditText = mLayoutAdvancePayment.getEditText();
        mLayoutQuantity = findViewById(R.id.layout_feature_amount);
        mLayoutFreeText = findViewById(R.id.text_layout_feature_free_text);
        mQuantityEditText = mLayoutQuantity.getEditText();
        mFreeTextEditText = mLayoutFreeText.getEditText();

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

                    onBackPressed();
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
        String freeText = mFreeTextEditText.getText().toString();
        String supplierEmail = mEmailEditText.getText().toString();
        String supplierName = mSupplierNameEditText.getText().toString();
        String supplierPhone = mSupplierPhoneEditText.getText().toString();
        String supplierKey = "";
        String featureKey = "";

        // If the user gave a name of the supplier (which is a must if he gave
        // for him a phone number or an email) then create a record for this supplier
        // The key for the records of suppliers for this user is the user id.
        if (!TextUtils.isEmpty(supplierName)) {
            supplierKey = mRootReference.child(Constants.PATH_SUPPLIERS)
                    .child(mFirebaseAuth.getUid())
                    .push()
                    .getKey();

        }
        // Write the supplier record if it exists locally
        if (supplierKey != null && !TextUtils.isEmpty(supplierKey)) {
            mRootReference.child(Constants.PATH_SUPPLIERS)
                    .child(mFirebaseAuth.getUid())
                    .child(supplierKey)
                    .setValue(new Supplier(supplierName, supplierPhone, supplierEmail));
        }
        // Concat the local currency symbol with the payments
        if (!TextUtils.isEmpty(paymentBalance))
            paymentBalance = mCurrencySymbol + paymentBalance;

        if (!TextUtils.isEmpty(advancePayment))
            advancePayment = mCurrencySymbol + advancePayment;
        // Create the feature record

        Feature feature = new Feature(
                featureName,
                supplierKey,
                freeText,
                advancePayment,
                paymentBalance,
                quantity);
        // Write the record to the db
        mRootReference.child(Constants.PATH_FEATURES)
                .child(mFirebaseAuth.getUid())
                .push()
                .setValue(feature);

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
        if ((!TextUtils.isEmpty(mSupplierPhoneEditText.getText())
                || !TextUtils.isEmpty(mSupplierEmailEditText.getText()))
                && TextUtils.isEmpty(mSupplierNameEditText.getText())) {
            mLayoutSupplierName.setError(getString(R.string.error_supplier_name));
            mLayoutSupplierName.requestFocus();

            return false;
        } else
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
                stringDecimal = String.format(mLocal, "%1$,.2f", BigDecimal.valueOf(Double.parseDouble(stringDecimal)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return stringDecimal;
    }


    /**
     * TextWatcher for the feature name, supplier name and supplier email
     */
    private static class FeatureTextWatcher implements TextWatcher {

        private TextInputLayout mTextInputLayout;

        public FeatureTextWatcher(TextInputLayout textInputLayout) {
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

        public NumberTextWatcher(EditText et) {
            df = new DecimalFormat("#,###.##");
            df.setDecimalSeparatorAlwaysShown(true);
            dfnd = new DecimalFormat("#,###");
            this.et = et;
            hasFractionalPart = false;
        }

        @SuppressWarnings("unused")
        private static final String TAG = "NumberTextWatcher";

        @Override
        public void afterTextChanged(Editable s) {
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
                // We'll get here if the edittext's text is empty and that's ok.
                if (!TextUtils.isEmpty(et.getText())) {
                    nfe.printStackTrace();
                    Toast.makeText(AddFeatureActivity.this, "An error occurred, check your input", Toast.LENGTH_LONG).show();
                }

            }

            et.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))) {
                hasFractionalPart = true;
            } else {
                hasFractionalPart = false;
            }
        }

    }
}