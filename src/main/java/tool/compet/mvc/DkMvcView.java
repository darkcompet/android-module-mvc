/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.mvc;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelStoreOwner;

@SuppressWarnings("rawtypes")
public interface DkMvcView<L extends DkMvcController, D> extends ViewModelStoreOwner {
	L newLogic();
	D newData();

	void setLogic(L logic);
	void setData(D data);

	<VM extends ViewModel> VM obtainOwnViewModel(String key, Class<VM> modelType);
}
