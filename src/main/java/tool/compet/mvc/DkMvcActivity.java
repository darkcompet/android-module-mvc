/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.mvc;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import tool.compet.compactview.DkCompactActivity;
import tool.compet.navigation.DkNavigatorOwner;
import tool.compet.topic.DkTopicProvider;

/**
 * This is MVL Logic component. Provides more features as:
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
public abstract class DkMvcActivity<L extends DkMvcController, D, B extends ViewDataBinding>
	extends DkCompactActivity<B>
	implements DkMvcView<L, D>, DkNavigatorOwner, DkTopicProvider {

	/**
	 * Allow setup Logic which is considered as Controller of the View.
	 */
	protected boolean enableDesignPattern() {
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

	// Logic for View (to instantiate it, subclass just provide generic type of logic when extends this view)
	protected L logic;

	// Data for View (to instantiate it, subclass just provide generic type of data when extends this view)
	protected D data;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Must run after #super.onCreate()
		if (enableDesignPattern()) {
			MvcMaker.make(this, this, savedInstanceState);

			if (logic != null) {
				logic.onViewCreate(this, savedInstanceState);
			}
		}
	}

	@CallSuper
	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		// Let Logic run first, so View can use latest data which be updated from Logic
		if (logic != null) {
			logic.onViewReady(this, savedInstanceState);
		}
		super.onPostCreate(savedInstanceState);
	}

	@Override // onPostCreate() -> onRestoreInstanceState() -> onStart()
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		if (logic != null) {
			logic.onViewRestoreInstanceState(this, savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		if (logic != null) {
			logic.onViewStart(this);
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		if (logic != null) {
			logic.onViewResume(this);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (logic != null) {
			logic.onViewPause(this);
		}
		super.onPause();
	}

	@Override // maybe called before onStop() or onDestroy()
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (logic != null) {
			logic.onViewSaveInstanceState(this, outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		if (logic != null) {
			logic.onViewStop(this);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (logic != null) {
			logic.onViewDestroy(this);
		}
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if (logic != null) {
			logic.onLowMemory(this);
		}
		super.onLowMemory();
	}
}
