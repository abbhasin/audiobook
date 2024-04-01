package com.enigma.audiobook.adapters;

import static com.enigma.audiobook.activities.GodPageActivity.GOD_ID_KEY;
import static com.enigma.audiobook.activities.MandirPageActivity.MANDIR_ID_KEY;
import static com.enigma.audiobook.activities.PujariPageActivity.INFLUENCER_ID_KEY;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.enigma.audiobook.R;
import com.enigma.audiobook.activities.DarshanActivity;
import com.enigma.audiobook.activities.FirebaseUIActivity;
import com.enigma.audiobook.activities.GodPageActivity;
import com.enigma.audiobook.activities.MandirPageActivity;
import com.enigma.audiobook.activities.MyDetails;
import com.enigma.audiobook.backend.models.responses.Page;
import com.enigma.audiobook.models.MorePageModel;

import java.util.List;

public class MorePagesCardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    RequestManager requestManager;
    List<MorePageModel> menuItemModels;
    Context context;

    public MorePagesCardViewAdapter(RequestManager requestManager, List<MorePageModel> menuItemModels, Context context) {
        this.requestManager = requestManager;
        this.menuItemModels = menuItemModels;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MorePagesCardViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.card_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MorePagesCardViewHolder) holder).onBind(menuItemModels.get(position), requestManager);
        ((MorePagesCardViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(((MorePagesCardViewHolder) holder).getPage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItemModels.size();
    }

    private void launchActivity(Page page) {
        Intent i;
        switch (page.getPageType()) {
            case GOD:
                i = new Intent(context, GodPageActivity.class);
                i.putExtra(GOD_ID_KEY, page.getMyGodPageInfo().getGodId());
                context.startActivity(i);
                return;
            case MANDIR:
                i = new Intent(context, MandirPageActivity.class);
                i.putExtra(MANDIR_ID_KEY, page.getMyMandirPageInfo().getMandirId());
                context.startActivity(i);
                return;
            case INFLUENCER:
                i = new Intent(context, DarshanActivity.class);
                i.putExtra(INFLUENCER_ID_KEY, page.getMyInfluencerPageInfo().getInfluencerId());
                context.startActivity(i);
                return;
            case SING_IN:
                i = new Intent(context, FirebaseUIActivity.class);
                context.startActivity(i);
                return;
            case MY_DETAILS:
                i = new Intent(context, MyDetails.class);
                context.startActivity(i);
                return;
            default:
                throw new IllegalStateException("PageType not supported:" + page.getPageType());
        }
    }

    public static class MorePagesCardViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView image;
        private Page page;
        private CardView cardView;

        public MorePagesCardViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.cardViewMenuItemText);
            image = itemView.findViewById(R.id.cardViewMenuItemImage);
            cardView = itemView.findViewById(R.id.cardViewMenuItem);
        }

        public void onBind(MorePageModel menuItemModel, RequestManager requestManager) {
            this.textView.setText(menuItemModel.getPage().getTitle());
            this.page = menuItemModel.getPage();
            switch (menuItemModel.getPage().getPageType()) {
                case SING_IN:
                case MY_DETAILS:
                    requestManager
                            .load(menuItemModel.getDrawableResourceId())
                            .into(image);
                    break;
                default:
                    requestManager
                            .load(menuItemModel.getPage().getImageUrl())
                            .into(image);
                    break;
            }

        }

        public Page getPage() {
            return page;
        }
    }
}
