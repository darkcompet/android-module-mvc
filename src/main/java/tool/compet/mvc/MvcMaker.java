/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.mvc;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

@SuppressWarnings({"rawtypes", "unchecked"})
class MvcMaker {
	/**
	 * Start from all compact-annotated fields inside the View. Collect all compact-annotated fields
	 * which be specified in each type of field. After at all, init them and inject to correspond field.
	 * <p></p>
	 * Note that, this method must be called after #super.onCreate() inside subclass of View.
	 */
	static <V extends DkMvcView<L, D>, L extends DkMvcController, D> void make(
		V view,
		FragmentActivity host,
		@Nullable Bundle savedInstanceState
	) {
		// Init logic and data
		MvcContainer<L, D> container = view.obtainOwnViewModel(
			MvcContainer.class.getName(),
			MvcContainer.class
		);
		final boolean isInit = container.isInit;
		if (isInit) {
			container.isInit = false;
			container.logic = view.newLogic();
			container.data = view.newData();
		}

		L logic = container.logic;
		D data = container.data;

		// Attach Host & View & Data to Logic as soon as possible
		logic.host = host;
		logic.view = view;
		logic.data = data;

		// Pass Logic & Data to View as soon as possible
		view.setLogic(logic);
		view.setData(data);

		// Tell Logic init event
		if (isInit) {
			logic.onCreate(host, savedInstanceState);
		}
	}
}
