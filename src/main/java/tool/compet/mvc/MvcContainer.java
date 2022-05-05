package tool.compet.mvc;

import androidx.lifecycle.ViewModel;

// Must be public for initialization
@SuppressWarnings("rawtypes")
public class MvcContainer<L extends DkMvcController, D> extends ViewModel {
	boolean isInit = true;
	L logic;
	D data;

	@Override
	protected void onCleared() {
		super.onCleared();

		if (logic != null) {
			logic.onDestroy();
		}
	}
}
