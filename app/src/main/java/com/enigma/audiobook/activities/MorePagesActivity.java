package com.enigma.audiobook.activities;

import static com.enigma.audiobook.proxies.adapters.ModelAdapters.convertMorePages;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.adapters.MorePagesCardViewAdapter;
import com.enigma.audiobook.backend.models.responses.MorePages;
import com.enigma.audiobook.backend.models.responses.Page;
import com.enigma.audiobook.backend.models.responses.PageType;
import com.enigma.audiobook.models.MorePageModel;
import com.enigma.audiobook.proxies.MorePagesService;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.utils.NavigationUtils;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.enigma.audiobook.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MorePagesActivity extends AppCompatActivity {

    private String userId;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_pages);

        userId = SharedPreferencesHandler.getUserId(this).get();

        setupNavigation();

        intiRecyclerView();
    }

    private void intiRecyclerView() {
        recyclerView = findViewById(R.id.menuRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        getAndSetMorePages();

    }

    private void getAndSetMorePages() {
        MorePagesService morePagesService = RetrofitFactory.getInstance().createService(MorePagesService.class);
        Call<MorePages> morePagesCall = morePagesService.getMorePages(userId);
        List<MorePageModel> morePageModels = new ArrayList<>();
        RetryHelper.enqueueWithRetry(morePagesCall, new Callback<MorePages>() {
            @Override
            public void onResponse(Call<MorePages> call, Response<MorePages> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MorePagesActivity.this, "unable to load more pages", Toast.LENGTH_SHORT).show();
                    addStaticModelsAndUpdate(morePageModels);
                    return;
                }

                morePageModels.addAll(convertMorePages(response.body()));
                addStaticModelsAndUpdate(morePageModels);
            }

            @Override
            public void onFailure(Call<MorePages> call, Throwable t) {
                Toast.makeText(MorePagesActivity.this, "unable to load more pages", Toast.LENGTH_SHORT).show();
                addStaticModelsAndUpdate(morePageModels);
            }
        });
    }

    private void addStaticModelsAndUpdate(List<MorePageModel> morePageModels) {
        addStaticPages(morePageModels);

        MorePagesCardViewAdapter adapter =
                new MorePagesCardViewAdapter(initGlide(),
                        morePageModels,
                        MorePagesActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private void addStaticPages(List<MorePageModel> morePageModels) {
        Page signInPage = new Page();
        signInPage.setTitle("Sign-In");
        signInPage.setImageUrl("");
        signInPage.setPageType(PageType.SING_IN);

        morePageModels.add(new MorePageModel(signInPage, R.drawable.login_icon));

        Page myDetailsPage = new Page();
        myDetailsPage.setTitle("My Details");
        myDetailsPage.setImageUrl("");
        myDetailsPage.setPageType(PageType.MY_DETAILS);

        morePageModels.add(new MorePageModel(myDetailsPage, R.drawable.contact_icon));
    }

    private void setupNavigation() {
        bottomNavigationView = NavigationUtils.setupNavigationDrawer(
                this,
                R.id.morePagesBottomNavigation,
                R.id.menuItemMore);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NavigationUtils.setMenuItemChecked(bottomNavigationView, R.id.menuItemMore);
    }

    private RequestManager initGlide() {
        return Utils.initGlide(this);
    }
}