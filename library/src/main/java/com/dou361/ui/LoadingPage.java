package com.dou361.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dou361.pool.ThreadManager;
import com.dou361.utils.UIUtils;
import com.jingwang.eluxue_online.R;

/**
 * created by jjdxm on 2015-2-24 下午1:06:04 说明 请求网络时显示的页面
 **/
public abstract class LoadingPage extends FrameLayout {

	/** 默认状态 */
	private final int STATE_DEFAULt = 1;
	/** 加载状态 */
	private final int STATE_LONDING = 2;
	/** 错误状态 */
	private final int STATE_ERROR = 3;
	/** 空状态 */
	private final int STATE_EMPTY = 4;
	/** 成功状态 */
	public final static int STATE_SUCCESS = 5;

	/** 记录当前状态 */
	public int mState;

	/** 加载中的View */
	private View mLoadingView;
	/** 错误的View */
	private View mErrorView;
	/** 空的View */
	private View mEmptyView;
	/** 成功的View */
	private View mSuccessView;

	public LoadingPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LoadingPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LoadingPage(Context context) {
		super(context);
		init();
	}

	/** 初始化Page */
	private void init() {
		mState = STATE_DEFAULt;
		if (mLoadingView == null) {
			mLoadingView = createLoadingView();
			addView(mLoadingView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
		if (mErrorView == null) {
			mErrorView = createErrorView();
			addView(mErrorView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
		if (mEmptyView == null) {
			mEmptyView = createEmptyView();
			addView(mEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}

		showSafePage();
	}

	/** 安全显示当前状态对应的页面 */
	private void showSafePage() {
		UIUtils.runInMainThread(new Runnable() {

			@Override
			public void run() {
				showPage();
			}

		});
	}

	/** 显示当前状态对应的页面 */
	private void showPage() {
		if (mLoadingView != null) {
			mLoadingView.setVisibility(mState == STATE_DEFAULt
					|| mState == STATE_LONDING ? View.VISIBLE : View.INVISIBLE);
		}
		if (mErrorView != null) {
			mErrorView.setVisibility(mState == STATE_ERROR ? View.VISIBLE
					: View.INVISIBLE);
		}
		if (mEmptyView != null) {
			mEmptyView.setVisibility(mState == STATE_EMPTY ? View.VISIBLE
					: View.INVISIBLE);
		}

		if (mState == STATE_SUCCESS && mSuccessView == null) {
			mSuccessView = createSuccessView();
			addView(mSuccessView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}

		if (mSuccessView != null) {
			mSuccessView.setVisibility(mState == STATE_SUCCESS ? View.VISIBLE
					: View.INVISIBLE);
		}
	}

	/** 创建加载中的View */
	private View createLoadingView() {
		View view = UIUtils.inflate(R.layout.loading_page_loading);
		ImageView pp = (ImageView) view.findViewById(R.id.pp);
		((AnimationDrawable) pp.getDrawable()).start();
		return view;
	}

	/** 创建加载失败的View */
	private View createErrorView() {
		View view = UIUtils.inflate(R.layout.loading_page_error);
		view.findViewById(R.id.ll_content).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						show();
					}
				});
		return view;
	}

	/** 创建加载成功但数据为空的View */
	private View createEmptyView() {
		return UIUtils.inflate(R.layout.loading_page_empty);
	}

	/** 创建成功状态的View */
	public abstract View createSuccessView();

	/** 加载数据 */
	public abstract LoadResult load();

	/** 显示页面 */
	public synchronized void show() {
		if (mState == STATE_ERROR || mState == STATE_EMPTY) {
			mState = STATE_DEFAULt;
		}
		if (mState == STATE_DEFAULt) {
			mState = STATE_LONDING;
			LoadingTask task = new LoadingTask();
			ThreadManager.getLongPool().execute(task);
		}
		showSafePage();
	}

	/** 重新显示页面 */
	public synchronized void reShow() {
		if (mState == STATE_ERROR || mState == STATE_EMPTY
				|| mState == STATE_SUCCESS) {
			mState = STATE_DEFAULt;
		}
		if (mSuccessView != null) {
			removeView(mSuccessView);
		}
		mSuccessView = null;
		if (mState == STATE_DEFAULt) {
			mState = STATE_LONDING;
			LoadingTask task = new LoadingTask();
			ThreadManager.getLongPool().execute(task);
		}
		showSafePage();
	}

	private class LoadingTask implements Runnable {

		@Override
		public void run() {
			final LoadResult mLoadResult = load();
			if (mLoadResult != null) {
				mState = mLoadResult.getValue();
			}
			showSafePage();
		}

	}

	/** 加载数据的结果 */
	public enum LoadResult {
		ERROR(3), EMPTY(4), SUCCESS(5);
		/** 3为数据错误4为没有数据5为加载数据成功 */
		int value;

		LoadResult(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

}