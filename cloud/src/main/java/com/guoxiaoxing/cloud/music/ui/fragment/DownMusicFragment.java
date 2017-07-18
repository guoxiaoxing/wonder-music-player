package com.guoxiaoxing.cloud.music.ui.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guoxiaoxing.cloud.music.R;
import com.guoxiaoxing.cloud.music.handler.HandlerUtil;
import com.guoxiaoxing.cloud.music.magicasakura.widgets.TintImageView;
import com.guoxiaoxing.cloud.music.model.info.MusicInfo;
import com.guoxiaoxing.cloud.music.ui.BaseFragment;
import com.guoxiaoxing.cloud.music.ui.activity.SelectActivity;
import com.guoxiaoxing.cloud.music.service.MusicPlayer;
import com.guoxiaoxing.cloud.music.uitl.IConstants;
import com.guoxiaoxing.cloud.music.uitl.MusicUtils;
import com.guoxiaoxing.cloud.music.widget.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wm on 2016/1/18.
 */
public class DownMusicFragment extends BaseFragment {
    private String folder_path;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private FolderDetailAdapter folderDetailAdapter;

    public static DownMusicFragment newInstance(String id, boolean useTransition, String transitionName) {
        DownMusicFragment fragment = new DownMusicFragment();
        Bundle args = new Bundle();
        args.putString("folder_path", id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            folder_path = getArguments().getString("folder_path");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.down_music_recylerview, container, false);

        layoutManager = new LinearLayoutManager(mContext);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        folderDetailAdapter = new FolderDetailAdapter(null);
        recyclerView.setAdapter(folderDetailAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();
        reloadAdapter();


        return view;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAdapter();
    }

    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                boolean hasFolder = false;
                File file = new File(folder_path);
                if(!file.exists()){
                    hasFolder = file.mkdirs();
                }else {
                    hasFolder = true;
                }
                if(hasFolder){
                    List<MusicInfo> albumList = MusicUtils.queryMusic(mContext, folder_path, IConstants.START_FROM_FOLDER);
                    folderDetailAdapter.updateDataSet(albumList);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                folderDetailAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    class FolderDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        List<MusicInfo> mList;

        public FolderDetailAdapter(List<MusicInfo> musicInfos) {
            mList = musicInfos;
            //list.add(0,null);
        }

        //更新adpter的数据
        public void updateDataSet(List<MusicInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));
            } else {
                return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("共" + mList.size() + "首");

                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", (ArrayList) mList);
                        mContext.startActivity(intent);
                    }
                });
            }
            if (holder instanceof ListItemViewHolder) {
                MusicInfo musicInfo = mList.get(position - 1);
                ((ListItemViewHolder) holder).mainTitle.setText(musicInfo.musicName);
                ((ListItemViewHolder) holder).title.setText(musicInfo.artist);
                //判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == musicInfo.songId) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                    ((ListItemViewHolder) holder).playState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() + 1 : 0);
        }

        ;


        class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.play_all_number);
                this.select = (ImageView) view.findViewById(R.id.select);
                view.setOnClickListener(this);
            }

            //播放文件夹
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mList.size()];
                        HashMap<Long, MusicInfo> infos = new HashMap();
                        for (int i = 0; i < mList.size(); i++) {
                            MusicInfo info = mList.get(i);
                            list[i] = info.songId;
                            info.islocal = true;
                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                            infos.put(list[i], mList.get(i));
                        }
                        MusicPlayer.playAll(infos, list, 0, false);
                    }
                },70);
            }


        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView moreOverflow;
            TextView mainTitle, title;
            TintImageView playState;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = (TintImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);

                //设置弹出菜单
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = new MoreFragment().newInstance(mList.get(getAdapterPosition() - 1), IConstants.MUSICOVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            //播放歌曲
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mList.size()];
                        HashMap<Long, MusicInfo> infos = new HashMap();
                        for (int i = 0; i < mList.size(); i++) {
                            MusicInfo info = mList.get(i);
                            list[i] = info.songId;
                            info.islocal = true;
                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                            infos.put(list[i], mList.get(i));
                        }
                        if (getAdapterPosition() > 0)
                            MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
                    }
                }, 60);
            }

        }

    }
}
