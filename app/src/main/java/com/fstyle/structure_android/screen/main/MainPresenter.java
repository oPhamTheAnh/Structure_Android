package com.fstyle.structure_android.screen.main;

import android.util.Log;
import com.fstyle.structure_android.data.source.UserRepository;
import com.fstyle.structure_android.utils.common.StringUtils;
import com.fstyle.structure_android.utils.rx.BaseSchedulerProvider;
import com.fstyle.structure_android.utils.validator.Validator;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by le.quang.dao on 10/03/2017.
 */

class MainPresenter implements MainContract.Presenter {
    private static final String TAG = MainPresenter.class.getName();

    private MainContract.ViewModel mMainViewModel;
    private UserRepository mUserRepository;
    private Validator mValidator;
    private final CompositeSubscription mCompositeSubscription;
    private BaseSchedulerProvider mSchedulerProvider;

    MainPresenter(UserRepository userRepository, Validator validator,
            BaseSchedulerProvider schedulerProvider) {
        mUserRepository = userRepository;
        mValidator = validator;
        mSchedulerProvider = schedulerProvider;
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(mValidator.initNGWordPattern());
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
        mCompositeSubscription.clear();
    }

    @Override
    public void setViewModel(MainContract.ViewModel viewModel) {
        mMainViewModel = viewModel;
    }

    @Override
    public void validateKeywordInput(String keyword) {
        String message = mValidator.validateValueNonEmpty(keyword);
        if (StringUtils.isBlank(message)) {
            message = mValidator.validateNGWord(keyword);
        }
        mMainViewModel.onInvalidKeyWord(message);
    }

    @Override
    public void validateLimitNumberInput(String limit) {
        String message = mValidator.validateValueNonEmpty(limit);
        if (StringUtils.isBlank(message)) {
            message = mValidator.validateValueRangeFrom0to100(limit);
        }
        mMainViewModel.onInvalidLimitNumber(message);
    }

    @Override
    public boolean validateDataInput(String keyword, String limit) {
        validateKeywordInput(keyword);
        validateLimitNumberInput(limit);
        try {
            return mValidator.validateAll(mMainViewModel);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "validateDataInput: ", e);
            return false;
        }
    }

    @Override
    public void searchUsers(String keyWord, int limit) {
        Subscription subscription = mUserRepository.searchUsers(limit, keyWord)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(users -> mMainViewModel.onSearchUsersSuccess(users),
                        throwable -> mMainViewModel.onSearchError(throwable));
        mCompositeSubscription.add(subscription);
    }
}
