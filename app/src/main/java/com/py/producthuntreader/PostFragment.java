package com.py.producthuntreader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.py.producthuntreader.model.Post;
import com.py.producthuntreader.model.VolleySingleton;

import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Posts.
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 */
public class PostFragment extends Fragment {

    private RecyclerView mPostRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PostRecyclerViewAdapter mPostAdapter;
    private OnListFragmentInteractionListener mListener;

    private String mCategoryTitle = "tech";

    private Post[] mPosts;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                if (mListener != null) {
                    mListener.onListSwipeUpdate();
                }
            }
        });

        mPostRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycle_list);
        //after creation we must set LayoutManager
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCategoryTitle = bundle.getString("cat_name");
        }

        //set new data to Adapter and update RecyclerView
        updateUI();

        return view;
    }

    /** get data, set new Adapter, insert in RecyclerView. */
    public void updateUI() {
        if (mPosts == null) {
            return;
        }

        List<Post> posts = Arrays.asList(mPosts);

        if (mPostAdapter == null) {
            mPostAdapter = new PostRecyclerViewAdapter(posts, mListener);
            mPostRecyclerView.setAdapter(mPostAdapter);
        } else {
            //update
            mPostAdapter.notifyDataSetChanged();
        }
        //if was swipe
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void setPosts(Post[] posts) {
        mPosts = posts;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    /** Interaction with activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Post post);
        void onListSwipeUpdate();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnListFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

    }

    /** Holder class. */
    public class PostHolder extends RecyclerView.ViewHolder {

        /** model. */
        private View mView;

        /** views. */
        private NetworkImageView mNetworkImageView;
        private TextView mNameView;
        private TextView mTagline;
        private TextView mVotes;
        private Post mItem;

        public PostHolder(View view) {
            super(view);

            mView = view;
            mNetworkImageView = (NetworkImageView) view.findViewById(R.id.post_image_small);
            mNameView = (TextView) view.findViewById(R.id.post_title);
            mTagline = (TextView) view.findViewById(R.id.post_tagline);
            mVotes = (TextView)  view.findViewById(R.id.post_votes);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }

    /** Adapter class */
    public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostHolder> {

        private final List<Post> mValues;
        private final OnListFragmentInteractionListener mListener;

        public PostRecyclerViewAdapter(List<Post> items, OnListFragmentInteractionListener listener) {
            mValues = items;
            mListener = listener;
        }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_post, parent, false);
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(final PostHolder holder, int position) {
            holder.mItem = mValues.get(position);

            String title = getString(R.string.holder_title) + " " + mValues.get(position).getName();
            holder.mNameView.setText(title);
            String tagline = mValues.get(position).getTagline().substring(0,20) + "...";
            holder.mTagline.setText(tagline);
            String votes = getString(R.string.holder_votes) + " " + mValues.get(position).getVotes_count();
            holder.mVotes.setText(votes);

            holder.mNetworkImageView.setDefaultImageResId(R.drawable.image_view_small_blank);
            holder.mNetworkImageView.setErrorImageResId(R.drawable.image_view_small_error);
            holder.mNetworkImageView.setImageUrl(holder.mItem.getThumbnail(), VolleySingleton.getInstance().getImageLoader());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

    }
}
