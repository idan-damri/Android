package com.example.easywedding;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easywedding.model.Feature;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeaturesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeaturesFragment extends Fragment {

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private Intent mIntent;
    private View mFragmentLayout;
    private FloatingActionButton mFabAddFeature;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Feature, FeatureViewHolder> mAdapter;

    public static final String EXTRA_FEATURE = "com.example.easywedding.FEATURE";
    public static final String EXTRA_FEATURE_KEY = "com.example.easywedding.FEATURE_KEY";


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeaturesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeaturesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeaturesFragment newInstance(String param1, String param2) {
        FeaturesFragment fragment = new FeaturesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mFragmentLayout = inflater.inflate(R.layout.fragment_features, container, false);

        mFabAddFeature = mFragmentLayout.findViewById(R.id.fab_features);

        mFabAddFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddFeatureActivity.class));
            }
        });


        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView = mFragmentLayout.findViewById(R.id.recyclerview_features);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);


        /* FOR A DIVIDER.

        int[] ATTRS = new int[]{android.R.attr.listDivider};
        TypedArray a = getContext().obtainStyledAttributes(ATTRS);
        Drawable divider = a.getDrawable(0);
        int inset = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);
        InsetDrawable insetDivider = new InsetDrawable(divider, inset, 0, inset, 0);
        a.recycle();

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        //itemDecoration.setDrawable(insetDivider);
        mRecyclerView.addItemDecoration(itemDecoration);
        */

        setAdapter();

        return mFragmentLayout;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.features_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete_by_supplier:
                Toast.makeText(getContext(),"DELETE SUPPLIERS",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAdapter() {

        DatabaseReference featuresReference = FirebaseDatabase.getInstance()
                .getReference().child(Constants.PATH_FEATURES)
                .child(FirebaseAuth.getInstance().getUid());

        FirebaseRecyclerOptions<Feature> options = new FirebaseRecyclerOptions.Builder<Feature>()
                .setQuery(featuresReference, Feature.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Feature, FeaturesFragment.FeatureViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FeaturesFragment.FeatureViewHolder holder, final int position, @NonNull final Feature model) {
                final String featureName = model.getName();
                final String featureKey = getRef(position).getKey();

                String paymentBalance = model.getPaymentBalance();
                // TODO change this
                holder.supplierName.setVisibility(View.GONE);

                if (featureName != null)
                    holder.featureName.setText(featureName);
                if (paymentBalance != null)
                    holder.paymentBalance.setText(paymentBalance);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Pass AddFeatureActivity the model object, which relates to the current
                        // feature that the user pressed on in FeaturesFragment.
                        mIntent = new Intent(getContext(), AddFeatureActivity.class);
                        mIntent.putExtra(EXTRA_FEATURE, model);
                        mIntent.putExtra(EXTRA_FEATURE_KEY, featureKey);
                        startActivity(mIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FeaturesFragment.FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // The ViewHolder will keep references to the views in the list item view.
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_features, parent, false);
                return new FeatureViewHolder(view);
            }

        };


        // According to Google codelabs. Always scroll to bottom.
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }
    public static class FeatureViewHolder extends RecyclerView.ViewHolder {

        TextView featureName, supplierName, paymentBalance;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);

            featureName = itemView.findViewById(R.id.list_item_feature_name);
            supplierName= itemView.findViewById(R.id.list_item_supplier_name);
            paymentBalance = itemView.findViewById(R.id.list_item_balance);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAdapter != null)
            mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAdapter != null)
            mAdapter.stopListening();
    }


}