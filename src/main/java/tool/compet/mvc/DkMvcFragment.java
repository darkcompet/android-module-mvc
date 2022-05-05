/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.mvc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import tool.compet.compactview.DkCompactFragment;
import tool.compet.navigation.DkNavigatorOwner;
import tool.compet.topic.DkTopicProvider;

/**
 * This is MVL View component. Provides more features as:
 * - [Optional] Navigator (we can forward, backward, dismiss... page easily)
 * - [Optional] Scoped topic (pass data between/under fragments, activities, app)
 * - [Optional] Compact Design pattern (coupling View and Logic), enable/disable via `enableCompactDesignPattern()`.
 * - [Optional] Message display (snack, toast...)
 *
 * @param <L> Logic
 * @param <D> Data
 * @param <B> ViewDataBinding
 */
@SuppressWarnings("rawtypes")
public abstract class DkMvcFragment<L extends DkMvcController, D, B extends ViewDataBinding>
	extends DkCompactFragment<B>
	implements DkMvcView<L, D>, DkNavigatorOwner, DkTopicProvider {

	/**
	 * Allow init Logic which couples with this View.
	 * So we can access the via `View.logic`.
	 */
	protected boolean enableCompactDesignPattern() {
		return true;
	}

	@Override
	public void setLogic(L logic) {
		this.logic = logic;
	}

	@Override
	public void setData(D data) {
		this.data = data;
	}

	/**
	 * Allow init child views via databinding feature.
	 * So we can access to child views via `binder.*` instead of calling findViewById().
	 */
	protected boolean enableDataBinding() {
		return true;
	}

	// Logic for View (to instantiate it, subclass just provide generic type of logic when extends this view)
	protected L logic;

	// Data for View (to instantiate it, subclass just provide generic type of data when extends this view)
	protected D data;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Must run after #super.onCreate()
		if (enableCompactDesignPattern()) {
			MvcMaker.make(this, host, savedInstanceState);

			if (logic != null) {
				logic.onViewCreate(host, savedInstanceState);
			}
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		// Let Logic run first, so View can use latest data which be updated from Logic
		if (logic != null) {
			logic.onViewReady(host, savedInstanceState);
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override // onViewCreated() -> onViewStateRestored() -> onStart()
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		if (logic != null) {
			logic.onViewRestoreInstanceState(host, savedInstanceState);
		}
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onStart() {
		if (logic != null) {
			logic.onViewStart(host);
		}
		super.onStart();
	}

	@Override
	public void onResume() {
		if (logic != null) {
			logic.onViewResume(host);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (logic != null) {
			logic.onViewPause(host);
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		if (logic != null) {
			logic.onViewStop(host);
		}
		super.onStop();
	}

	@Override // called before onDestroy()
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (logic != null) {
			logic.onViewSaveInstanceState(host, outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		if (logic != null) {
			logic.onViewDestroy(host);
		}
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if (logic != null) {
			logic.onLowMemory(host);
		}
		super.onLowMemory();
	}
}
