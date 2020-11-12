/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpack.scriptmanager.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 12, 2020
 */

public class BillingActivity extends AppCompatActivity {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();
    private BillingClient mBillingClient;
    private boolean mClientInitialized = false;
    private List<String> mSkuList = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatTextView mCancel = findViewById(R.id.cancel_button);

        mData.add(new RecycleViewItem(getString(R.string.support_app), getResources().getDrawable(R.drawable.ic_donate)));
        mData.add(new RecycleViewItem(getString(R.string.support_coffee), getResources().getDrawable(R.drawable.ic_coffee)));
        mData.add(new RecycleViewItem(getString(R.string.support_meal), getResources().getDrawable(R.drawable.ic_meal)));
        mData.add(new RecycleViewItem(getString(R.string.support_dinner), getResources().getDrawable(R.drawable.ic_dinner)));

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (position == 0) {
                buyDonationApp();
            } else if (position == 1) {
                buyMeACoffee();
            } else if (position == 2) {
                buyMeAMeal();
            } else if (position == 3) {
                buyMeADinner();
            }
        });

        mBack.setOnClickListener(v -> super.onBackPressed());
        mCancel.setOnClickListener(v -> super.onBackPressed());

        mBillingClient = BillingClient.newBuilder(BillingActivity.this).enablePendingPurchases().setListener((billingResult, list) -> {
            if (list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (Purchase purchase : list) {
                    handlePurchases(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.support_retry_message));
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.support_already_received_message));
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    mClientInitialized = true;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Utils.snackbar(findViewById(android.R.id.content), getString(R.string.billing_client_disconnected));
            }
        });
    }

    private void buyDonationApp() {
        if (!Utils.isNotDonated(this)) {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.support_already_received_message));
            return;
        }
        Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.donate", this);
    }

    private void buyMeACoffee() {
        if (!mClientInitialized) {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.billing_client_disconnected));
            return;
        }
        mSkuList.clear();
        mSkuList.add("donation_coffee");
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(mSkuList).setType(BillingClient.SkuType.INAPP);

        mBillingClient.querySkuDetailsAsync(params.build(), (billingResult, list) -> {
            if (list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (final SkuDetails skuDetails : list) {

                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build();

                    mBillingClient.launchBillingFlow(BillingActivity.this, flowParams);

                }
            }
        });
    }

    private void buyMeADinner() {
        if (!mClientInitialized) {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.billing_client_disconnected));
            return;
        }
        mSkuList.clear();
        mSkuList.add("donation_dinner");
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(mSkuList).setType(BillingClient.SkuType.INAPP);

        mBillingClient.querySkuDetailsAsync(params.build(), (billingResult, list) -> {
            if (list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (final SkuDetails skuDetails : list) {

                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build();

                    mBillingClient.launchBillingFlow(BillingActivity.this, flowParams);

                }
            }
        });
    }

    private void buyMeAMeal() {
        if (!mClientInitialized) {
            Utils.snackbar(findViewById(android.R.id.content), getString(R.string.billing_client_disconnected));
            return;
        }
        mSkuList.clear();
        mSkuList.add("donation_meal");
        final SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(mSkuList).setType(BillingClient.SkuType.INAPP);

        mBillingClient.querySkuDetailsAsync(params.build(), (billingResult, list) -> {
            if (list != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (final SkuDetails skuDetails : list) {

                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build();

                    mBillingClient.launchBillingFlow(BillingActivity.this, flowParams);

                }
            }
        });
    }

    private void handlePurchases(Purchase purchase) {
        try {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (purchase.getSku().equals("donation_coffee") || purchase.getSku().equals("donation_meal") || purchase.getSku().equals("donation_dinner")) {
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    ConsumeResponseListener mConsumeResponseListener = (billingResult, s) -> Utils.snackbar(findViewById(android.R.id.content), getString(R.string.support_acknowledged));

                    mBillingClient.consumeAsync(consumeParams, mConsumeResponseListener);
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(getString(R.string.support_received_message))
                            .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .show();
                }
            }
        } catch (Exception ignored) {}
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private ArrayList<RecycleViewItem> data;

        private static ClickListener clickListener;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_donate, parent, false);
            return new ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            try {
                holder.mTitle.setText(this.data.get(position).getTitle());
                holder.mIcon.setImageDrawable(this.data.get(position).getIcon());
            } catch (NullPointerException ignored) {}
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageView mIcon;
            private AppCompatTextView mTitle;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
                this.mTitle = view.findViewById(R.id.title);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        public void setOnItemClickListener(ClickListener clickListener) {
            RecycleViewAdapter.clickListener = clickListener;
        }

        public interface ClickListener {
            void onItemClick(int position, View v);
        }

    }

    private static class RecycleViewItem implements Serializable {
        private String mTitle;
        private Drawable mIcon;

        public RecycleViewItem(String title, Drawable icon) {
            this.mTitle = title;
            this.mIcon = icon;
        }

        public String getTitle() {
            return mTitle;
        }

        public Drawable getIcon() {
            return mIcon;
        }
    }

}