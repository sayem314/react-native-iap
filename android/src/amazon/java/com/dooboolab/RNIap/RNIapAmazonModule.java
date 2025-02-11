package com.dooboolab.RNIap;

import android.util.Log;
import androidx.annotation.Nullable;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.CoinsReward;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ObjectAlreadyConsumedException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;

public class RNIapAmazonModule extends ReactContextBaseJavaModule {
  final String TAG = "RNIapAmazonModule";

  public static final String PROMISE_BUY_ITEM = "PROMISE_BUY_ITEM";
  public static final String PROMISE_GET_PRODUCT_DATA =
    "PROMISE_GET_PRODUCT_DATA";
  public static final String PROMISE_QUERY_PURCHASES =
    "PROMISE_QUERY_PURCHASES";
  public static final String PROMISE_QUERY_AVAILABLE_ITEMS =
    "PROMISE_QUERY_AVAILABLE_ITEMS";
  public static final String PROMISE_GET_USER_DATA = "PROMISE_GET_USER_DATA";



  public RNIapAmazonModule(final ReactApplicationContext reactContext) {
    super(reactContext);

    UiThreadUtil.runOnUiThread(() -> {
              PurchasingService.registerListener(
                      reactContext,
                      new RNIapAmazonListener(reactContext)
              );
            });
      LifecycleEventListener lifecycleEventListener =
      new LifecycleEventListener() {

        @Override
        public void onHostResume() {
          PurchasingService.getUserData();
          PurchasingService.getPurchaseUpdates(false);
        }

        @Override
        public void onHostPause() {}

        @Override
        public void onHostDestroy() {}
      };

    reactContext.addLifecycleEventListener(lifecycleEventListener);
  }

  @Override
  public String getName() {
    return TAG;
  }

  @ReactMethod
  public void initConnection(final Promise promise) {
    promise.resolve(true);
  }

  @ReactMethod
  public void endConnection(final Promise promise) {
    promise.resolve(true);
  }

  @ReactMethod
  public void refreshItems(final Promise promise) {
    // TODO: Determine what needs to happen here on Amazon, if anything.
    // This is called from RNIap.consumeAllItemsAndroid()
    // Android only
    // Consume all items so they are able to buy again.
    promise.resolve(true);
  }

  @ReactMethod
  public void getUser(final Promise promise) {
    RequestId requestId = PurchasingService.getUserData();
    DoobooUtils
    .getInstance()
    .addPromiseForKey(PROMISE_GET_USER_DATA, promise);
  }

  @ReactMethod
  public void getItemsByType(
    final String type,
    final ReadableArray skuArr,
    final Promise promise
  ) {
    final Set<String> productSkus = new HashSet<String>();
    for (int ii = 0, skuSize = skuArr.size(); ii < skuSize; ii++) {
      productSkus.add(skuArr.getString(ii));
    }
    DoobooUtils
            .getInstance()
            .addPromiseForKey(PROMISE_GET_PRODUCT_DATA, promise);
    RequestId requestId = PurchasingService.getProductData(productSkus);

  }

  @ReactMethod
  public void getAvailableItemsByType(
    final String type,
    final Promise promise
  ) {
    DoobooUtils
      .getInstance()
      .addPromiseForKey(PROMISE_QUERY_AVAILABLE_ITEMS, promise);
    PurchasingService.getPurchaseUpdates(true);
  }

  @ReactMethod
  public void getPurchaseHistoryByType(
    final String type,
    final Promise promise
  ) {
    // TODO
    final WritableNativeArray items = new WritableNativeArray();
    promise.resolve(items);
  }

  @ReactMethod
  public void buyItemByType(
    final String type,
    final String sku,
    final String oldSku,
    final String purchaseToken,
    final Integer prorationMode,
    final String obfuscatedAccountId,
    final String obfuscatedProfileId,
    final Promise promise
  ) {
    DoobooUtils.getInstance().addPromiseForKey(PROMISE_BUY_ITEM, promise);
    RequestId requestId = PurchasingService.purchase(sku);
  }

  @ReactMethod
  public void acknowledgePurchase(
    final String token,
    final String developerPayLoad,
    final Promise promise
  ) {
    PurchasingService.notifyFulfillment(token, FulfillmentResult.FULFILLED);
    promise.resolve(true);
  }

  @ReactMethod
  public void consumeProduct(
    final String token,
    final String developerPayLoad,
    final Promise promise
  ) {
    PurchasingService.notifyFulfillment(token, FulfillmentResult.FULFILLED);
    promise.resolve(true);
  }

  private void sendUnconsumedPurchases(final Promise promise) {
    PurchasingService.getPurchaseUpdates(false);
    DoobooUtils
      .getInstance()
      .addPromiseForKey(PROMISE_QUERY_PURCHASES, promise);
  }

  @ReactMethod
  public void startListening(final Promise promise) {
    sendUnconsumedPurchases(promise);
  }
}
