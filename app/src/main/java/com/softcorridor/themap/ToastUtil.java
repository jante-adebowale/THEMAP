package com.softcorridor.themap;

import android.content.Context;
import android.view.Gravity;

public class ToastUtil {
    //默认
    public static void showToast(Context context, String msg) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show();
    }


    public static void showToast(Context context, int resId) {
        android.widget.Toast.makeText(context, resId, android.widget.Toast.LENGTH_SHORT).show();
    }

    //定义位置
    public static void showToastTop(Context context, String msg) {
        android.widget.Toast toast= android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT/*2000 */);
        toast.setGravity(Gravity.TOP| Gravity.CENTER, 0, 120);
        toast.show();
    }

    public static void showToastDef(Context context, String msg, int pos, int tm) {
        android.widget.Toast toast= android.widget.Toast.makeText(context, msg, tm);
        toast.setGravity(Gravity.TOP| Gravity.CENTER, 0, pos);
        toast.show();
    }

    public static void showToastEx(Context context, String msg, int tm) {
        android.widget.Toast toast= android.widget.Toast.makeText(context, msg, tm);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

	/*
	//带图片
	Toast toast=Toast.makeText(getApplicationContext(), "显示带图片的toast", 3000);
	toast.setGravity(Gravity.CENTER, 0, 0);
	//创建图片视图对象
	ImageView imageView= new ImageView(getApplicationContext());
	//设置图片
	imageView.setImageResource(R.drawable.ic_launcher);
	//获得toast的布局
	LinearLayout toastView = (LinearLayout) toast.getView();
	//设置此布局为横向的
	toastView.setOrientation(LinearLayout.HORIZONTAL);
	//将ImageView在加入到此布局中的第一个位置
	toastView.addView(imageView, 0);
	toast.show();
	*/

    //完全自定义
	/*
	private void showToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        image = (ImageView) view.findViewById(R.id.image);
        title = (TextView) view.findViewById(R.id.title);
        content = (TextView) view.findViewById(R.id.content);
        image.setBackgroundResource(R.drawable.ic_launcher);
        title.setText("自定义toast");
        content.setText("hello,self toast");
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
	*/

}
