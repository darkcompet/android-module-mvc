/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.mvc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import tool.compet.core.DkLogcats;
import tool.compet.core.DkRunner1;

/**
 * VML design pattern `Logic` component. This can invoke `View` by calling `sendToView()`
 * or use nullable `view` directly.
 *
 * In theory, Logic should accept view as a listener, but for convenience, we
 * declare view type for quickly development (access from editor).
 */
public abstract class DkMvcController<V, D> {
	// Indicate state of this logic and view
	protected static final int STATE_INVALID = -1;
	protected static final int STATE_CREATE = 0; // this logic is initialized (init)
	protected static final int STATE_VIEW_CREATE = 1; // view's onCreate()
	protected static final int STATE_VIEW_READY = 2; // view's Fragment.onViewCreated(), Activity.onCreate()
	protected static final int STATE_VIEW_START = 3; // view's onStart()
	protected static final int STATE_VIEW_RESUME = 4; // view's onResume()
	protected static final int STATE_VIEW_PAUSE = 5; // view's onPause()
	protected static final int STATE_VIEW_STOP = 6; // view's onStop()
	protected static final int STATE_VIEW_DESTROY = 7; // view's onDestroy()
	protected static final int STATE_DESTROY = 8; // this logic is destroyed (deinit)
	protected int state = STATE_INVALID;

	// Current host (be detached when this logic was destroyed)
	@Nullable
	protected FragmentActivity host;

	// Reference to the View, this field  will be attached and detached respectively at #onCreate(), #onDestroy().
	// Only use this field directly if you know the view is still present, otherwise lets use `sendToView()` instead.
	@Nullable
	protected V view;

	// Data for Logic and View
	protected D data;

	// Holds pending tasks which be sent from caller  via `sendToView()` while the `view` was absent.
	@Nullable
	private List<DkRunner1<V>> pendingTasks;

	// To make UI smooth, we post via this Handler to avoid run task on main thread directly
	private static final Handler handler = new Handler(Looper.getMainLooper());

	/**
	 * The task will be executed immediate if the View is in active (normally, onStart()+ -> onStop()-).
	 * Otherwise, the task will be added to pendingTasks and be executed when the View become active.
	 */
	protected void sendToView(DkRunner1<V> task) {
		if (this.state != STATE_DESTROY) {
			// View is not null, but layout maybe not yet ready, so we should see status of lifecycle state
			final V view = this.view;
			if (view != null && this.state >= STATE_VIEW_READY && this.state < STATE_VIEW_STOP) {
				task.run(view);
			}
			else {
				addPendingTask(task);
			}
		}
		else {
			DkLogcats.warning(this, "Ignore the task at current state: " + this.state);
		}
	}

	/**
	 * It is same with `sendToView()` except it does NOT execute given `task` immediately,
	 * but it post the task to and execute at main/ui thread.
	 *
	 * Since this method will create a GAP/JUMP of execution. So ONLY use it for scheduled task, for eg,.
	 * while the View is busy with lifecycle events: onCreate() -> onResume(), use this method will bring
	 * the execution to after onResumt(), so we can avoid lagging UI while View was busy.
	 */
	protected void postToView(DkRunner1<V> task) {
		handler.post(() -> sendToView(task));
	}

	/**
	 * Called only one time when create Logic and Data.
	 * It is coupled with `onDestroy()`.
	 */
	@CallSuper
	protected void onCreate(FragmentActivity host, @Nullable Bundle savedInstanceState) {
		state = STATE_CREATE;
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewDestroy()`.
	 */
	@CallSuper
	protected void onViewCreate(FragmentActivity host, @Nullable Bundle savedInstanceState) {
		state = STATE_VIEW_CREATE;
	}

	/**
	 * Called multiple times from View. The app can use `view` directly from this time,
	 * since `layout` inside View was initialized completely.
	 */
	@CallSuper
	protected void onViewReady(FragmentActivity host, @Nullable Bundle savedInstanceState) {
		state = STATE_VIEW_READY;
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewStop()`.
	 */
	@CallSuper
	protected void onViewStart(FragmentActivity host) {
		state = STATE_VIEW_START;

		// This is good time to execute pending actions since at most of cases,
		// this method is called. Of course, we can put execution at `onViewResume()`
		// but it is enough good place to run
		executePendingTasks();
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewInactive()`.
	 */
	@CallSuper
	protected void onViewResume(FragmentActivity host) {
		state = STATE_VIEW_RESUME;
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewActive()`.
	 */
	@CallSuper
	protected void onViewPause(FragmentActivity host) {
		state = STATE_VIEW_PAUSE;
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewStart()`.
	 */
	@CallSuper
	protected void onViewStop(FragmentActivity host) {
		state = STATE_VIEW_STOP;
	}

	/**
	 * Called multiple times from View.
	 * It is coupled with `onViewCreate()`.
	 * Subclass should stop using `view` at this time since we `layout` inside View
	 * is not ready.
	 */
	@CallSuper
	protected void onViewDestroy(FragmentActivity host) {
		this.state = STATE_VIEW_DESTROY;
		this.view = null;
	}

	@CallSuper
	protected void onViewSaveInstanceState(FragmentActivity host, Bundle outState) {
	}

	@CallSuper
	protected void onViewRestoreInstanceState(FragmentActivity host, Bundle savedInstanceState) {
	}

	/**
	 * Called only one time when this logic is destroyed.
	 * It is coupled with `onCreate()`.
	 */
	@CallSuper
	protected void onDestroy() {
		this.state = STATE_DESTROY;
		this.host = null;
	}

	/**
	 * Called when the app is in low memory.
	 */
	@CallSuper
	protected void onLowMemory(FragmentActivity host) {
	}

	private void addPendingTask(DkRunner1<V> task) {
		if (pendingTasks == null) {
			pendingTasks = new ArrayList<>();
		}
		pendingTasks.add(task);
	}

	// We try to execute pendingTasks in View, and to make UI smooth, we post tasks to main/ui thread instead of
	// run it directly in main thread.
	private void executePendingTasks() {
		handler.post(() -> {
			final V view = this.view;
			if (view != null) {
				// To make UI more smooth, we should avoid call View while it is busy for rendering
				final List<DkRunner1<V>> pendingTasks = this.pendingTasks;
				if (pendingTasks != null) {
					for (DkRunner1<V> action : pendingTasks) {
						action.run(view);
					}
					if (BuildConfig.DEBUG) {
						DkLogcats.info(this, "Executed %d pending actions", pendingTasks.size());
					}
					pendingTasks.clear();
				}
			}
		});
	}
}
