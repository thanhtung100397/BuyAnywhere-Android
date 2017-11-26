package com.uides.buyanywhere.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.hedgehog.ratingbar.RatingBar;
import com.uides.buyanywhere.Constant;
import com.uides.buyanywhere.R;
import com.uides.buyanywhere.auth.UserAuth;
import com.uides.buyanywhere.custom_view.PriceTextView;
import com.uides.buyanywhere.custom_view.dialog.RatingDialog;
import com.uides.buyanywhere.custom_view.StrikeThroughPriceTextView;
import com.uides.buyanywhere.model.Product;
import com.uides.buyanywhere.network.Network;
import com.uides.buyanywhere.service.cart.AddCartService;
import com.uides.buyanywhere.service.cart.DeleteCartService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by TranThanhTung on 19/11/2017.
 */

public class ProductDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProductDetailActivity";
    @BindView(R.id.image_slider)
    SliderLayout imageSlider;
    @BindView(R.id.txt_name)
    TextView textName;
    @BindView(R.id.txt_shop_name)
    TextView textShop;
    @BindView(R.id.txt_quantity)
    TextView textQuantity;
    @BindView(R.id.txt_created_date)
    TextView textCreatedDate;
    @BindView(R.id.txt_current_price)
    PriceTextView textCurrentPrice;
    @BindView(R.id.txt_origin_price)
    StrikeThroughPriceTextView textOriginPrice;
    @BindView(R.id.txt_discount_percentage)
    TextView textDiscountPercentage;
    @BindView(R.id.rating_bar)
    RatingBar ratingBar;
    @BindView(R.id.txt_rating_score)
    TextView textRatingScore;
    @BindView(R.id.txt_rating_count)
    TextView textRatingCount;
    @BindView(R.id.txt_description)
    TextView textDescription;
    @BindView(R.id.img_discount)
    ImageView imageDiscount;
    @BindView(R.id.btn_rating)
    Button ratingButton;
    @BindView(R.id.btn_purchase)
    Button purchaseButton;
    @BindView(R.id.fab_shopping_cart)
    FloatingActionButton addToCartFab;

    private Product product;
    private MenuItem addToCartButton;
    private AddCartService addCartService;
    private DeleteCartService deleteCartService;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ButterKnife.bind(this);
        initService();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            product = (Product) bundle.getSerializable(Constant.PRODUCT);
        }
        showViews(product);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    private void initService() {
        compositeDisposable = new CompositeDisposable();
        addCartService = Network.getInstance().createService(AddCartService.class);
        deleteCartService = Network.getInstance().createService(DeleteCartService.class);
    }

    private void showViews(Product product) {
        initToolBar();
        initFloatingButton();
        initImageSlider(product.getDescriptiveImageUrl());

        textName.setText(product.getName());
        textShop.setText(product.getShopName());
        textQuantity.setText("" + product.getQuantity());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DAY_MONTH_YEAR, Locale.US);
        textCreatedDate.setText(simpleDateFormat.format(product.getCreatedDate()));
        long currentPrice = product.getCurrentPrice();
        textCurrentPrice.setPrice("" + currentPrice, Constant.PRICE_UNIT);
        long originPrice = product.getOriginPrice();
        if (currentPrice < originPrice) {
            int discountPercentage = Math.round((originPrice - currentPrice) * 1.0f / originPrice * 100);
            textOriginPrice.setPrice("" + originPrice, Constant.PRICE_UNIT);
            textDiscountPercentage.setText("-" + discountPercentage + "%");
        } else {
            textOriginPrice.setVisibility(View.INVISIBLE);
            textDiscountPercentage.setVisibility(View.INVISIBLE);
            imageDiscount.setVisibility(View.INVISIBLE);
        }
        float productRating = product.getRating();
        ratingBar.setStar(productRating);
        textRatingScore.setText("" + productRating);
        textRatingCount.setText("" + product.getRatingCount());
        textDescription.setText(product.getDescription());

        ratingButton.setOnClickListener(this);
        purchaseButton.setOnClickListener(this);
    }

    private void initToolBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addToCartButton = toolbar.getMenu().findItem(R.id.action_add_to_cart);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initFloatingButton() {
        addToCartFab.setOnClickListener(this);

        if (product.isAddedToCart()) {
            addToCartFab.setImageResource(R.drawable.ic_added_to_cart);
        }
    }

    private void initImageSlider(List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }
        for (String imageUrl : imageUrls) {
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView.image(imageUrl)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            imageSlider.addSlider(textSliderView);
        }

        imageSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        imageSlider.setCustomAnimation(new DescriptionAnimation());
        imageSlider.setDuration(Constant.SLIDE_DURATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            CollapsingToolbarLayout collapsing_toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
            collapsing_toolbar_layout.setExpandedTitleTextColor(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail_tool_bar, menu);
        addToCartButton = menu.findItem(R.id.action_add_to_cart);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
            }
            break;

            case R.id.action_add_to_cart: {
                if (product.isAddedToCart()) {
                    removeProductFromCart(product);
                } else {
                    addProductToCart(product);
                }
            }
            break;

            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_shopping_cart: {
                if (product.isAddedToCart()) {
                    removeProductFromCart(product);
                } else {
                    addProductToCart(product);
                }
            }
            break;

            case R.id.btn_rating: {
                new RatingDialog(this)
                        .setOnPositiveButtonClickListener((rating, textFeedback) -> {
                            //TODO send rating
                            Toast.makeText(ProductDetailActivity.this,
                                    R.string.feedback_sent,
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }).show();
            }
            break;

            case R.id.btn_purchase: {
                //TODO purchasing product logic
            }
            break;

            default: {
                break;
            }
        }
    }

    private void removeProductFromCart(Product product) {
        Disposable disposable = deleteCartService
                .deleteProductFromCart(UserAuth.getAuthUser().getAccessToken(), product.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> onRemoveProductFromCartSuccess(), this::onCartRequestFailed);
        compositeDisposable.add(disposable);
    }

    private void addProductToCart(Product product) {
        Disposable disposable = addCartService
                .addProductToCart(UserAuth.getAuthUser().getAccessToken(), product.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> onAddProductToCartSuccess(), this::onCartRequestFailed);
        compositeDisposable.add(disposable);
    }

    private void onCartRequestFailed(Throwable e) {
        Log.i(TAG, "onCartRequestFailed: " + e);
        Toast.makeText(this, R.string.unexpected_error_message, Toast.LENGTH_SHORT).show();
    }

    void onAddProductToCartSuccess() {
        product.setAddedToCart(true);
        Toast.makeText(this, R.string.add_product_success, Toast.LENGTH_SHORT).show();
        addToCartButton.setIcon(R.drawable.ic_added_to_cart);
        addToCartFab.setImageResource(R.drawable.ic_added_to_cart);
    }

    void onRemoveProductFromCartSuccess() {
        product.setAddedToCart(false);
        Toast.makeText(this, R.string.remove_product_success, Toast.LENGTH_SHORT).show();
        addToCartButton.setIcon(R.drawable.ic_add_to_cart);
        addToCartFab.setImageResource(R.drawable.ic_add_to_cart);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constant.CART_REMOVED, !product.isAddedToCart());
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }
}
