/*
 Copyright © 2015, 2016 Jenly Yu <a href="mailto:jenly1314@gmail.com">Jenly</a>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 	http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package com.king.base;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.king.base.model.EventMessage;
import com.king.base.util.StringUtils;
import com.king.base.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Jenly
 */
public abstract class BaseDialogFragment extends DialogFragment implements BaseInterface {

	protected Context context;

	private Dialog dialog;

	private BaseProgressDialog progressDialog;

	protected View rootView;

	protected int curPage;

	protected boolean isStop;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerEvent(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		context = getActivity();
		rootView = inflater.inflate(inflaterRootView(), container, false);

		curPage = 1;
		initUI();
		initData();
		addListeners();
		if(rootView!=null)
			return rootView;


		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dismissDialog();
		unregisterEvent(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		isStop = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		isStop = true;
	}

	protected View inflate(@LayoutRes int id){
		return inflate(id,null);
	}

	protected View inflate(@LayoutRes int id, @Nullable ViewGroup root){
		return LayoutInflater.from(context).inflate(id,root);
	}

	protected <T extends View> T findView(int resId){
		return (T)rootView.findViewById(resId);
	}

	protected void setOnClickListener(@IdRes int id,View.OnClickListener onClicklistener){
		findView(id).setOnClickListener(onClicklistener);
	}
	//-----------------------------------

	protected Intent getIntent(Class<?> cls){
		return new Intent(context,cls);
	}

	protected Intent getIntent(Class<?> cls,int flags){
		Intent intent = getIntent(cls);
		intent.setFlags(flags);
		return intent;
	}

	protected void startActivity(Class<?> cls){
		startActivity(getIntent(cls));
	}

	protected void startActivity(Class<?> cls,int flags){
		startActivity(getIntent(cls,flags));
	}

	protected void startActivityFinish(Class<?> cls){
		startActivity(cls);
		finish();
	}

	protected void startActivityFinish(Class<?> cls,int flags){
		startActivity(cls,flags);
		finish();
	}

	protected void finish(){
		getActivity().finish();
	}

	//-----------------------------------

	public void replaceFragment(@IdRes int resId, Fragment fragment){
		replaceFragment(resId,fragment,false);
	}

	public void replaceFragment(@IdRes int resId, Fragment fragment, boolean isBackStack) {
		FragmentTransaction fragmentTransaction =  getFragmentManager().beginTransaction();
		fragmentTransaction.replace(resId, fragment);
		if(isBackStack){
			fragmentTransaction.addToBackStack(null);
		}
		fragmentTransaction.commit();
	}

	//-----------------------------------


	protected void showToast(@StringRes  int resId){
		if(resId != NONE)
			ToastUtils.showToast(context,resId);
	}

	protected void showLongToast(@StringRes  int resId){
		if(resId != NONE)
			ToastUtils.showToast(context,resId, Toast.LENGTH_LONG);
	}

	protected void showToast(CharSequence text){
		ToastUtils.showToast(context,text);
	}

	protected void showLongToast(CharSequence text){
		ToastUtils.showToast(context,text, Toast.LENGTH_LONG);
	}

	//-----------------------------------

	public boolean checkInput(TextView tv){
		return checkInput(tv,NONE);
	}

	public boolean checkInput(TextView tv,@StringRes int resId){
		return checkInput(tv,resId,false);
	}

	public boolean checkInput(TextView tv,@StringRes int resId,boolean isShake){

		if(StringUtils.isBlank(tv.getText())){
			if(isShake)
				startShake(tv,resId);
			else
				showToast(resId);
			return false;
		}

		return true;
	}

	public void startShake(View v,@StringRes int resId){
		startShake(v);
		showToast(resId);
	}

	public void startShake(View view){
		view.startAnimation(AnimationUtils.loadAnimation(context,R.anim.shake));
	}


	/**
	 * 隐藏软键盘
	 *
	 * @param v
	 */
	public void hideInputMethod(final EditText v) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);

	}

	/**
	 * 显示软键盘
	 *
	 * @param v
	 */
	public void showInputMethod(final EditText v) {

		v.requestFocus();
		InputMethodManager imm = (InputMethodManager)context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v,InputMethodManager.SHOW_IMPLICIT);
	}

	//-----------------------------------

	public Dialog getProgressDialog() {
		return progressDialog;
	}

	public Dialog getDialog() {
		return dialog;
	}

	protected void dismissProgressDialog(){
		dismissDialog(progressDialog);
	}

	protected void dismissDialog(){
		dismissDialog(dialog);
	}

	protected void dismissDialog(Dialog dialog){
		if(dialog != null && dialog.isShowing()){
			dialog.dismiss();
		}
	}

	protected void dismissDialogFragment(DialogFragment dialogFragment){
		if(dialogFragment != null){
			dialogFragment.dismiss();
		}
	}

	protected void showProgressDialog(){
		showProgressDialog(new ProgressBar(context));
	}

	protected void showProgressDialog(@LayoutRes int resId){
		showProgressDialog(LayoutInflater.from(context).inflate(resId,null));
	}

	protected void showProgressDialog(View v){
		dismissProgressDialog();
		progressDialog = new BaseProgressDialog(context);
		progressDialog.setContentView(v);
		progressDialog.show();
	}

	protected void showDialog(View contentView){
		showDialog(context,contentView);
	}

	protected void showDialog(Context context,View contentView){
		dismissDialog();
		dialog = new Dialog(context,R.style.dialog);
		dialog.setContentView(contentView);
		dialog.setCanceledOnTouchOutside(false);
		getDialogWindow(dialog);
		dialog.show();

	}

	protected void getDialogWindow(Dialog dialog){
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = (int)(getWidthPixels()*0.8f);
		window.setAttributes(lp);
	}

	//-----------------------------------


	public static void registerEvent(Object obj){
		EventBus.getDefault().register(obj);
	}

	public static void unregisterEvent(Object obj){
		EventBus.getDefault().unregister(obj);
	}

	public static void sendEvent(Object obj){
		EventBus.getDefault().post(obj);
	}

	//-----------------------------------

	public void exit(){
		sendEvent(true);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(EventMessage em){
		onEventMessage(em);
	}

	protected void asyncThread(Runnable runnable){
		new Thread(runnable).start();
	}
	//-----------------------------------

	protected DisplayMetrics getDisplayMetrics(){
		return getResources().getDisplayMetrics();
	}

	protected int getWidthPixels(){
		return getDisplayMetrics().widthPixels;
	}

	protected int getHeightPixels(){
		return getDisplayMetrics().heightPixels;
	}

	//-----------------------------------



	@LayoutRes
	public abstract int inflaterRootView();
}