package net.dingrui.web.italker.push.service;

import com.google.common.base.Strings;
import net.dingrui.web.italker.push.bean.api.account.AccountRspModel;
import net.dingrui.web.italker.push.bean.api.account.LoginModel;
import net.dingrui.web.italker.push.bean.api.account.RegisterModel;
import net.dingrui.web.italker.push.bean.api.base.ResponseModel;
import net.dingrui.web.italker.push.bean.db.User;
import net.dingrui.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author dingrui
 */
// 127.0.0.1/api/account/...
@Path("/account")
public class AccountService {

    @POST
    //指定请求与返回的响应体
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public ResponseModel<AccountRspModel> register(RegisterModel registerModel) {

        //检测用户名密码姓名是否为空
        if (!RegisterModel.check(registerModel)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.findByPhone(registerModel.getAccount().trim());
        if (user != null) {
            //返回已有账户
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(registerModel.getName().trim());
        if (user != null) {
            //返回已有用户名
            return ResponseModel.buildHaveNameError();
        }

        //开始注册逻辑
        user = UserFactory.register(registerModel.getAccount(),
                registerModel.getPassword(),
                registerModel.getName());

        if (user != null) {

            //如果有pushId
            if (!Strings.isNullOrEmpty(registerModel.getPushId())) {
                return bind(user, registerModel.getPushId());
            }

            //返回当前账户
            AccountRspModel accountRspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(accountRspModel);
        } else {
            //注册异常
            return ResponseModel.buildRegisterError();
        }
    }

    @POST
    //指定请求与返回的响应体
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public ResponseModel<AccountRspModel> login(LoginModel loginModel) {

        //检测用户名密码是否为空
        if (!LoginModel.check(loginModel)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.login(loginModel.getAccount(), loginModel.getPassword());

        if (user != null) {

            //如果有pushId
            if (!Strings.isNullOrEmpty(loginModel.getPushId())) {
                return bind(user, loginModel.getPushId());
            }

            //返回当前的账户
            AccountRspModel accountRspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(accountRspModel);
        } else {
            //返回登陆失败
            return ResponseModel.buildLoginError();
        }
    }


    @POST
    //指定请求与返回的响应体
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bind/{pushId}")
    //从请求头中获取token字段
    //pushId从url地址中获取
    public ResponseModel<AccountRspModel> bind(@HeaderParam("token") String token,
                                               @PathParam("pushId") String pushId) {

        //检测用户名密码是否为空
        if (Strings.isNullOrEmpty("token") || Strings.isNullOrEmpty("pushId")) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        //拿到用户自己的信息
        User user = UserFactory.findByToken(token);
        return bind(user, pushId);
    }

    /**
     * 绑定的操作
     *
     * @param self   自己
     * @param pushId pushId
     * @return
     */
    private ResponseModel<AccountRspModel> bind(User self, String pushId) {
        //进行设备id绑定的操作
        User user = UserFactory.bindPushId(self, pushId);

        if (user == null) {
            //绑定失败返回服务器异常
            return ResponseModel.buildServiceError();
        }

        //返回当前账户，并且绑定
        AccountRspModel accountRspModel = new AccountRspModel(user,true);
        return ResponseModel.buildOk(accountRspModel);
    }
}
