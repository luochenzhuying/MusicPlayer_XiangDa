package com.music.xiangdamuxic;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.music.xiangdamuxic.ui.CircularAnim;
import com.music.xiangdamuxic.utils.Constant;
import com.music.xiangdamuxic.utils.Utils;

public class LoginActivity extends SuperActivity implements View.OnClickListener {
    /**
     * 密码输入框
     */
    private EditText mEtPassword;


    /**
     * 账户输入框
     */
    private EditText mEtMobile;

    private static final int REQUEST_CODE_GO_TO_REGIST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //ActionBar隐藏，透明化任务栏
        StatusBarUtil.setTransparent(this);

        //设置状态栏字体颜色为黑色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //初始化用户名输入框
        initUserNameEditText();

        //初始化密码输入框
        initPasswordEditText();

        //初始化图片点击（清除用户名，密码，显示隐藏密码）
        initImageButton();

        //初始化登录按钮
        initLoginButton();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_GO_TO_REGIST:
                //判断注册是否成功  如果注册成功
                if (resultCode == RESULT_OK) {
                    //则获取data中的账号和密码  动态设置到EditText中
                    String username = data.getStringExtra("user_name");
                    mEtMobile.setText(username);
                }
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_clean_phone:
                mEtMobile.setText("");
                break;
            case R.id.clean_password:
                mEtPassword.setText("");
                break;
            case R.id.iv_show_pwd:
                if (mEtPassword.getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    //密码可见
                    mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ImageView mIvShowPwd = findViewById(R.id.iv_show_pwd);
                    mIvShowPwd.setImageResource(R.drawable.pass_visuable);
                } else {
                    mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ImageView mIvShowPwd = findViewById(R.id.iv_show_pwd);
                    mIvShowPwd.setImageResource(R.drawable.pass_gone);
                }
                String pwd = mEtPassword.getText().toString();
                if (!TextUtils.isEmpty(pwd))
                    mEtPassword.setSelection(pwd.length());
                break;
        }
    }

    /**
     * 初始化登录界面
     */
    private void initLoginButton() {
        //获取
        final ProgressBar mProgressBar = findViewById(R.id.progressBar22);
        final Button login = findViewById(R.id.btn_login);

        //设置点击（mProgressBar和button配合）
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mEtMobile.getText())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "用户名为空", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 1080);//设置提示框显示的位置

                    toast.show();//显示消息
                } else if (TextUtils.isEmpty(mEtPassword.getText())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入正确的密码", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 1080);//设置提示框显示的位置

                    toast.show();//显示消息
                } else {

                    if (Utils.getString(LoginActivity.this, Constant.userNameSPKey, "").equals(mEtMobile.getText().toString())
                            && Utils.getString(LoginActivity.this, Constant.userPasswordSPKey, "").equals(mEtPassword.getText().toString())) {

                        CircularAnim.hide(login)
                                .endRadius(mProgressBar.getHeight() / 2)
                                .go(new CircularAnim.OnAnimationEndListener() {
                                    @Override
                                    public void onAnimationEnd() {
                                        login.setVisibility(View.GONE);
                                        mProgressBar.setVisibility(View.VISIBLE);

                                        mProgressBar.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                CircularAnim.fullActivity(LoginActivity.this, mProgressBar)
                                                        .colorOrImageRes(R.color.theme)
                                                        .go(new CircularAnim.OnAnimationEndListener() {
                                                            @Override
                                                            public void onAnimationEnd() {
                                                                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_LONG).show();

                                                                //登录成功将已经登录的标志存入SP中，下次进入直接进主页，无需登录
                                                                Utils.putBool(LoginActivity.this,Constant.isLogOn,true);

                                                                Intent intent = new Intent();
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                intent.setClass(LoginActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                            }
                                        }, 3000);
                                    }
                                });
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), "账户不存在或密码错误~，请重试或尝试注册~", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 1080);//设置提示框显示的位置

                        toast.show();//显示消息
                    }
                }
            }
        });

        //注册
        findViewById(R.id.regist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GO_TO_REGIST);
            }
        });
    }

    /**
     * 初始化图片点击（清除用户名，密码，显示隐藏密码）
     */
    private void initImageButton() {
        findViewById(R.id.iv_clean_phone).setOnClickListener(this);
        findViewById(R.id.clean_password).setOnClickListener(this);
        findViewById(R.id.iv_show_pwd).setOnClickListener(this);
    }


    /**
     * 初始化密码输入框
     */
    private void initPasswordEditText() {
        mEtPassword = findViewById(R.id.et_password);
        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            /**
             * 输入完检测
             * @param s
             */
            @Override
            public void afterTextChanged(Editable s) {
                ImageView mCleanPassword = findViewById(R.id.clean_password);
                if (!TextUtils.isEmpty(s) && mCleanPassword.getVisibility() == View.GONE) {
                    mCleanPassword.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(s)) {
                    mCleanPassword.setVisibility(View.GONE);
                }
                if (s.toString().isEmpty()) {
                    return;
                }
                if (!s.toString().matches("[A-Za-z0-9]+")) {
                    String temp = s.toString();
                    Toast.makeText(LoginActivity.this, "请输入数字或字母", Toast.LENGTH_SHORT).show();
                    s.delete(temp.length() - 1, temp.length());
                    //取消当前的输入
                    mEtPassword.setSelection(s.length());
                }
            }
        });
    }

    /**
     * 初始化用户名输入框
     */
    private void initUserNameEditText() {
        mEtMobile = findViewById(R.id.et_mobile);
        mEtMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ImageView mIvCleanPhone = findViewById(R.id.iv_clean_phone);
                if (!TextUtils.isEmpty(s) && mIvCleanPhone.getVisibility() == View.GONE) {
                    mIvCleanPhone.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(s)) {
                    mIvCleanPhone.setVisibility(View.GONE);
                }
            }
        });

    }
}
