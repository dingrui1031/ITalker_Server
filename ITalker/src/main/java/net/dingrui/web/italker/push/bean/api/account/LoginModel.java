package net.dingrui.web.italker.push.bean.api.account;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;

/**
 * @author dingrui
 */
public class LoginModel {

    @Expose
    private String account;
    @Expose
    private String password;
    @Expose
    private String pushId;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    /**
     * 校验账户密码是否为空
     * @param loginModel
     * @return
     */
    public static boolean check(LoginModel loginModel) {
        return loginModel != null
                && !Strings.isNullOrEmpty(loginModel.account)
                && !Strings.isNullOrEmpty(loginModel.password);
    }
}
