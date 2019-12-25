package ceui.lisa.fragments;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;

import java.util.ArrayList;
import java.util.List;

import ceui.lisa.R;
import ceui.lisa.activities.TemplateActivity;
import ceui.lisa.adapters.NHAdapter;
import ceui.lisa.databinding.FragmentLikeIllustHorizontalBinding;
import ceui.lisa.http.NullCtrl;
import ceui.lisa.http.Retro;
import ceui.lisa.interfaces.OnItemClickListener;
import ceui.lisa.model.ListNovelResponse;
import ceui.lisa.model.NovelBean;
import ceui.lisa.model.UserDetailResponse;
import ceui.lisa.utils.DataChannel;
import ceui.lisa.utils.DensityUtil;
import ceui.lisa.utils.Params;
import ceui.lisa.view.LinearItemHorizontalDecoration;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static ceui.lisa.activities.Shaft.sUserModel;

public class FragmentLikeNovelHorizontal extends BaseBindFragment<FragmentLikeIllustHorizontalBinding> {

    private List<NovelBean> allItems = new ArrayList<>();
    private UserDetailResponse mUserDetailResponse;
    private NHAdapter mAdapter;
    private int type; // 0某人收藏的小说，1某人创作的小说

    public static FragmentLikeNovelHorizontal newInstance(UserDetailResponse userDetailResponse, int pType) {
        FragmentLikeNovelHorizontal fragmentLikeIllustHorizontal = new FragmentLikeNovelHorizontal();
        fragmentLikeIllustHorizontal.mUserDetailResponse = userDetailResponse;
        fragmentLikeIllustHorizontal.type = pType;
        return fragmentLikeIllustHorizontal;
    }

    @Override
    void initLayout() {
        mLayoutID = R.layout.fragment_like_illust_horizontal;
    }

    @Override
    public void initView(View view) {
        baseBind.rootParentView.setVisibility(View.GONE);
        baseBind.recyclerView.addItemDecoration(new
                LinearItemHorizontalDecoration(DensityUtil.dp2px(8.0f)));
        if (type == 1) {
            baseBind.title.setText("小说作品");
            baseBind.howMany.setText(String.format(getString(R.string.how_many_illust_works),
                    mUserDetailResponse.getProfile().getTotal_novels()));
            baseBind.howMany.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, TemplateActivity.class);
                    intent.putExtra(TemplateActivity.EXTRA_FRAGMENT,
                            baseBind.title.getText().toString());
                    intent.putExtra(Params.USER_ID, mUserDetailResponse.getUser().getId());
                    startActivity(intent);
                }
            });
        } else if (type == 0) {
            baseBind.title.setText("小说收藏");
            baseBind.howMany.setText("查看全部");
            baseBind.howMany.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, TemplateActivity.class);
                    intent.putExtra(TemplateActivity.EXTRA_FRAGMENT,
                            baseBind.title.getText().toString());
                    intent.putExtra(Params.USER_ID, mUserDetailResponse.getUser().getId());
                    startActivity(intent);
                }
            });
        }
        LinearLayoutManager manager = new LinearLayoutManager(
                mContext, LinearLayoutManager.HORIZONTAL, false);
        baseBind.recyclerView.setLayoutManager(manager);
        baseBind.recyclerView.setHasFixedSize(true);
        mAdapter = new NHAdapter(allItems, mContext);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int viewType) {
                DataChannel.get().setNovelList(allItems);
                Intent intent = new Intent(mContext, TemplateActivity.class);
                intent.putExtra(Params.INDEX, position);
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "小说详情");
                intent.putExtra("hideStatusBar", true);
                startActivity(intent);
            }
        });
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(baseBind.recyclerView);
        baseBind.recyclerView.setAdapter(mAdapter);
        ViewGroup.LayoutParams layoutParams = baseBind.recyclerView.getLayoutParams();
        layoutParams.width = MATCH_PARENT;
        layoutParams.height = DensityUtil.dp2px(180.0f) + mContext.getResources()
                .getDimensionPixelSize(R.dimen.sixteen_dp);
        baseBind.recyclerView.setLayoutParams(layoutParams);

    }

    @Override
    void initData() {
        Observable<ListNovelResponse> mApi = null;
        if (type == 0) {
            mApi = Retro.getAppApi().getUserLikeNovel(sUserModel.getResponse().getAccess_token(),
                    mUserDetailResponse.getUser().getId(), FragmentLikeIllust.TYPE_PUBLUC);
        } else {
            mApi = Retro.getAppApi().getUserSubmitNovel(sUserModel.getResponse().getAccess_token(),
                    mUserDetailResponse.getUser().getId());
        }
        mApi.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NullCtrl<ListNovelResponse>() {
                    @Override
                    public void success(ListNovelResponse listNovelResponse) {
                        if (listNovelResponse.getList().size() > 0) {
                            allItems.clear();
                            if (listNovelResponse.getList().size() > 10) {
                                allItems.addAll(listNovelResponse.getList().subList(0, 10));
                            } else {
                                allItems.addAll(listNovelResponse.getList());
                            }
                            mAdapter.notifyItemRangeInserted(0, allItems.size());
                            baseBind.rootParentView.setVisibility(View.VISIBLE);
                            Animation animation = new AlphaAnimation(0.0f, 1.0f);
                            animation.setDuration(800L);
                            baseBind.rootParentView.startAnimation(animation);
                        }
                    }
                });
    }
}
